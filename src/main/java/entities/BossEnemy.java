package entities;

import physics.PhysicsWorld;

import java.awt.Color;
import java.awt.Graphics2D;

public final class BossEnemy extends Enemy {
    private static final double PATROL_SPEED = 150.0;
    private static final double GRAVITY = 1400.0;
    private static final double LEAP_HORIZONTAL_SPEED = 410.0;
    private static final double LEAP_VERTICAL_SPEED = 660.0;
    private static final double LEAP_INTERVAL = 1.75;

    private BossPhase phase = BossPhase.PATROLLING;
    private int direction;
    private double leapTimer = LEAP_INTERVAL;

    public BossEnemy(double x, double y, double width, double height, int maxHealth, int initialDirection) {
        super(x, y, width, height, maxHealth);
        this.direction = initialDirection >= 0 ? 1 : -1;
    }

    @Override
    protected void updateBehavior(double deltaSeconds, PhysicsWorld physicsWorld, Player player) {
        if (phase == BossPhase.LEAPING) {
            setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
            return;
        }

        leapTimer = Math.max(0.0, leapTimer - deltaSeconds);
        setVelocityX(direction * PATROL_SPEED);
        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);

        if (leapTimer == 0.0 && isOnGround()) {
            startLeap(player);
        }
    }

    @Override
    public void afterPhysicsResolve(PhysicsWorld physicsWorld) {
        if (isDead()) {
            return;
        }

        if (phase == BossPhase.LEAPING && isOnGround()) {
            phase = BossPhase.PATROLLING;
            leapTimer = LEAP_INTERVAL;
            return;
        }

        if (phase == BossPhase.PATROLLING && isOnGround() && Math.abs(getVelocityX()) < 0.001) {
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

        Color bodyColor = isRecentlyHit() ? new Color(255, 201, 138) : new Color(119, 55, 103);
        g2d.setColor(bodyColor);
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 20, 20);

        g2d.setColor(new Color(57, 24, 57));
        g2d.fillRect(renderX + renderWidth / 5, renderY + renderHeight / 4, renderWidth / 6, renderHeight / 8);
        g2d.fillRect(renderX + (renderWidth * 3) / 5, renderY + renderHeight / 4, renderWidth / 6, renderHeight / 8);
        g2d.fillRect(renderX + renderWidth / 4, renderY + (renderHeight * 3) / 4, renderWidth / 2, 6);
    }

    public boolean isLeaping() {
        return phase == BossPhase.LEAPING;
    }

    private void startLeap(Player player) {
        double playerCenterX = player.getX() + player.getWidth() * 0.5;
        double bossCenterX = getX() + getWidth() * 0.5;
        direction = playerCenterX >= bossCenterX ? 1 : -1;
        phase = BossPhase.LEAPING;
        setVelocityX(direction * LEAP_HORIZONTAL_SPEED);
        setVelocityY(-LEAP_VERTICAL_SPEED);
    }

    private enum BossPhase {
        PATROLLING,
        LEAPING
    }
}
