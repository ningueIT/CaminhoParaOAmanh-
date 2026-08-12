package engine;

import entities.Enemy;
import entities.DialogInteractable;
import entities.Gate;
import entities.Interactable;
import entities.LevelExit;
import entities.Lever;
import entities.MagicProjectile;
import entities.ManaPickup;
import entities.MysteriousKnight;
import entities.Platform;
import entities.Player;
import entities.Signpost;
import entities.Spike;
import input.InputManager;
import level.Level;
import level.LevelParser;
import physics.AABB;
import physics.PhysicsWorld;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GamePanel extends JPanel {
    public static final int PANEL_WIDTH = 1280;
    public static final int PANEL_HEIGHT = 720;
    private static final int TILE_SIZE = 64;
    private static final double PLAYER_WIDTH_RATIO = 0.75;
    private static final double PLAYER_HEIGHT_RATIO = 1.0;
    private static final double INTERACTION_RANGE = 24.0;
    private static final String[] LEVEL_1 = {
            "................................",
            "................................",
            "................................",
            "................................",
            "..........####..................",
            "................................",
            "..................####..........",
            "................................",
            "......###.......................",
            "................................",
            "......................###.......",
            "................................",
            "......................V.........",
            ".............####...............",
            "................................",
            "..P..L.MS...E....^^..G...X.....",
            "################################"
    };
    private static final String[] LEVEL_2 = {
            "................................",
            "................................",
            "................................",
            "................................",
            ".......####.....................",
            "................................",
            "................####............",
            "................................",
            "............###.................",
            "................................",
            "......................####......",
            "................................",
            ".................###............",
            "................................",
            "..P...N.^^....E....B....^^...X..",
            "################################"
    };
    private static final List<String[]> ALL_LEVELS = List.of(LEVEL_1, LEVEL_2);

    private final Object worldLock = new Object();
    private final GameLoop gameLoop;
    private final InputManager inputManager;
    private final HUD hud;
    private final Player player;
    private final DialogManager dialogManager = new DialogManager();
    private final AudioManager audioManager = new AudioManager();

    private Camera camera;
    private List<Enemy> enemies = List.of();
    private List<Gate> gates = List.of();
    private List<Interactable> interactables = List.of();
    private List<Lever> levers = List.of();
    private List<LevelExit> levelExits = List.of();
    private List<ManaPickup> manaPickups = List.of();
    private List<Platform> platforms = List.of();
    private List<Spike> spikes = List.of();
    private final List<MagicProjectile> magicProjectiles = new ArrayList<>();
    private PhysicsWorld physicsWorld;
    private List<Signpost> signposts = List.of();
    private List<MysteriousKnight> mysteriousKnights = List.of();
    private int worldWidth;
    private int worldHeight;
    private int currentLevelIndex;
    private volatile GameState gameState = GameState.MAIN_MENU;

    private volatile double interpolationAlpha;
    private boolean wasInteractPressed;
    private boolean wasConfirmPressed;

    public GamePanel(InputManager inputManager) {
        this.inputManager = inputManager;
        this.player = new Player(
                0.0,
                0.0,
                TILE_SIZE * PLAYER_WIDTH_RATIO,
                TILE_SIZE * PLAYER_HEIGHT_RATIO
        );
        this.hud = new HUD(player);
        loadLevel(0);
        this.gameLoop = new GameLoop(this);

        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setDoubleBuffered(true);
        setFocusable(true);
        setBackground(new Color(32, 37, 58));
        addKeyListener(inputManager);
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }

    public void fixedUpdate(double deltaSeconds) {
        synchronized (worldLock) {
            boolean confirmPressed = inputManager.isConfirmPressed();
            boolean confirmJustPressed = confirmPressed && !wasConfirmPressed;
            wasConfirmPressed = confirmPressed;

            switch (gameState) {
                case MAIN_MENU -> updateMainMenu(confirmJustPressed);
                case PLAYING -> updateGameplay(deltaSeconds);
                case DIALOGUE -> updateDialog(deltaSeconds);
                case GAME_OVER -> updateGameOver(confirmJustPressed);
                case ENDING -> updateEnding(confirmJustPressed);
            }
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void requestRender(double interpolationAlpha) {
        this.interpolationAlpha = interpolationAlpha;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            renderScene(g2d);
        } finally {
            g2d.dispose();
        }
    }

    private void renderScene(Graphics2D g2d) {
        drawBackground(g2d);
        if (gameState == GameState.MAIN_MENU) {
            drawMainMenu(g2d);
            return;
        }

        renderWorld(g2d);
        synchronized (worldLock) {
            hud.render(g2d);
            switch (gameState) {
                case DIALOGUE -> dialogManager.render(g2d, PANEL_WIDTH, PANEL_HEIGHT);
                case GAME_OVER -> drawGameOver(g2d);
                case ENDING -> drawEnding(g2d);
                default -> {
                }
            }
        }
    }

    private void renderWorld(Graphics2D g2d) {
        Graphics2D worldGraphics = (Graphics2D) g2d.create();
        try {
            synchronized (worldLock) {
                // O fundo fica fixo na tela; apenas o mundo recebe deslocamento da camera.
                worldGraphics.translate(-camera.getX(), -camera.getY());
                drawGround(worldGraphics);
                drawPlatforms(worldGraphics);
                drawLevelExits(worldGraphics);
                drawGates(worldGraphics);
                drawSpikes(worldGraphics);
                drawManaPickups(worldGraphics);
                drawEnemies(worldGraphics);
                drawMagicProjectiles(worldGraphics);
                drawInteractables(worldGraphics);
                player.render(worldGraphics, interpolationAlpha);
            }
        } finally {
            worldGraphics.dispose();
        }
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint sky = new GradientPaint(
                0,
                0,
                new Color(71, 111, 173),
                0,
                PANEL_HEIGHT,
                new Color(23, 28, 49)
        );

        g2d.setPaint(sky);
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
    }

    private void drawMainMenu(Graphics2D g2d) {
        drawCenteredText(g2d, "O Caminho para o Amanhecer", PANEL_HEIGHT / 2 - 30, 38, new Color(246, 226, 174));
        drawCenteredText(g2d, "Pressione Enter para comecar", PANEL_HEIGHT / 2 + 30, 20, new Color(222, 232, 249));
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 172));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawCenteredText(
                g2d,
                "Levante-se. Ainda não chegamos ao amanhecer.",
                PANEL_HEIGHT / 2 - 12,
                28,
                new Color(245, 225, 222)
        );
        drawCenteredText(g2d, "Pressione Enter para renascer", PANEL_HEIGHT / 2 + 34, 18, new Color(201, 213, 235));
    }

    private void drawEnding(Graphics2D g2d) {
        g2d.setColor(new Color(8, 12, 27, 172));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawCenteredText(g2d, "O amanhecer chegou.", PANEL_HEIGHT / 2 - 12, 30, new Color(255, 230, 164));
        drawCenteredText(g2d, "Pressione Enter para voltar ao menu", PANEL_HEIGHT / 2 + 34, 18, new Color(218, 228, 247));
    }

    private void drawCenteredText(Graphics2D g2d, String text, int baselineY, int fontSize, Color color) {
        Font previousFont = g2d.getFont();
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        try {
            FontMetrics metrics = g2d.getFontMetrics();
            int x = (PANEL_WIDTH - metrics.stringWidth(text)) / 2;
            g2d.setColor(color);
            g2d.drawString(text, x, baselineY);
        } finally {
            g2d.setFont(previousFont);
        }
    }

    private void drawGround(Graphics2D g2d) {
        int groundTop = (int) physicsWorld.getFloorY();

        if (groundTop >= worldHeight) {
            return;
        }

        g2d.setColor(new Color(53, 107, 72));
        g2d.fillRect(0, groundTop, worldWidth, worldHeight - groundTop);

        g2d.setColor(new Color(92, 161, 108));
        g2d.fillRect(0, groundTop, worldWidth, 10);
    }

    private void drawPlatforms(Graphics2D g2d) {
        for (Platform platform : platforms) {
            platform.render(g2d);
        }
    }

    private void drawLevelExits(Graphics2D g2d) {
        for (LevelExit levelExit : levelExits) {
            levelExit.render(g2d);
        }
    }

    private void drawGates(Graphics2D g2d) {
        for (Gate gate : gates) {
            gate.render(g2d);
        }
    }

    private void drawSpikes(Graphics2D g2d) {
        for (Spike spike : spikes) {
            spike.render(g2d);
        }
    }

    private void drawManaPickups(Graphics2D g2d) {
        for (ManaPickup manaPickup : manaPickups) {
            manaPickup.render(g2d);
        }
    }

    private void drawEnemies(Graphics2D g2d) {
        for (Enemy enemy : enemies) {
            enemy.render(g2d, interpolationAlpha);
        }
    }

    private void drawMagicProjectiles(Graphics2D g2d) {
        for (MagicProjectile magicProjectile : magicProjectiles) {
            magicProjectile.render(g2d);
        }
    }

    private void drawInteractables(Graphics2D g2d) {
        for (Signpost signpost : signposts) {
            signpost.render(g2d);
        }

        for (MysteriousKnight mysteriousKnight : mysteriousKnights) {
            mysteriousKnight.render(g2d);
        }

        for (Lever lever : levers) {
            lever.render(g2d);
        }
    }

    private List<Signpost> extractSignposts(List<Interactable> interactables) {
        List<Signpost> foundSignposts = new ArrayList<>();

        for (Interactable interactable : interactables) {
            if (interactable instanceof Signpost signpost) {
                foundSignposts.add(signpost);
            }
        }

        return List.copyOf(foundSignposts);
    }

    private List<Lever> extractLevers(List<Interactable> interactables) {
        List<Lever> foundLevers = new ArrayList<>();

        for (Interactable interactable : interactables) {
            if (interactable instanceof Lever lever) {
                foundLevers.add(lever);
            }
        }

        return List.copyOf(foundLevers);
    }

    private List<MysteriousKnight> extractMysteriousKnights(List<Interactable> interactables) {
        List<MysteriousKnight> foundKnights = new ArrayList<>();

        for (Interactable interactable : interactables) {
            if (interactable instanceof MysteriousKnight mysteriousKnight) {
                foundKnights.add(mysteriousKnight);
            }
        }

        return List.copyOf(foundKnights);
    }

    private void loadLevel(int index) {
        if (index < 0 || index >= ALL_LEVELS.size()) {
            throw new IllegalArgumentException("Invalid level index: " + index);
        }

        String[] mapRows = ALL_LEVELS.get(index);
        Level level = LevelParser.parse(mapRows, TILE_SIZE);

        currentLevelIndex = index;
        worldWidth = getMaxColumns(mapRows) * TILE_SIZE;
        worldHeight = mapRows.length * TILE_SIZE;

        platforms = level.getPlatforms();
        spikes = level.getSpikes();
        enemies = level.getEnemies();
        gates = level.getGates();
        levelExits = level.getExits();
        manaPickups = level.getManaPickups();
        interactables = level.getInteractables();
        signposts = extractSignposts(interactables);
        levers = extractLevers(interactables);
        mysteriousKnights = extractMysteriousKnights(interactables);

        physicsWorld = new PhysicsWorld(worldWidth, worldHeight, platforms, gates);
        magicProjectiles.clear();
        player.respawn(level.getPlayerStartX(), level.getPlayerStartY());
        physicsWorld.resolve(player);
        player.refreshState();

        camera = new Camera(PANEL_WIDTH, PANEL_HEIGHT, worldWidth, worldHeight);
        camera.update(player);
        wasInteractPressed = false;
        dialogManager.close();
    }

    private void updateMainMenu(boolean confirmJustPressed) {
        if (confirmJustPressed) {
            gameState = GameState.PLAYING;
        }
    }

    private void updateGameplay(double deltaSeconds) {
        player.fixedUpdate(inputManager, deltaSeconds);
        playPendingPlayerSoundEvents();
        physicsWorld.resolve(player);
        spawnPendingMagicProjectile();
        updateMagicProjectiles(deltaSeconds);
        updateEnemies(deltaSeconds);
        handlePlayerAttacks();
        handleEnemyContactDamage();
        handleHazards();
        playPendingPlayerSoundEvents();
        if (player.isDead()) {
            gameState = GameState.GAME_OVER;
            return;
        }

        handleManaPickups();
        player.refreshState();
        handleInteraction();
        handleLevelTransition();
        camera.update(player);
    }

    private void updateGameOver(boolean confirmJustPressed) {
        if (!confirmJustPressed) {
            return;
        }

        magicProjectiles.clear();
        player.respawn();
        physicsWorld.resolve(player);
        player.refreshState();
        camera.update(player);
        wasInteractPressed = false;
        gameState = GameState.PLAYING;
    }

    private void updateEnding(boolean confirmJustPressed) {
        if (!confirmJustPressed) {
            return;
        }

        loadLevel(0);
        gameState = GameState.MAIN_MENU;
    }

    private void playPendingPlayerSoundEvents() {
        if (player.consumeJumpSoundRequest()) {
            audioManager.playEvent(AudioManager.SoundEffect.JUMP);
        }
        if (player.consumeDamageSoundRequest()) {
            audioManager.playEvent(AudioManager.SoundEffect.DAMAGE);
        }
    }

    private void updateEnemies(double deltaSeconds) {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }

            enemy.fixedUpdate(deltaSeconds, physicsWorld, player);
            if (enemy.usesWorldPhysics()) {
                physicsWorld.resolve(enemy);
                enemy.afterPhysicsResolve(physicsWorld);
            }
        }
    }

    private void spawnPendingMagicProjectile() {
        MagicProjectile projectile = player.consumePendingMagicProjectile();
        if (projectile != null) {
            magicProjectiles.add(projectile);
        }
    }

    private void updateMagicProjectiles(double deltaSeconds) {
        Iterator<MagicProjectile> iterator = magicProjectiles.iterator();
        while (iterator.hasNext()) {
            MagicProjectile projectile = iterator.next();
            projectile.fixedUpdate(deltaSeconds);

            if (projectile.isOutsideWorld(worldWidth)) {
                iterator.remove();
                continue;
            }

            for (Enemy enemy : enemies) {
                if (enemy.isDead() || !projectile.getBounds().intersects(enemy.getBounds())) {
                    continue;
                }

                enemy.takeDamage(projectile.getDamage());
                projectile.deactivate();
                break;
            }

            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    private void handlePlayerAttacks() {
        if (!player.isAttacking()) {
            return;
        }

        AABB attackHitbox = player.getAttackHitbox();
        for (Enemy enemy : enemies) {
            if (enemy.isDead() || !attackHitbox.intersects(enemy.getBounds())) {
                continue;
            }

            enemy.takeDamage(1);
        }
    }

    private void handleEnemyContactDamage() {
        AABB playerBounds = player.getBounds();
        for (Enemy enemy : enemies) {
            if (enemy.isDead() || !playerBounds.intersects(enemy.getBounds())) {
                continue;
            }

            player.takeDamage(1);
            physicsWorld.resolve(player);
            break;
        }
    }

    private void handleHazards() {
        AABB playerBounds = player.getBounds();
        for (Spike spike : spikes) {
            if (!playerBounds.intersects(spike.getBounds())) {
                continue;
            }

            player.takeDamage(1);
            physicsWorld.resolve(player);
            break;
        }
    }

    private void handleManaPickups() {
        AABB playerBounds = player.getBounds();
        for (ManaPickup manaPickup : manaPickups) {
            if (!manaPickup.isCollected() && playerBounds.intersects(manaPickup.getBounds())) {
                manaPickup.tryCollect(player);
            }
        }
    }

    private void handleLevelTransition() {
        AABB playerBounds = player.getBounds();
        for (LevelExit levelExit : levelExits) {
            if (!playerBounds.intersects(levelExit.getBounds())) {
                continue;
            }

            transitionToNextLevel();
            break;
        }
    }

    private void transitionToNextLevel() {
        int nextLevelIndex = currentLevelIndex + 1;
        if (nextLevelIndex >= ALL_LEVELS.size()) {
            gameState = GameState.ENDING;
            return;
        }

        loadLevel(nextLevelIndex);
    }

    private void handleInteraction() {
        boolean interactPressed = inputManager.isInteracting();
        if (!interactPressed || wasInteractPressed) {
            wasInteractPressed = interactPressed;
            return;
        }

        AABB interactionBounds = createInteractionBounds();
        for (Interactable interactable : interactables) {
            if (!interactionBounds.intersects(interactable.getInteractionBounds())) {
                continue;
            }

            if (interactable instanceof DialogInteractable dialogInteractable) {
                dialogManager.open(dialogInteractable.getDialogMessage());
                gameState = GameState.DIALOGUE;
            } else {
                interactable.onInteract(player);
            }
            break;
        }

        wasInteractPressed = true;
    }

    private void updateDialog(double deltaSeconds) {
        dialogManager.fixedUpdate(deltaSeconds);

        boolean interactPressed = inputManager.isInteracting();
        if (interactPressed && !wasInteractPressed) {
            dialogManager.advance();
        }
        wasInteractPressed = interactPressed;

        if (!dialogManager.isOpen()) {
            gameState = GameState.PLAYING;
        }
    }

    private AABB createInteractionBounds() {
        return new AABB(
                player.getX() - INTERACTION_RANGE,
                player.getY() - INTERACTION_RANGE * 0.5,
                player.getWidth() + INTERACTION_RANGE * 2.0,
                player.getHeight() + INTERACTION_RANGE
        );
    }

    private int getMaxColumns(String[] mapRows) {
        int maxColumns = 0;

        for (String row : mapRows) {
            if (row.length() > maxColumns) {
                maxColumns = row.length();
            }
        }

        return maxColumns;
    }
}
