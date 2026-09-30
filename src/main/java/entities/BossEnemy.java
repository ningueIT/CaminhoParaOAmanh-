package entities;

import physics.PhysicsWorld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Queue;

public final class BossEnemy extends Enemy {
    private static final double GRAVITY = 1400.0;
    private static final double IDLE_DURATION = 0.70;
    private static final double CHARGE_WINDUP_DURATION = 0.58;
    private static final double CHARGE_DURATION = 0.64;
    private static final double CHARGE_SPEED = 680.0;
    private static final double ENRAGED_IDLE_DURATION = 0.44;
    private static final double ENRAGED_CHARGE_WINDUP_DURATION = 0.42;
    private static final double ENRAGED_CHARGE_DURATION = 0.56;
    private static final double ENRAGED_CHARGE_SPEED = 790.0;
    private static final double PROJECTILE_DURATION = 1.25;
    private static final double PROJECTILE_INTERVAL = 0.28;
    private static final double ENRAGED_PROJECTILE_INTERVAL = 0.19;
    private static final double PROJECTILE_SPEED = 390.0;
    private static final double ENRAGED_PROJECTILE_SPEED = 455.0;
    private static final double CORRUPTION_DURATION = 1.25;
    private static final double RECOVERY_DURATION = 0.55;
    private static final double ENRAGED_RECOVERY_DURATION = 0.40;
    private static final int PROJECTILES_PER_VOLLEY = 3;
    private static final int ENRAGED_PROJECTILES_PER_VOLLEY = 5;
    private static final double CORRUPTION_ZONE_WIDTH = 118.0;
    private static final double CORRUPTION_ZONE_HEIGHT = 26.0;
    private static final double CORRUPTION_ZONE_LIFETIME = 2.60;
    private static final double CORRUPTION_TELEGRAPH_DURATION = 0.68;

    private final Queue<BossProjectile> pendingProjectiles = new ArrayDeque<>();
    private final Queue<CorruptionZone> pendingCorruptionZones = new ArrayDeque<>();
    private BossPhase phase = BossPhase.IDLE;
    private int direction;
    private int nextAttack;
    private int projectilesRemaining;
    private int projectileIndex;
    private int projectileVolleySize;
    private int chargesRemaining;
    private double phaseTimer = IDLE_DURATION;
    private double projectileTimer;

    public BossEnemy(double x, double y, double width, double height, int maxHealth, int initialDirection) {
        super(x, y, width, height, maxHealth);
        this.direction = initialDirection >= 0 ? 1 : -1;
    }

    @Override
    protected void updateBehavior(double deltaSeconds, PhysicsWorld physicsWorld, Player player) {
        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
        switch (phase) {
            case IDLE -> updateIdle(deltaSeconds, physicsWorld, player);
            case CHARGE_WINDUP -> updateChargeWindup(deltaSeconds);
            case CHARGING -> updateCharge(deltaSeconds, player);
            case PROJECTILES -> updateProjectileVolley(deltaSeconds, player);
            case CORRUPTION -> updateCorruption(deltaSeconds);
            case RECOVERING -> updateRecovery(deltaSeconds);
        }
    }

    @Override
    public void afterPhysicsResolve(PhysicsWorld physicsWorld) {
        if (isDead()) {
            return;
        }

        if (phase == BossPhase.CHARGING && Math.abs(getVelocityX()) < 0.001) {
            beginRecovery();
            return;
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

        Color bodyColor = getBodyColor();
        g2d.setColor(bodyColor);
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 26, 26);

        g2d.setColor(new Color(40, 17, 50));
        for (int index = 0; index < 4; index++) {
            int rootX = renderX + renderWidth / 6 + index * renderWidth / 5;
            g2d.fillRect(rootX, renderY + renderHeight * 3 / 4, 6, renderHeight / 3);
        }

        Color eyeColor = phase == BossPhase.CHARGE_WINDUP ? new Color(255, 230, 122) : new Color(243, 94, 188);
        g2d.setColor(eyeColor);
        g2d.fillOval(renderX + renderWidth / 5, renderY + renderHeight / 4, renderWidth / 6, renderHeight / 8);
        g2d.fillOval(renderX + (renderWidth * 3) / 5, renderY + renderHeight / 4, renderWidth / 6, renderHeight / 8);
        g2d.setColor(new Color(35, 14, 43));
        g2d.fillRoundRect(renderX + renderWidth / 4, renderY + (renderHeight * 3) / 5, renderWidth / 2, 7, 7, 7);
    }

