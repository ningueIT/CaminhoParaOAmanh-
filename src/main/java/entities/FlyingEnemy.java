package entities;

import physics.PhysicsWorld;

import java.awt.Color;
import java.awt.Graphics2D;

public final class FlyingEnemy extends Enemy {
    private static final double PATROL_SPEED = 90.0;
    private static final double PURSUIT_SPEED = 190.0;
    private static final double PURSUIT_RANGE = 280.0;
    private static final double WAVE_AMPLITUDE = 36.0;
    private static final double WAVE_ANGULAR_FREQUENCY = 2.8;

    private final double patrolCenterY;
    private int direction;
    private double waveTime;

    public FlyingEnemy(double x, double y, double width, double height, int maxHealth, int initialDirection) {
        super(x, y, width, height, maxHealth);
        this.patrolCenterY = y;
        this.direction = initialDirection >= 0 ? 1 : -1;
    }

    @Override
    protected void updateBehavior(double deltaSeconds, PhysicsWorld physicsWorld, Player player) {
        constrainToWorldBounds(physicsWorld);

        double horizontalDistance = getCenterX() - getPlayerCenterX(player);
        double verticalDistance = getCenterY() - getPlayerCenterY(player);
        double distance = Math.hypot(horizontalDistance, verticalDistance);
        if (distance <= PURSUIT_RANGE && distance > 0.0) {
            pursuePlayer(player, distance);
            return;
        }

        patrolInWave(deltaSeconds, physicsWorld);
    }

    @Override
    public boolean usesWorldPhysics() {
        return false;
    }

    @Override
    public void render(Graphics2D g2d, double alpha) {
        if (isDead()) {
            return;
        }

        int renderX = (int) Math.round(getRenderX(alpha));
        int renderY = (int) Math.round(getRenderY(alpha));
        int renderWidth = (int) Math.round(getWidth());
        int renderHeight = (int) Math.round(getHeight());

        Color bodyColor = isRecentlyHit() ? new Color(204, 241, 255) : new Color(96, 172, 212);
        g2d.setColor(bodyColor);
        g2d.fillOval(renderX, renderY, renderWidth, renderHeight);

        g2d.setColor(new Color(44, 72, 119));
        g2d.fillOval(renderX - 8, renderY + renderHeight / 3, 14, renderHeight / 3);
        g2d.fillOval(renderX + renderWidth - 6, renderY + renderHeight / 3, 14, renderHeight / 3);

        g2d.setColor(new Color(24, 39, 77));
        g2d.fillOval(renderX + renderWidth / 4, renderY + renderHeight / 3, 6, 6);
        g2d.fillOval(renderX + (renderWidth * 2) / 3, renderY + renderHeight / 3, 6, 6);
    }

    private void pursuePlayer(Player player, double distance) {
        double horizontalDistance = getPlayerCenterX(player) - getCenterX();
        double verticalDistance = getPlayerCenterY(player) - getCenterY();
        setVelocityX((horizontalDistance / distance) * PURSUIT_SPEED);
        setVelocityY((verticalDistance / distance) * PURSUIT_SPEED);

        if (horizontalDistance != 0.0) {
            direction = horizontalDistance > 0.0 ? 1 : -1;
        }
    }

    private void patrolInWave(double deltaSeconds, PhysicsWorld physicsWorld) {
        waveTime += deltaSeconds;
        if (getX() <= 0.0 || getX() + getWidth() >= physicsWorld.getWorldWidth()) {
            direction *= -1;
        }

        double targetY = patrolCenterY + Math.sin(waveTime * WAVE_ANGULAR_FREQUENCY) * WAVE_AMPLITUDE;
        setVelocityX(direction * PATROL_SPEED);
        setVelocityY((targetY - getY()) / deltaSeconds);
    }

    private void constrainToWorldBounds(PhysicsWorld physicsWorld) {
        double maxX = Math.max(0.0, physicsWorld.getWorldWidth() - getWidth());
        double maxY = Math.max(0.0, physicsWorld.getFloorY() - getHeight());
        double clampedX = Math.max(0.0, Math.min(getX(), maxX));
        double clampedY = Math.max(0.0, Math.min(getY(), maxY));
        if (clampedX != getX() || clampedY != getY()) {
            setPosition(clampedX, clampedY);
        }
    }

    private double getCenterX() {
        return getX() + getWidth() * 0.5;
    }

    private double getCenterY() {
        return getY() + getHeight() * 0.5;
    }

    private double getPlayerCenterX(Player player) {
        return player.getX() + player.getWidth() * 0.5;
    }

    private double getPlayerCenterY(Player player) {
        return player.getY() + player.getHeight() * 0.5;
    }
}
