package entities;

import physics.AABB;
import physics.PhysicsWorld;

import java.awt.Color;
import java.awt.Graphics2D;

public final class PatrolEnemy extends Enemy {
    private static final double PATROL_SPEED = 90.0;
    private static final double GRAVITY = 1400.0;
    private static final double LEDGE_PROBE_OFFSET = 6.0;
    private static final double GROUND_PROBE_DEPTH = 4.0;

    private int direction;

    public PatrolEnemy(double x, double y, double width, double height, int maxHealth, int initialDirection) {
        super(x, y, width, height, maxHealth);
        this.direction = initialDirection >= 0 ? 1 : -1;
    }

    @Override
    protected void updateBehavior(double deltaSeconds, PhysicsWorld physicsWorld, Player player) {
        setVelocityX(direction * PATROL_SPEED);
        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
    }

    @Override
    public void afterPhysicsResolve(PhysicsWorld physicsWorld) {
        if (isDead()) {
            return;
        }

        boolean hitWall = isOnGround() && Math.abs(getVelocityX()) < 0.001;
        boolean reachedEdge = isOnGround() && !hasGroundAhead(physicsWorld);

        if (hitWall || reachedEdge) {
            direction *= -1;
        }
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

        Color bodyColor = isRecentlyHit() ? new Color(255, 168, 168) : new Color(165, 82, 82);
        g2d.setColor(bodyColor);
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 14, 14);

        g2d.setColor(new Color(73, 29, 29));
        g2d.fillRect(renderX + 8, renderY + 14, 8, 8);
        g2d.fillRect(renderX + renderWidth - 16, renderY + 14, 8, 8);
    }

    private boolean hasGroundAhead(PhysicsWorld physicsWorld) {
        double probeX = direction > 0 ? getX() + getWidth() + LEDGE_PROBE_OFFSET : getX() - LEDGE_PROBE_OFFSET;
        double probeY = getY() + getHeight() + GROUND_PROBE_DEPTH;

        if (probeY >= physicsWorld.getFloorY()) {
            return true;
        }

        for (Platform platform : physicsWorld.getPlatforms()) {
            AABB bounds = platform.getBounds();
            boolean withinX = probeX >= bounds.getLeft() && probeX <= bounds.getRight();
            boolean withinY = probeY >= bounds.getTop() && probeY <= bounds.getBottom();

            if (withinX && withinY) {
                return true;
            }
        }

        return false;
    }
}
