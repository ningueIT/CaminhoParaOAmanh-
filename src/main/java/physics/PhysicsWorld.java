package physics;

import entities.Entity;
import entities.Gate;
import entities.Platform;

import java.util.List;

public final class PhysicsWorld {
    private final double worldWidth;
    private final double floorY;
    private final List<Platform> platforms;
    private final List<Gate> gates;

    public PhysicsWorld(double worldWidth, double floorY, List<Platform> platforms) {
        this(worldWidth, floorY, platforms, List.of());
    }

    public PhysicsWorld(double worldWidth, double floorY, List<Platform> platforms, List<Gate> gates) {
        this.worldWidth = worldWidth;
        this.floorY = floorY;
        this.platforms = List.copyOf(platforms);
        this.gates = List.copyOf(gates);
    }

    public void resolve(Entity entity) {
        entity.setOnGround(false);
        clampHorizontalBounds(entity);
        resolveFloorCollision(entity);
        resolvePlatformCollisions(entity);
        resolveGateCollisions(entity);
        clampHorizontalBounds(entity);
    }

    public double getFloorY() {
        return floorY;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Gate> getGates() {
        return gates;
    }

    private void clampHorizontalBounds(Entity entity) {
        double clampedX = Math.max(0.0, Math.min(entity.getX(), worldWidth - entity.getWidth()));

        if (clampedX != entity.getX()) {
            entity.setPosition(clampedX, entity.getY());
            entity.setVelocityX(0.0);
        }
    }

    private void resolveFloorCollision(Entity entity) {
        AABB bounds = entity.getBounds();

        if (bounds.getBottom() < floorY) {
            return;
        }

        entity.setPosition(entity.getX(), floorY - entity.getHeight());
        entity.setVelocityY(0.0);
        entity.setOnGround(true);
    }

    private void resolvePlatformCollisions(Entity entity) {
        for (Platform platform : platforms) {
            AABB entityBounds = entity.getBounds();
            AABB platformBounds = platform.getBounds();

            if (!entityBounds.intersects(platformBounds)) {
                continue;
            }

            resolveSolidCollision(entity, entityBounds, platformBounds);
        }
    }

    private void resolveGateCollisions(Entity entity) {
        for (Gate gate : gates) {
            if (gate.isOpen()) {
                continue;
            }

            AABB entityBounds = entity.getBounds();
            AABB gateBounds = gate.getBounds();

            if (!entityBounds.intersects(gateBounds)) {
                continue;
            }

            // Portoes fechados compartilham a mesma resolucao AABB das plataformas.
            resolveSolidCollision(entity, entityBounds, gateBounds);
        }
    }

    private void resolveSolidCollision(Entity entity, AABB entityBounds, AABB obstacleBounds) {
        double overlapX = Math.min(entityBounds.getRight(), obstacleBounds.getRight())
                - Math.max(entityBounds.getLeft(), obstacleBounds.getLeft());
        double overlapY = Math.min(entityBounds.getBottom(), obstacleBounds.getBottom())
                - Math.max(entityBounds.getTop(), obstacleBounds.getTop());

        if (overlapX <= 0.0 || overlapY <= 0.0) {
            return;
        }

        // A menor profundidade de penetracao indica o eixo mais barato para separar os dois AABBs.
        if (overlapX < overlapY) {
            resolveHorizontalCollision(entity, entityBounds, obstacleBounds, overlapX);
            return;
        }

        // No eixo Y, empurramos para cima ou para baixo conforme o lado do impacto e zeramos a velocidade vertical.
        resolveVerticalCollision(entity, entityBounds, obstacleBounds, overlapY);
    }

    private void resolveHorizontalCollision(Entity entity, AABB entityBounds, AABB platformBounds, double overlapX) {
        boolean hitFromLeft = entity.getVelocityX() > 0.0
                || (entity.getVelocityX() == 0.0 && entityBounds.getLeft() < platformBounds.getLeft());

        double resolvedX = hitFromLeft ? entity.getX() - overlapX : entity.getX() + overlapX;
        entity.setPosition(resolvedX, entity.getY());
        entity.setVelocityX(0.0);
    }

    private void resolveVerticalCollision(Entity entity, AABB entityBounds, AABB platformBounds, double overlapY) {
        boolean landedOnTop = entity.getVelocityY() > 0.0
                || (entity.getVelocityY() == 0.0 && entityBounds.getTop() < platformBounds.getTop());

        double resolvedY = landedOnTop ? entity.getY() - overlapY : entity.getY() + overlapY;
        entity.setPosition(entity.getX(), resolvedY);
        entity.setVelocityY(0.0);

        if (landedOnTop) {
            entity.setOnGround(true);
        }
    }
}