    public boolean isLeaping() {
        return phase == BossPhase.CHARGING;
    }

    public boolean isCharging() {
        return phase == BossPhase.CHARGING;
    }

    public BossProjectile consumePendingProjectile() {
        return pendingProjectiles.poll();
    }

    public CorruptionZone consumePendingCorruptionZone() {
        return pendingCorruptionZones.poll();
    }

    private void updateIdle(double deltaSeconds, PhysicsWorld physicsWorld, Player player) {
        setVelocityX(0.0);
        phaseTimer = Math.max(0.0, phaseTimer - deltaSeconds);
        if (phaseTimer == 0.0) {
            beginNextAttack(physicsWorld, player);
        }
    }

    private void updateChargeWindup(double deltaSeconds) {
        setVelocityX(0.0);
        phaseTimer = Math.max(0.0, phaseTimer - deltaSeconds);
        if (phaseTimer == 0.0) {
            phase = BossPhase.CHARGING;
            phaseTimer = isEnraged() ? ENRAGED_CHARGE_DURATION : CHARGE_DURATION;
            setVelocityX(direction * getChargeSpeed());
        }
    }

    private void updateCharge(double deltaSeconds, Player player) {
        setVelocityX(direction * getChargeSpeed());
        phaseTimer = Math.max(0.0, phaseTimer - deltaSeconds);
        if (phaseTimer == 0.0) {
            chargesRemaining--;
            if (chargesRemaining > 0) {
                startChargeWindup(player);
            } else {
                beginRecovery();
            }
        }
    }

    private void updateProjectileVolley(double deltaSeconds, Player player) {
        setVelocityX(0.0);
        phaseTimer = Math.max(0.0, phaseTimer - deltaSeconds);
        projectileTimer = Math.max(0.0, projectileTimer - deltaSeconds);
        if (projectilesRemaining > 0 && projectileTimer == 0.0) {
            queueProjectile(player, projectileIndex, projectileVolleySize);
            projectilesRemaining--;
            projectileIndex++;
            projectileTimer = getProjectileInterval();
        }
        if (phaseTimer == 0.0) {
            beginRecovery();
        }
    }

    private void updateCorruption(double deltaSeconds) {
        setVelocityX(0.0);
        phaseTimer = Math.max(0.0, phaseTimer - deltaSeconds);
        if (phaseTimer == 0.0) {
            beginRecovery();
        }
    }

    private void updateRecovery(double deltaSeconds) {
        setVelocityX(0.0);
        phaseTimer = Math.max(0.0, phaseTimer - deltaSeconds);
        if (phaseTimer == 0.0) {
            phase = BossPhase.IDLE;
            phaseTimer = getIdleDuration();
        }
    }

    private void beginNextAttack(PhysicsWorld physicsWorld, Player player) {
        switch (nextAttack) {
            case 0 -> beginChargeAttack(player);
            case 1 -> beginProjectileVolley();
            case 2 -> beginCorruption(physicsWorld, player);
            default -> throw new IllegalStateException("Unknown boss attack: " + nextAttack);
        }
        nextAttack = (nextAttack + 1) % 3;
    }

    private void beginChargeAttack(Player player) {
        chargesRemaining = isEnraged() ? 2 : 1;
        startChargeWindup(player);
    }

    private void startChargeWindup(Player player) {
        double playerCenterX = player.getX() + player.getWidth() * 0.5;
        double bossCenterX = getX() + getWidth() * 0.5;
        direction = playerCenterX >= bossCenterX ? 1 : -1;
        phase = BossPhase.CHARGE_WINDUP;
        phaseTimer = isEnraged() ? ENRAGED_CHARGE_WINDUP_DURATION : CHARGE_WINDUP_DURATION;
    }

    private void beginProjectileVolley() {
        phase = BossPhase.PROJECTILES;
        phaseTimer = PROJECTILE_DURATION;
        projectileTimer = 0.0;
        projectileVolleySize = getProjectilesPerVolley();
        projectilesRemaining = projectileVolleySize;
        projectileIndex = 0;
    }

