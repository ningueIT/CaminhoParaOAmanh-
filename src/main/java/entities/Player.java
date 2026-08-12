package entities;

import input.InputManager;
import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class Player extends Entity {
    private static final int DEFAULT_MAX_HEALTH = 3;
    private static final int DEFAULT_MAX_MANA = 100;
    private static final int MAGIC_COST = 25;
    private static final double MOVE_SPEED = 260.0;
    private static final double DASH_SPEED = 840.0;
    private static final double DASH_DURATION = 0.25;
    private static final double DASH_COOLDOWN = 0.55;
    private static final double ATTACK_DURATION = 0.20;
    private static final double ATTACK_COOLDOWN = 0.30;
    private static final double MAGIC_COOLDOWN = 0.35;
    private static final double ATTACK_WIDTH = 42.0;
    private static final double ATTACK_HEIGHT = 26.0;
    private static final double GROUND_ACCELERATION = 2000.0;
    private static final double AIR_ACCELERATION = 1200.0;
    private static final double DRAG = 2400.0;
    private static final double GRAVITY = 1400.0;
    private static final double JUMP_SPEED = 620.0;
    private static final double RUNNING_THRESHOLD = 8.0;
    private static final double INVULNERABILITY_DURATION = 1.5;
    private static final double BLINK_FREQUENCY = 12.0;

    private final int maxHealth;
    private final int maxMana;
    private PlayerState state = PlayerState.IDLE;
    private int currentHealth;
    private int currentMana;
    private double spawnX;
    private double spawnY;
    private double attackTimer;
    private double attackCooldownTimer;
    private double magicCooldownTimer;
    private double dashTimer;
    private double dashCooldownTimer;
    private double invulnerabilityTimer;
    private int facingDirection = 1;
    private int dashDirection = 1;
    private boolean wasAttackPressed;
    private boolean wasDashPressed;
    private boolean wasMagicPressed;
    private boolean jumpSoundRequested;
    private boolean damageSoundRequested;
    private MagicProjectile pendingMagicProjectile;

    public Player(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.maxHealth = DEFAULT_MAX_HEALTH;
        this.currentHealth = maxHealth;
        this.maxMana = DEFAULT_MAX_MANA;
        this.currentMana = maxMana;
        this.spawnX = x;
        this.spawnY = y;
        setOnGround(true);
    }

    public void fixedUpdate(InputManager inputManager, double deltaSeconds) {
        if (isDead()) {
            return;
        }

        beginFixedUpdate();
        updateInvulnerability(deltaSeconds);
        updateAttackCooldown(deltaSeconds);
        updateMagicCooldown(deltaSeconds);
        updateDashCooldown(deltaSeconds);
        handleAttackTrigger(inputManager);
        handleMagicTrigger(inputManager);
        handleDashTrigger(inputManager);

        boolean attackEndedThisFrame = false;
        boolean dashEndedThisFrame = false;
        if (state == PlayerState.DODGING) {
            dashEndedThisFrame = updateDash(deltaSeconds);
        } else if (state == PlayerState.ATTACKING) {
            attackEndedThisFrame = updateAttack(deltaSeconds);
        } else {
            updateHorizontalMovement(inputManager, deltaSeconds);
            updateVerticalMovement(inputManager, deltaSeconds);
        }

        integrate(deltaSeconds);

        if (attackEndedThisFrame) {
            finishAttack();
        }

        if (dashEndedThisFrame) {
            finishDash();
        }

        wasAttackPressed = inputManager.isAttackPressed();
        wasDashPressed = inputManager.isDodging();
        wasMagicPressed = inputManager.isMagicPressed();
    }

    public void refreshState() {
        if (state == PlayerState.DODGING || state == PlayerState.ATTACKING) {
            return;
        }

        if (!isOnGround()) {
            state = getVelocityY() < 0.0 ? PlayerState.JUMPING : PlayerState.FALLING;
            return;
        }

        state = Math.abs(getVelocityX()) > RUNNING_THRESHOLD ? PlayerState.RUNNING : PlayerState.IDLE;
    }

    public PlayerState getState() {
        return state;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int restoreMana(int amount) {
        if (amount <= 0 || currentMana == maxMana) {
            return 0;
        }

        int restoredAmount = Math.min(amount, maxMana - currentMana);
        currentMana += restoredAmount;
        return restoredAmount;
    }

    public MagicProjectile consumePendingMagicProjectile() {
        MagicProjectile projectile = pendingMagicProjectile;
        pendingMagicProjectile = null;
        return projectile;
    }

    public boolean isAttacking() {
        return state == PlayerState.ATTACKING;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0.0;
    }

    public boolean isDead() {
        return currentHealth == 0;
    }

    public boolean consumeJumpSoundRequest() {
        boolean requested = jumpSoundRequested;
        jumpSoundRequested = false;
        return requested;
    }

    public boolean consumeDamageSoundRequest() {
        boolean requested = damageSoundRequested;
        damageSoundRequested = false;
        return requested;
    }

    public AABB getAttackHitbox() {
        if (!isAttacking()) {
            return new AABB(getX(), getY(), 0.0, 0.0);
        }

        return createAttackHitbox(getX(), getY());
    }

    public void takeDamage(int amount) {
        // I-frames impedem dano em cascata enquanto o temporizador estiver ativo.
        if (amount <= 0 || currentHealth <= 0 || invulnerabilityTimer > 0.0) {
            return;
        }

        currentHealth = Math.max(0, currentHealth - amount);
        damageSoundRequested = true;
        if (currentHealth == 0) {
            setVelocityX(0.0);
            setVelocityY(0.0);
            pendingMagicProjectile = null;
            return;
        }

        invulnerabilityTimer = INVULNERABILITY_DURATION;
    }

    public void respawn() {
        // Restaura o jogador ao ponto de renascimento com vida cheia e sem inercia acumulada.
        currentHealth = maxHealth;
        currentMana = maxMana;
        setPosition(spawnX, spawnY);
        beginFixedUpdate();

        setVelocityX(0.0);
        setVelocityY(0.0);
        setOnGround(false);

        dashTimer = 0.0;
        attackTimer = 0.0;
        attackCooldownTimer = 0.0;
        magicCooldownTimer = 0.0;
        dashCooldownTimer = 0.0;
        invulnerabilityTimer = INVULNERABILITY_DURATION;
        state = PlayerState.IDLE;
        wasAttackPressed = false;
        wasDashPressed = false;
        wasMagicPressed = false;
        jumpSoundRequested = false;
        damageSoundRequested = false;
        pendingMagicProjectile = null;
    }

    public void respawn(double startX, double startY) {
        spawnX = startX;
        spawnY = startY;
        respawn();
    }

    @Override
    public void render(Graphics2D g2d, double alpha) {
        if (isAttacking()) {
            drawAttackHitbox(g2d, createAttackHitbox(getRenderX(alpha), getRenderY(alpha)));
        }

        if (shouldSkipRender()) {
            return;
        }

        int renderX = (int) Math.round(getRenderX(alpha));
        int renderY = (int) Math.round(getRenderY(alpha));
        int renderWidth = (int) Math.round(getWidth());
        int renderHeight = (int) Math.round(getHeight());

        g2d.setColor(new Color(241, 162, 205));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 16, 16);

        g2d.setColor(new Color(62, 49, 80));
        g2d.fillRect(renderX + 8, renderY + 14, 8, 8);
        g2d.fillRect(renderX + renderWidth - 16, renderY + 14, 8, 8);
    }

    private void updateInvulnerability(double deltaSeconds) {
        // O timer e atualizado no fixed step para manter a duracao consistente em qualquer FPS.
        invulnerabilityTimer = Math.max(0.0, invulnerabilityTimer - deltaSeconds);
    }

    private void updateAttackCooldown(double deltaSeconds) {
        attackCooldownTimer = Math.max(0.0, attackCooldownTimer - deltaSeconds);
    }

    private void updateMagicCooldown(double deltaSeconds) {
        magicCooldownTimer = Math.max(0.0, magicCooldownTimer - deltaSeconds);
    }

    private void updateDashCooldown(double deltaSeconds) {
        dashCooldownTimer = Math.max(0.0, dashCooldownTimer - deltaSeconds);
    }

    private void handleAttackTrigger(InputManager inputManager) {
        boolean attackPressed = inputManager.isAttackPressed();
        if (!attackPressed || wasAttackPressed || state == PlayerState.DODGING
                || state == PlayerState.ATTACKING || attackCooldownTimer > 0.0) {
            return;
        }

        startAttack();
    }

    private void handleDashTrigger(InputManager inputManager) {
        boolean dashPressed = inputManager.isDodging();
        if (!dashPressed || wasDashPressed || state == PlayerState.DODGING
                || state == PlayerState.ATTACKING || dashCooldownTimer > 0.0) {
            return;
        }

        startDash(inputManager);
    }

    private void handleMagicTrigger(InputManager inputManager) {
        boolean magicPressed = inputManager.isMagicPressed();
        if (!magicPressed || wasMagicPressed || magicCooldownTimer > 0.0 || currentMana < MAGIC_COST) {
            return;
        }

        currentMana -= MAGIC_COST;
        magicCooldownTimer = MAGIC_COOLDOWN;
        pendingMagicProjectile = createMagicProjectile();
    }

    private void startAttack() {
        attackTimer = ATTACK_DURATION;
        attackCooldownTimer = ATTACK_COOLDOWN;
        state = PlayerState.ATTACKING;
        setVelocityX(0.0);
    }

    private void startDash(InputManager inputManager) {
        dashDirection = resolveDashDirection(inputManager);
        facingDirection = dashDirection;
        dashTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        state = PlayerState.DODGING;

        setVelocityX(dashDirection * DASH_SPEED);
        setVelocityY(0.0);
    }

    private int resolveDashDirection(InputManager inputManager) {
        if (inputManager.isMovingLeft() && !inputManager.isMovingRight()) {
            return -1;
        }

        if (inputManager.isMovingRight() && !inputManager.isMovingLeft()) {
            return 1;
        }

        return facingDirection;
    }

    private boolean updateAttack(double deltaSeconds) {
        // O ataque trava apenas o deslocamento horizontal; a fisica vertical continua ativa.
        setVelocityX(0.0);
        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
        attackTimer = Math.max(0.0, attackTimer - deltaSeconds);
        return attackTimer == 0.0;
    }

    private boolean updateDash(double deltaSeconds) {
        // Durante o dash, congelamos o eixo Y e mantemos um impulso horizontal constante.
        setVelocityX(dashDirection * DASH_SPEED);
        setVelocityY(0.0);
        dashTimer = Math.max(0.0, dashTimer - deltaSeconds);
        return dashTimer == 0.0;
    }

    private void finishAttack() {
        state = isOnGround() ? PlayerState.IDLE : PlayerState.FALLING;
    }

    private void finishDash() {
        setVelocityX(0.0);
        state = isOnGround() ? PlayerState.IDLE : PlayerState.FALLING;
    }

    private void updateHorizontalMovement(InputManager inputManager, double deltaSeconds) {
        int direction = 0;
        if (inputManager.isMovingLeft()) {
            direction--;
        }
        if (inputManager.isMovingRight()) {
            direction++;
        }

        if (direction != 0) {
            facingDirection = direction;
        }

        double targetVelocityX = direction * MOVE_SPEED;
        double acceleration = direction == 0 ? DRAG : (isOnGround() ? GROUND_ACCELERATION : AIR_ACCELERATION);

        setVelocityX(moveTowards(getVelocityX(), targetVelocityX, acceleration * deltaSeconds));
    }

    private void updateVerticalMovement(InputManager inputManager, double deltaSeconds) {
        if (inputManager.isJumpPressed() && isOnGround()) {
            setVelocityY(-JUMP_SPEED);
            setOnGround(false);
            jumpSoundRequested = true;
        }

        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
    }

    private double moveTowards(double current, double target, double maxDelta) {
        if (current < target) {
            return Math.min(current + maxDelta, target);
        }

        return Math.max(current - maxDelta, target);
    }

    private boolean shouldSkipRender() {
        return isInvulnerable()
                && ((int) Math.floor(invulnerabilityTimer * BLINK_FREQUENCY)) % 2 == 0;
    }

    private AABB createAttackHitbox(double baseX, double baseY) {
        double attackX = facingDirection > 0 ? baseX + getWidth() : baseX - ATTACK_WIDTH;
        double attackY = baseY + ((getHeight() - ATTACK_HEIGHT) * 0.5);
        return new AABB(attackX, attackY, ATTACK_WIDTH, ATTACK_HEIGHT);
    }

    private MagicProjectile createMagicProjectile() {
        double projectileX = facingDirection > 0 ? getX() + getWidth() : getX() - MagicProjectile.WIDTH;
        double projectileY = getY() + ((getHeight() - MagicProjectile.HEIGHT) * 0.5);
        return new MagicProjectile(projectileX, projectileY, facingDirection, 1);
    }

    private void drawAttackHitbox(Graphics2D g2d, AABB attackHitbox) {
        int renderX = (int) Math.round(attackHitbox.getLeft());
        int renderY = (int) Math.round(attackHitbox.getTop());
        int renderWidth = (int) Math.round(attackHitbox.getRight() - attackHitbox.getLeft());
        int renderHeight = (int) Math.round(attackHitbox.getBottom() - attackHitbox.getTop());

        g2d.setColor(new Color(255, 241, 143));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 8, 8);

        g2d.setColor(new Color(255, 255, 255));
        g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 8, 8);
    }
}
