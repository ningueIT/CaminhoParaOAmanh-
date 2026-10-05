package entities;

import engine.Animation;
import engine.AssetManager;
import input.InputManager;
import physics.AABB;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public final class Player extends Entity {
    private static final int DEFAULT_MAX_HEALTH = 3;
    private static final int DEFAULT_MAX_MANA = 100;
    private static final int MAGIC_COST = 25;
    private static final int MAX_POTIONS_PER_TYPE = 5;
    private static final int HEALTH_POTION_RESTORE = 2;
    private static final int MANA_POTION_RESTORE = 50;
    private static final double MOVE_SPEED = 260.0;
    private static final double DASH_SPEED = 840.0;
    private static final double DASH_DURATION = 0.25;
    private static final double DASH_COOLDOWN = 0.55;
    private static final double ATTACK_DURATION = 0.20;
    private static final double ATTACK_COOLDOWN = 0.30;
    private static final double MAGIC_COOLDOWN = 0.35;
    private static final double HORIZONTAL_ATTACK_WIDTH = 42.0;
    private static final double HORIZONTAL_ATTACK_HEIGHT = 26.0;
    private static final double VERTICAL_ATTACK_WIDTH = 26.0;
    private static final double VERTICAL_ATTACK_HEIGHT = 42.0;
    private static final double GROUND_ACCELERATION = 2000.0;
    private static final double AIR_ACCELERATION = 1200.0;
    private static final double DRAG = 2400.0;
    private static final double GRAVITY = 1400.0;
    private static final double JUMP_SPEED = 620.0;
    private static final double ENEMY_BOUNCE_SPEED = 560.0;
    private static final double KNOCKBACK_SPEED = 360.0;
    private static final double KNOCKBACK_LIFT_SPEED = 260.0;
    private static final double RUNNING_THRESHOLD = 8.0;
    private static final double INVULNERABILITY_DURATION = 1.5;
    private static final double BLINK_FREQUENCY = 12.0;
    private static final int MAX_AIR_JUMPS = 1;
    private static final double COYOTE_TIME_SECONDS = 0.12;
    private static final double JUMP_BUFFER_SECONDS = 0.12;
    private static final double DASH_AFTERIMAGE_INTERVAL_SECONDS = 0.045;
    private static final double DASH_AFTERIMAGE_LIFETIME_SECONDS = 0.20;
    private static final int MAX_DASH_AFTERIMAGES = 4;
    private static final String PLAYER_SPRITE_DIRECTORY = "sprites/player/";

    private final int maxHealth;
    private final int maxMana;
    private final Map<PlayerState, Animation> animations;
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
    private double coyoteTimer;
    private double jumpBufferTimer;
    private double dashAfterimageTimer;
    private int facingDirection = 1;
    private int dashDirection = 1;
    private AttackDirection aimDirection = AttackDirection.RIGHT;
    private AttackDirection attackDirection = AttackDirection.RIGHT;
    private int availableAirJumps = MAX_AIR_JUMPS;
    private int healthPotionCount;
    private int manaPotionCount;
    private int collectibleCount;
    private final Deque<DashAfterimage> dashAfterimages = new ArrayDeque<>();
    private boolean wasAttackPressed;
    private boolean wasDashPressed;
    private boolean wasMagicPressed;
    private boolean wasJumpPressed;
    private boolean wasHealthPotionPressed;
    private boolean wasManaPotionPressed;
    private boolean jumpSoundRequested;
    private boolean damageSoundRequested;
    private MagicProjectile pendingMagicProjectile;

    public Player(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.maxHealth = DEFAULT_MAX_HEALTH;
        this.currentHealth = maxHealth;
        this.maxMana = DEFAULT_MAX_MANA;
        this.currentMana = maxMana;
        this.animations = createAnimations(width, height);
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
        updateJumpGraceTimers(inputManager, deltaSeconds);
        handlePotionTriggers(inputManager);
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
            updateVerticalMovement(deltaSeconds);
        }

        integrate(deltaSeconds);
        updateDashAfterimages(deltaSeconds);

        if (attackEndedThisFrame) {
            finishAttack();
        }

        if (dashEndedThisFrame) {
            finishDash();
        }

        wasAttackPressed = inputManager.isAttackPressed();
        wasDashPressed = inputManager.isDodging();
        wasMagicPressed = inputManager.isMagicPressed();
        wasJumpPressed = inputManager.isJumpPressed();
        wasHealthPotionPressed = inputManager.isUseHealthPotionPressed();
        wasManaPotionPressed = inputManager.isUseManaPotionPressed();
        getCurrentAnimation().update();
    }

    public void refreshState() {
        if (state == PlayerState.DODGING || state == PlayerState.ATTACKING) {
            return;
        }

        if (!isOnGround()) {
            setState(getVelocityY() < 0.0 ? PlayerState.JUMPING : PlayerState.FALLING);
            return;
        }

        setState(Math.abs(getVelocityX()) > RUNNING_THRESHOLD ? PlayerState.RUNNING : PlayerState.IDLE);
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

    public int restoreHealth(int amount) {
        if (amount <= 0 || currentHealth == maxHealth || isDead()) {
            return 0;
        }

        int restoredAmount = Math.min(amount, maxHealth - currentHealth);
        currentHealth += restoredAmount;
        return restoredAmount;
    }

    public boolean addHealthPotion() {
        if (healthPotionCount == MAX_POTIONS_PER_TYPE) {
            return false;
        }

        healthPotionCount++;
        return true;
    }

    public boolean addManaPotion() {
        if (manaPotionCount == MAX_POTIONS_PER_TYPE) {
            return false;
        }

        manaPotionCount++;
        return true;
    }

    public boolean useHealthPotion() {
        if (healthPotionCount == 0 || restoreHealth(HEALTH_POTION_RESTORE) == 0) {
            return false;
        }

        healthPotionCount--;
        return true;
    }

    public boolean useManaPotion() {
        if (manaPotionCount == 0 || restoreMana(MANA_POTION_RESTORE) == 0) {
            return false;
        }

        manaPotionCount--;
        return true;
    }

    public void addCollectible() {
        collectibleCount++;
    }

    public int getHealthPotionCount() {
        return healthPotionCount;
    }

    public int getManaPotionCount() {
        return manaPotionCount;
    }

    public int getCollectibleCount() {
        return collectibleCount;
    }

    public int getAvailableAirJumps() {
        return availableAirJumps;
    }

    public double getCoyoteTimeRemaining() {
        return coyoteTimer;
    }

    public double getJumpBufferTimeRemaining() {
        return jumpBufferTimer;
    }

    public int getDashAfterimageCount() {
        return dashAfterimages.size();
    }

    public void resetJourneyInventory() {
        healthPotionCount = 0;
        manaPotionCount = 0;
        collectibleCount = 0;
    }

    public void updateAttackAim(double worldX, double worldY) {
        double playerCenterX = getX() + getWidth() * 0.5;
        double playerCenterY = getY() + getHeight() * 0.5;
        double distanceX = worldX - playerCenterX;
        double distanceY = worldY - playerCenterY;
        if (Math.abs(distanceX) >= Math.abs(distanceY)) {
            aimDirection = distanceX >= 0.0 ? AttackDirection.RIGHT : AttackDirection.LEFT;
            facingDirection = aimDirection == AttackDirection.RIGHT ? 1 : -1;
            return;
        }

        aimDirection = distanceY >= 0.0 ? AttackDirection.DOWN : AttackDirection.UP;
    }

    public AttackDirection getAttackDirection() {
        return attackDirection;
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
        return invulnerabilityTimer > 0.0 || state == PlayerState.DODGING;
    }

    public boolean isDead() {
        return currentHealth == 0;
    }

    public boolean isDodging() {
        return state == PlayerState.DODGING;
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
        if (amount <= 0 || currentHealth <= 0 || isInvulnerable()) {
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
        resetAnimations();
        state = PlayerState.IDLE;
        wasAttackPressed = false;
        wasDashPressed = false;
        wasMagicPressed = false;
        wasJumpPressed = false;
        wasHealthPotionPressed = false;
        wasManaPotionPressed = false;
        availableAirJumps = MAX_AIR_JUMPS;
        coyoteTimer = 0.0;
        jumpBufferTimer = 0.0;
        dashAfterimageTimer = 0.0;
        dashAfterimages.clear();
        jumpSoundRequested = false;
        damageSoundRequested = false;
        pendingMagicProjectile = null;
    }

    public void respawn(double startX, double startY) {
        spawnX = startX;
        spawnY = startY;
        respawn();
    }

    public void bounceFromEnemy() {
        if (isDead()) {
            return;
        }

        attackTimer = 0.0;
        dashTimer = 0.0;
        setVelocityY(-ENEMY_BOUNCE_SPEED);
        setOnGround(false);
        availableAirJumps = MAX_AIR_JUMPS;
        coyoteTimer = 0.0;
        jumpBufferTimer = 0.0;
        setState(PlayerState.JUMPING);
    }

    public void applyKnockbackFrom(double sourceX) {
        if (isDead()) {
            return;
        }

        double playerCenterX = getX() + getWidth() * 0.5;
        int direction = playerCenterX >= sourceX ? 1 : -1;
        setVelocityX(direction * KNOCKBACK_SPEED);
        setVelocityY(-KNOCKBACK_LIFT_SPEED);
        setOnGround(false);
        if (state != PlayerState.DODGING) {
            setState(PlayerState.FALLING);
        }
    }

    public void fixedUpdateCinematic(double deltaSeconds, double horizontalVelocity) {
        if (isDead()) {
            return;
        }
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative.");
        }

        beginFixedUpdate();
        updateInvulnerability(deltaSeconds);
        setVelocityX(horizontalVelocity);
        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
        integrate(deltaSeconds);
        setState(Math.abs(horizontalVelocity) > RUNNING_THRESHOLD ? PlayerState.RUNNING : PlayerState.FALLING);
        getCurrentAnimation().update();
    }

    @Override
    public void render(Graphics2D g2d, double alpha) {
        drawDashAfterimages(g2d);
        if (isAttacking()) {
            drawAttackHitbox(g2d, createAttackHitbox(getRenderX(alpha), getRenderY(alpha)));
        }

        if (shouldSkipRender()) {
            return;
        }

        BufferedImage currentFrame = getCurrentAnimation().getCurrentFrame();
        int renderX = (int) Math.round(getRenderX(alpha));
        int renderY = (int) Math.round(getRenderY(alpha));
        int renderWidth = (int) Math.round(getWidth());
        int renderHeight = (int) Math.round(getHeight());
        g2d.drawImage(currentFrame, renderX, renderY, renderWidth, renderHeight, null);
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

    private void updateJumpGraceTimers(InputManager inputManager, double deltaSeconds) {
        if (isOnGround()) {
            coyoteTimer = COYOTE_TIME_SECONDS;
            availableAirJumps = MAX_AIR_JUMPS;
        } else {
            coyoteTimer = Math.max(0.0, coyoteTimer - deltaSeconds);
        }

        if (inputManager.isJumpPressed() && !wasJumpPressed) {
            jumpBufferTimer = JUMP_BUFFER_SECONDS;
        } else {
            jumpBufferTimer = Math.max(0.0, jumpBufferTimer - deltaSeconds);
        }
    }

    private void handleAttackTrigger(InputManager inputManager) {
        boolean attackPressed = inputManager.isAttackPressed();
        if (!attackPressed || wasAttackPressed || state == PlayerState.DODGING
                || state == PlayerState.ATTACKING || attackCooldownTimer > 0.0) {
            return;
        }

        startAttack();
    }

    private void handlePotionTriggers(InputManager inputManager) {
        boolean healthPotionPressed = inputManager.isUseHealthPotionPressed();
        if (healthPotionPressed && !wasHealthPotionPressed) {
            useHealthPotion();
        }

        boolean manaPotionPressed = inputManager.isUseManaPotionPressed();
        if (manaPotionPressed && !wasManaPotionPressed) {
            useManaPotion();
        }
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
        attackDirection = aimDirection;
        setState(PlayerState.ATTACKING);
        setVelocityX(0.0);
    }

    private void startDash(InputManager inputManager) {
        dashDirection = resolveDashDirection(inputManager);
        facingDirection = dashDirection;
        dashTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        dashAfterimageTimer = 0.0;
        setState(PlayerState.DODGING);

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
        setState(isOnGround() ? PlayerState.IDLE : PlayerState.FALLING);
    }

    private void finishDash() {
        setVelocityX(0.0);
        setState(isOnGround() ? PlayerState.IDLE : PlayerState.FALLING);
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
            aimDirection = direction > 0 ? AttackDirection.RIGHT : AttackDirection.LEFT;
        }

        double targetVelocityX = direction * MOVE_SPEED;
        double acceleration = direction == 0 ? DRAG : (isOnGround() ? GROUND_ACCELERATION : AIR_ACCELERATION);

        setVelocityX(moveTowards(getVelocityX(), targetVelocityX, acceleration * deltaSeconds));
    }

    private void updateVerticalMovement(double deltaSeconds) {
        tryConsumeBufferedJump();
        setVelocityY(getVelocityY() + GRAVITY * deltaSeconds);
    }

    private void tryConsumeBufferedJump() {
        if (jumpBufferTimer == 0.0) {
            return;
        }

        if (isOnGround() || coyoteTimer > 0.0) {
            availableAirJumps = MAX_AIR_JUMPS;
            performJump();
            return;
        }

        if (availableAirJumps <= 0) {
            return;
        }

        availableAirJumps--;
        performJump();
    }

    private void performJump() {
        setVelocityY(-JUMP_SPEED);
        setOnGround(false);
        coyoteTimer = 0.0;
        jumpBufferTimer = 0.0;
        jumpSoundRequested = true;
    }

    private void updateDashAfterimages(double deltaSeconds) {
        Iterator<DashAfterimage> iterator = dashAfterimages.iterator();
        while (iterator.hasNext()) {
            DashAfterimage afterimage = iterator.next();
            afterimage.remainingSeconds = Math.max(0.0, afterimage.remainingSeconds - deltaSeconds);
            if (afterimage.remainingSeconds == 0.0) {
                iterator.remove();
            }
        }

        if (state != PlayerState.DODGING) {
            return;
        }

        dashAfterimageTimer = Math.max(0.0, dashAfterimageTimer - deltaSeconds);
        if (dashAfterimageTimer > 0.0) {
            return;
        }

        if (dashAfterimages.size() == MAX_DASH_AFTERIMAGES) {
            dashAfterimages.removeFirst();
        }
        dashAfterimages.addLast(new DashAfterimage(
                getX(),
                getY(),
                getCurrentAnimation().getCurrentFrame(),
                DASH_AFTERIMAGE_LIFETIME_SECONDS
        ));
        dashAfterimageTimer = DASH_AFTERIMAGE_INTERVAL_SECONDS;
    }

    private void drawDashAfterimages(Graphics2D g2d) {
        if (dashAfterimages.isEmpty()) {
            return;
        }

        Composite previousComposite = g2d.getComposite();
        try {
            for (DashAfterimage afterimage : dashAfterimages) {
                float opacity = (float) (0.45 * (afterimage.remainingSeconds / DASH_AFTERIMAGE_LIFETIME_SECONDS));
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g2d.drawImage(
                        afterimage.frame,
                        (int) Math.round(afterimage.x),
                        (int) Math.round(afterimage.y),
                        (int) Math.round(getWidth()),
                        (int) Math.round(getHeight()),
                        null
                );
            }
        } finally {
            g2d.setComposite(previousComposite);
        }
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

    private Map<PlayerState, Animation> createAnimations(double width, double height) {
        int spriteWidth = Math.max(1, (int) Math.round(width));
        int spriteHeight = Math.max(1, (int) Math.round(height));
        Map<PlayerState, Animation> createdAnimations = new EnumMap<>(PlayerState.class);

        createdAnimations.put(
                PlayerState.IDLE,
                createAnimation("idle", spriteWidth, spriteHeight, 24, new Color(241, 162, 205), new Color(248, 178, 215))
        );
        createdAnimations.put(
                PlayerState.RUNNING,
                createAnimation("running", spriteWidth, spriteHeight, 6, new Color(236, 119, 177), new Color(255, 153, 198))
        );
        createdAnimations.put(
                PlayerState.JUMPING,
                createAnimation("jumping", spriteWidth, spriteHeight, 12, new Color(166, 209, 247), new Color(195, 225, 255))
        );
        createdAnimations.put(
                PlayerState.FALLING,
                createAnimation("falling", spriteWidth, spriteHeight, 12, new Color(131, 165, 224), new Color(163, 193, 245))
        );
        createdAnimations.put(
                PlayerState.DODGING,
                createAnimation("dodging", spriteWidth, spriteHeight, 4, new Color(245, 213, 131), new Color(255, 234, 166))
        );
        createdAnimations.put(
                PlayerState.ATTACKING,
                createAnimation("attacking", spriteWidth, spriteHeight, 4, new Color(249, 119, 138), new Color(255, 158, 166))
        );

        return createdAnimations;
    }

    private Animation createAnimation(
            String animationId,
            int spriteWidth,
            int spriteHeight,
            int frameDelay,
            Color firstFrameColor,
            Color secondFrameColor
    ) {
        BufferedImage[] frames = {
                AssetManager.loadOrPlaceholder(
                        PLAYER_SPRITE_DIRECTORY + animationId + "_0.png",
                        "player-" + animationId + "-0",
                        spriteWidth,
                        spriteHeight,
                        firstFrameColor
                ),
                AssetManager.loadOrPlaceholder(
                        PLAYER_SPRITE_DIRECTORY + animationId + "_1.png",
                        "player-" + animationId + "-1",
                        spriteWidth,
                        spriteHeight,
                        secondFrameColor
                )
        };
        return new Animation(frames, frameDelay);
    }

    private Animation getCurrentAnimation() {
        return animations.get(state);
    }

    private void setState(PlayerState newState) {
        if (state == newState) {
            return;
        }

        state = newState;
        getCurrentAnimation().reset();
    }

    private void resetAnimations() {
        for (Animation animation : animations.values()) {
            animation.reset();
        }
    }

    private AABB createAttackHitbox(double baseX, double baseY) {
        return switch (attackDirection) {
            case LEFT -> new AABB(
                    baseX - HORIZONTAL_ATTACK_WIDTH,
                    baseY + ((getHeight() - HORIZONTAL_ATTACK_HEIGHT) * 0.5),
                    HORIZONTAL_ATTACK_WIDTH,
                    HORIZONTAL_ATTACK_HEIGHT
            );
            case RIGHT -> new AABB(
                    baseX + getWidth(),
                    baseY + ((getHeight() - HORIZONTAL_ATTACK_HEIGHT) * 0.5),
                    HORIZONTAL_ATTACK_WIDTH,
                    HORIZONTAL_ATTACK_HEIGHT
            );
            case UP -> new AABB(
                    baseX + ((getWidth() - VERTICAL_ATTACK_WIDTH) * 0.5),
                    baseY - VERTICAL_ATTACK_HEIGHT,
                    VERTICAL_ATTACK_WIDTH,
                    VERTICAL_ATTACK_HEIGHT
            );
            case DOWN -> new AABB(
                    baseX + ((getWidth() - VERTICAL_ATTACK_WIDTH) * 0.5),
                    baseY + getHeight(),
                    VERTICAL_ATTACK_WIDTH,
                    VERTICAL_ATTACK_HEIGHT
            );
        };
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

    private static final class DashAfterimage {
        private final double x;
        private final double y;
        private final BufferedImage frame;
        private double remainingSeconds;

        private DashAfterimage(double x, double y, BufferedImage frame, double remainingSeconds) {
            this.x = x;
            this.y = y;
            this.frame = frame;
            this.remainingSeconds = remainingSeconds;
        }
    }
}