    private void beginCorruption(PhysicsWorld physicsWorld, Player player) {
        phase = BossPhase.CORRUPTION;
        phaseTimer = CORRUPTION_DURATION;

        double playerCenterX = player.getX() + player.getWidth() * 0.5;
        int zoneCount = isEnraged() ? 5 : 3;
        double zoneSpacing = isEnraged() ? 150.0 : 190.0;
        for (int index = 0; index < zoneCount; index++) {
            double offset = (index - (zoneCount - 1) * 0.5) * zoneSpacing;
            double zoneX = playerCenterX + offset - CORRUPTION_ZONE_WIDTH * 0.5;
            zoneX = Math.max(0.0, Math.min(zoneX, physicsWorld.getWorldWidth() - CORRUPTION_ZONE_WIDTH));
            double zoneCenterX = zoneX + CORRUPTION_ZONE_WIDTH * 0.5;
            double zoneY = findGroundSurfaceY(physicsWorld, zoneCenterX, player.getY() + player.getHeight())
                    - CORRUPTION_ZONE_HEIGHT;
            pendingCorruptionZones.add(new CorruptionZone(
                    zoneX,
                    zoneY,
                    CORRUPTION_ZONE_WIDTH,
                    CORRUPTION_ZONE_HEIGHT,
                    CORRUPTION_ZONE_LIFETIME,
                    CORRUPTION_TELEGRAPH_DURATION
            ));
        }
    }

    private double findGroundSurfaceY(PhysicsWorld physicsWorld, double x, double minimumSurfaceY) {
        double surfaceY = physicsWorld.getFloorY();
        for (Platform platform : physicsWorld.getPlatforms()) {
            double platformLeft = platform.getBounds().getLeft();
            double platformRight = platform.getBounds().getRight();
            double platformTop = platform.getBounds().getTop();
            if (x >= platformLeft && x <= platformRight
                    && platformTop >= minimumSurfaceY - 4.0
                    && platformTop < surfaceY) {
                surfaceY = platformTop;
            }
        }
        return surfaceY;
    }

    private void beginRecovery() {
        phase = BossPhase.RECOVERING;
        phaseTimer = isEnraged() ? ENRAGED_RECOVERY_DURATION : RECOVERY_DURATION;
        setVelocityX(0.0);
    }

    private void queueProjectile(Player player, int index, int projectileCount) {
        double projectileX = getX() + getWidth() * 0.5;
        double projectileY = getY() + getHeight() / 3.0;
        double targetX = player.getX() + player.getWidth() * 0.5;
        double targetY = player.getY() + player.getHeight() * 0.5;
        double distanceX = targetX - projectileX;
        double distanceY = targetY - projectileY;
        double distance = Math.hypot(distanceX, distanceY);
        if (distance == 0.0) {
            distance = 1.0;
        }

        double spreadRadians = Math.toRadians(isEnraged() ? 10.0 : 7.0);
        double angle = (index - (projectileCount - 1) * 0.5) * spreadRadians;
        double normalizedX = distanceX / distance;
        double normalizedY = distanceY / distance;
        double rotatedX = normalizedX * Math.cos(angle) - normalizedY * Math.sin(angle);
        double rotatedY = normalizedX * Math.sin(angle) + normalizedY * Math.cos(angle);
        double speed = isEnraged() ? ENRAGED_PROJECTILE_SPEED : PROJECTILE_SPEED;

        pendingProjectiles.add(new BossProjectile(
                projectileX,
                projectileY,
                rotatedX * speed,
                rotatedY * speed
        ));
    }

    private boolean isEnraged() {
        return getCurrentHealth() <= getMaxHealth() / 2;
    }

    private double getIdleDuration() {
        return isEnraged() ? ENRAGED_IDLE_DURATION : IDLE_DURATION;
    }

    private double getChargeSpeed() {
        return isEnraged() ? ENRAGED_CHARGE_SPEED : CHARGE_SPEED;
    }

    private double getProjectileInterval() {
        return isEnraged() ? ENRAGED_PROJECTILE_INTERVAL : PROJECTILE_INTERVAL;
    }

    private int getProjectilesPerVolley() {
        return isEnraged() ? ENRAGED_PROJECTILES_PER_VOLLEY : PROJECTILES_PER_VOLLEY;
    }

    private Color getBodyColor() {
        if (isRecentlyHit()) {
            return new Color(255, 201, 138);
        }
        if (phase == BossPhase.CHARGE_WINDUP || phase == BossPhase.CHARGING) {
            return new Color(154, 55, 105);
        }
        if (phase == BossPhase.CORRUPTION) {
            return new Color(95, 49, 122);
        }
        if (isEnraged()) {
            return new Color(154, 62, 91);
        }
        return new Color(119, 55, 103);
    }

    private enum BossPhase {
        IDLE,
        CHARGE_WINDUP,
        CHARGING,
        PROJECTILES,
        CORRUPTION,
        RECOVERING
    }
}
