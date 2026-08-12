package entities;

import physics.PhysicsWorld;

public abstract class Enemy extends Entity {
    private static final double DAMAGE_IFRAME_DURATION = 0.25;

    private final int maxHealth;
    private int currentHealth;
    private double damageInvulnerabilityTimer;

    protected Enemy(double x, double y, double width, double height, int maxHealth) {
        super(x, y, width, height);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public final void fixedUpdate(double deltaSeconds, PhysicsWorld physicsWorld, Player player) {
        if (isDead()) {
            return;
        }

        beginFixedUpdate();
        updateDamageInvulnerability(deltaSeconds);
        updateBehavior(deltaSeconds, physicsWorld, player);
        integrate(deltaSeconds);
    }

    protected abstract void updateBehavior(double deltaSeconds, PhysicsWorld physicsWorld, Player player);

    public boolean usesWorldPhysics() {
        return true;
    }

    public void afterPhysicsResolve(PhysicsWorld physicsWorld) {
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || isDead() || damageInvulnerabilityTimer > 0.0) {
            return;
        }

        currentHealth = Math.max(0, currentHealth - amount);
        damageInvulnerabilityTimer = DAMAGE_IFRAME_DURATION;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    protected boolean isRecentlyHit() {
        return damageInvulnerabilityTimer > 0.0;
    }

    private void updateDamageInvulnerability(double deltaSeconds) {
        damageInvulnerabilityTimer = Math.max(0.0, damageInvulnerabilityTimer - deltaSeconds);
    }
}
