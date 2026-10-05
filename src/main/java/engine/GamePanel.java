package engine;

import entities.Enemy;
import entities.BossEnemy;
import entities.BossProjectile;
import entities.BrambleBarrier;
import entities.Collectible;
import entities.CorruptionZone;
import entities.DialogInteractable;
import entities.FlyingEnemy;
import entities.ForestWatcher;
import entities.Gate;
import entities.Interactable;
import entities.LevelExit;
import entities.Lever;
import entities.LightBeacon;
import entities.MagicProjectile;
import entities.ManaPickup;
import entities.MemoryKey;
import entities.MysteriousKnight;
import entities.Platform;
import entities.Player;
import entities.PatrolEnemy;
import entities.PotionPickup;
import entities.RuneConsole;
import entities.RuneSymbol;
import entities.Signpost;
import entities.Spike;
import input.InputManager;
import level.Level;
import level.LevelParser;
import physics.AABB;
import physics.PhysicsWorld;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class GamePanel extends JPanel {
    public static final int PANEL_WIDTH = 1280;
    public static final int PANEL_HEIGHT = 720;
    private static final int TILE_SIZE = 64;
    private static final double PLAYER_WIDTH_RATIO = 0.75;
    private static final double PLAYER_HEIGHT_RATIO = 1.0;
    private static final double INTERACTION_RANGE = 24.0;
    private static final int MAX_PARTICLES = 160;
    private static final int MIN_DARKNESS_ALPHA = 28;
    private static final int MAX_DARKNESS_ALPHA = 210;
    private static final float PLAYER_LIGHT_RADIUS = 260.0f;
    private static final int PHASE_THREE_INDEX = 2;
    private static final int FINAL_LEVEL_INDEX = 4;
    private static final double FOREST_REACTION_DURATION = 10.0;
    private static final double ENDING_DURATION = 11.0;
    private static final String[] LEVEL_1 = {
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "..P.CS..1....2..H.M....3..O.R..G.C..X....",
            "########################################"
    };
    private static final String[] LEVEL_2 = {
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "...........####.........................",
            "........................................",
            "........................................",
            "....................####................",
            "........................................",
            "........................................",
            "......####..............................",
            "........................................",
            "........................................",
            "........................................",
            "..P.C..M..H.E..A....V....O..E..YC...X....",
            "########################################"
    };
    private static final String[] LEVEL_3 = {
            "........................................",
            "........................................",
            "........................................",
            "........####............................",
            "........................................",
            "........................................",
            ".................#####..................",
            "........................................",
            "........................................",
            "............................####........",
            "........................................",
            "........................................",
            "............####........................",
            "........................................",
            "..P..N.C.K...w....H.....W...O..G.C..X.....",
            "########################################"
    };
    private static final String[] LEVEL_4 = {
            "........................................",
            "........................................",
            "........................................",
            "............................X.C.........",
            "..........................####..........",
            "........................................",
            "........................................",
            "....................V...................",
            "..................####..................",
            "........................................",
            "............V...C.......................",
            "..........####..........................",
            "........................................",
            "......V....O..A.........................",
            "........................................",
            "..P.H..E..C....Y.........................",
            "########################################"
    };
    private static final String[] LEVEL_5 = {
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "..........####################..........",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            ".......####..................####.......",
            "........................................",
            "........................................",
            "........................................",
            "..P.H.O.^^..C.......B......C.....^^^......!",
            "########################################"
    };
    private static final List<String[]> ALL_LEVELS = List.of(LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5);
    private static final List<String> LEVEL_BGM_RESOURCES = List.of(
            "/audio/level_1.wav",
            "/audio/level_2.wav",
            "/audio/level_3.wav",
            "/audio/level_4.wav",
            "/audio/level_5.wav"
    );

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
    private final List<Particle> particles = new ArrayList<>();
    private PhysicsWorld physicsWorld;
    private List<Signpost> signposts = List.of();
    private List<MysteriousKnight> mysteriousKnights = List.of();
    private List<RuneSymbol> runeSymbols = List.of();
    private List<RuneConsole> runeConsoles = List.of();
    private List<MemoryKey> memoryKeys = List.of();
    private List<ForestWatcher> forestWatchers = List.of();
    private List<PotionPickup> potionPickups = List.of();
    private List<Collectible> collectibles = List.of();
    private List<LightBeacon> lightBeacons = List.of();
    private List<BrambleBarrier> brambleBarriers = List.of();
    private final List<BossProjectile> bossProjectiles = new ArrayList<>();
    private final List<CorruptionZone> corruptionZones = new ArrayList<>();
    private final EnumSet<RuneSymbol.Rune> observedRunes = EnumSet.noneOf(RuneSymbol.Rune.class);
    private final Set<String> collectedWorldItemIds = new HashSet<>();
    private int worldWidth;
    private int worldHeight;
    private int currentLevelIndex;
    private volatile GameState gameState = GameState.MAIN_MENU;

    private volatile double interpolationAlpha;
    private boolean wasInteractPressed;
    private boolean wasConfirmPressed;
    private boolean wasMovingLeftPressed;
    private boolean wasMovingRightPressed;
    private boolean wasMovingUpPressed;
    private boolean wasMovingDownPressed;
    private RuneConsole activeRuneConsole;
    private ArrowSequence memorySequence;
    private StealthState stealthState = StealthState.INACTIVE;
    private double forestReactionRemainingSeconds;
    private CollapsePhase collapsePhase;
    private double collapseElapsedSeconds;
    private double endingElapsedSeconds;
    public static final int MENU_TAB_PLAY = 0;
    public static final int MENU_TAB_OPTIONS = 1;

    private final WindowController windowController;
    private int activeMenuTab = MENU_TAB_PLAY;
    private int optionsMenuSelection = 0;
    private boolean wasCancelPressed;
    private boolean wasTabPressed;
    private boolean wasFullScreenTogglePressed;
    private boolean wasMousePressed;
    private boolean letterUnlocked;
    private boolean replayingLetter;
    private int mainMenuSelection;
    private int hitstopFrames;
    private BufferedImage darknessMask;

    public GamePanel(InputManager inputManager) {
        this(inputManager, null);
    }

    public GamePanel(InputManager inputManager, WindowController windowController) {
        this.inputManager = inputManager;
        this.windowController = windowController != null ? windowController : new WindowController() {
            private boolean fs;

            @Override
            public boolean isFullScreen() {
                return fs;
            }

            @Override
            public void setFullScreen(boolean fullScreen) {
                this.fs = fullScreen;
            }
        };
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
        addMouseListener(inputManager);
        addMouseMotionListener(inputManager);
    }

    public WindowController getWindowController() {
        return windowController;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public int getActiveMenuTab() {
        return activeMenuTab;
    }

    public void setActiveMenuTab(int tab) {
        this.activeMenuTab = tab;
    }

    public int getOptionsMenuSelection() {
        return optionsMenuSelection;
    }

    public void setOptionsMenuSelection(int selection) {
        this.optionsMenuSelection = selection;
    }

    public int getPlayMenuSelection() {
        return mainMenuSelection;
    }

    public void setPlayMenuSelection(int selection) {
        this.mainMenuSelection = selection;
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
        audioManager.closeAll();
    }

    public void fixedUpdate(double deltaSeconds) {
        synchronized (worldLock) {
            InputFrame inputFrame = pollInputFrame();

            if (inputFrame.fullScreenToggleJustPressed()) {
                windowController.toggleFullScreen();
            }

            switch (gameState) {
                case MAIN_MENU -> updateMainMenu(inputFrame);
                case PLAYING -> updateGameplay(deltaSeconds);
                case DIALOGUE -> updateDialog(deltaSeconds);
                case RUNE_PUZZLE -> updateRunePuzzle(inputFrame);
                case MEMORY_SEQUENCE -> updateMemorySequence(deltaSeconds, inputFrame);
                case GAME_OVER -> updateGameOver(deltaSeconds, inputFrame.confirmJustPressed());
                case COLLAPSE -> updateCollapse(deltaSeconds);
                case ENDING -> updateEnding(deltaSeconds, inputFrame.confirmJustPressed());
            }
        }
    }

    public boolean consumeHitstopFrame() {
        synchronized (worldLock) {
            if (hitstopFrames == 0) {
                return false;
            }

            hitstopFrames--;
            return true;
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void requestRender(double interpolationAlpha) {
        this.interpolationAlpha = interpolationAlpha;
        repaint();
    }

    public double getRenderScale() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return 1.0;
        }
        return Math.min((double) w / PANEL_WIDTH, (double) h / PANEL_HEIGHT);
    }

    public int getRenderOffsetX() {
        int w = getWidth();
        double scale = getRenderScale();
        return (int) Math.round((w - PANEL_WIDTH * scale) / 2.0);
    }

    public int getRenderOffsetY() {
        int h = getHeight();
        double scale = getRenderScale();
        return (int) Math.round((h - PANEL_HEIGHT * scale) / 2.0);
    }

    public int screenToGameX(int screenX) {
        double scale = getRenderScale();
        if (scale <= 0.0) {
            return screenX;
        }
        return (int) Math.round((screenX - getRenderOffsetX()) / scale);
    }

    public int screenToGameY(int screenY) {
        double scale = getRenderScale();
        if (scale <= 0.0) {
            return screenY;
        }
        return (int) Math.round((screenY - getRenderOffsetY()) / scale);
    }

    public int getVirtualMouseX() {
        return screenToGameX(inputManager.getMouseX());
    }

    public int getVirtualMouseY() {
        return screenToGameY(inputManager.getMouseY());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics.create();
        try {
            int panelW = getWidth();
            int panelH = getHeight();

            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, panelW, panelH);

            double scale = getRenderScale();
            int offsetX = getRenderOffsetX();
            int offsetY = getRenderOffsetY();

            g2d.translate(offsetX, offsetY);
            g2d.scale(scale, scale);
            g2d.setClip(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
            drawDynamicLighting(g2d);
            hud.render(g2d);
            switch (gameState) {
                case DIALOGUE -> dialogManager.render(g2d, PANEL_WIDTH, PANEL_HEIGHT);
                case RUNE_PUZZLE -> drawRunePuzzle(g2d);
                case MEMORY_SEQUENCE -> drawMemorySequence(g2d);
                case GAME_OVER -> drawGameOver(g2d);
                case COLLAPSE -> drawCollapse(g2d);
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
                drawBrambleBarriers(worldGraphics);
                drawSpikes(worldGraphics);
                drawManaPickups(worldGraphics);
                drawPotionPickups(worldGraphics);
                drawCollectibles(worldGraphics);
                drawLightBeacons(worldGraphics);
                drawMemoryKeys(worldGraphics);
                drawForestWatchers(worldGraphics);
                drawCorruptionZones(worldGraphics);
                drawEnemies(worldGraphics);
                drawBossProjectiles(worldGraphics);
                drawMagicProjectiles(worldGraphics);
                drawParticles(worldGraphics);
                drawInteractables(worldGraphics);
                player.render(worldGraphics, interpolationAlpha);
            }
        } finally {
            worldGraphics.dispose();
        }
    }

    private void drawBackground(Graphics2D g2d) {
        Color topColor;
        Color bottomColor;
        if (collapsePhase == CollapsePhase.DAWN || gameState == GameState.ENDING) {
            topColor = new Color(244, 181, 116);
            bottomColor = new Color(103, 151, 151);
        } else {
            int finalLevelIndex = Math.max(1, ALL_LEVELS.size() - 1);
            float levelProgress = Math.min(1.0f, currentLevelIndex / (float) finalLevelIndex);
            topColor = interpolateColor(new Color(91, 157, 187), new Color(49, 45, 79), levelProgress);
            bottomColor = interpolateColor(new Color(36, 76, 76), new Color(17, 19, 38), levelProgress);
        }
        GradientPaint sky = new GradientPaint(
                0,
                0,
                topColor,
                0,
                PANEL_HEIGHT,
                bottomColor
        );

        g2d.setPaint(sky);
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
    }

    private void drawDynamicLighting(Graphics2D g2d) {
        int finalLevelIndex = Math.max(1, ALL_LEVELS.size() - 1);
        float levelProgress = Math.min(1.0f, currentLevelIndex / (float) finalLevelIndex);
        int edgeAlpha = Math.round(
                MIN_DARKNESS_ALPHA + (MAX_DARKNESS_ALPHA - MIN_DARKNESS_ALPHA) * levelProgress
        );
        ensureDarknessMask();

        Graphics2D maskGraphics = darknessMask.createGraphics();
        Composite previousComposite = maskGraphics.getComposite();
        try {
            maskGraphics.setComposite(AlphaComposite.Src);
            maskGraphics.setColor(new Color(0, 0, 0, edgeAlpha));
            maskGraphics.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            maskGraphics.setComposite(AlphaComposite.DstOut);
            carveLight(
                    maskGraphics,
                    player.getRenderX(interpolationAlpha) + player.getWidth() * 0.5 - camera.getX(),
                    player.getRenderY(interpolationAlpha) + player.getHeight() * 0.5 - camera.getY(),
                    PLAYER_LIGHT_RADIUS
            );

            for (MagicProjectile magicProjectile : magicProjectiles) {
                carveLight(
                        maskGraphics,
                        magicProjectile.getCenterX() - camera.getX(),
                        magicProjectile.getCenterY() - camera.getY(),
                        128.0f
                );
            }

            for (LightBeacon lightBeacon : lightBeacons) {
                if (!lightBeacon.isLit()) {
                    continue;
                }

                carveLight(
                        maskGraphics,
                        lightBeacon.getLightX() - camera.getX(),
                        lightBeacon.getLightY() - camera.getY(),
                        lightBeacon.getLightRadius()
                );
            }
        } finally {
            maskGraphics.setComposite(previousComposite);
            maskGraphics.dispose();
        }

        g2d.drawImage(darknessMask, 0, 0, null);
    }

    private void ensureDarknessMask() {
        if (darknessMask != null
                && darknessMask.getWidth() == PANEL_WIDTH
                && darknessMask.getHeight() == PANEL_HEIGHT) {
            return;
        }

        darknessMask = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    }

    private void carveLight(Graphics2D g2d, double screenX, double screenY, float radius) {
        if (screenX + radius < 0.0 || screenX - radius > PANEL_WIDTH
                || screenY + radius < 0.0 || screenY - radius > PANEL_HEIGHT) {
            return;
        }

        RadialGradientPaint lightGradient = new RadialGradientPaint(
                new Point2D.Float((float) screenX, (float) screenY),
                radius,
                new float[] {0.0f, 0.38f, 1.0f},
                new Color[] {
                        new Color(255, 255, 255, 255),
                        new Color(255, 255, 255, 176),
                        new Color(255, 255, 255, 0)
                }
        );
        g2d.setPaint(lightGradient);
        int diameter = Math.round(radius * 2.0f);
        g2d.fillOval(
                (int) Math.round(screenX - radius),
                (int) Math.round(screenY - radius),
                diameter,
                diameter
        );
    }

    private void drawMainMenu(Graphics2D g2d) {
        drawCenteredText(g2d, "O Caminho para o Amanhecer", PANEL_HEIGHT / 2 - 135, 38, new Color(246, 226, 174));

        drawTabsHeader(g2d);

        int cardX = PANEL_WIDTH / 2 - 270;
        int cardY = PANEL_HEIGHT / 2 - 40;
        int cardW = 540;
        int cardH = 210;

        g2d.setColor(new Color(15, 20, 36, 190));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);
        g2d.setColor(new Color(90, 115, 160, 140));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 16, 16);

        if (activeMenuTab == MENU_TAB_PLAY) {
            drawPlayTab(g2d);
        } else {
            drawOptionsTab(g2d);
        }
    }

    private void drawTabsHeader(Graphics2D g2d) {
        int tabY = PANEL_HEIGHT / 2 - 95;
        int tabW = 160;
        int tabH = 40;
        int tab1X = PANEL_WIDTH / 2 - 170;
        int tab2X = PANEL_WIDTH / 2 + 10;

        drawTabButton(g2d, "Jogar", tab1X, tabY, tabW, tabH, activeMenuTab == MENU_TAB_PLAY);
        drawTabButton(g2d, "Opções", tab2X, tabY, tabW, tabH, activeMenuTab == MENU_TAB_OPTIONS);
    }

    private void drawTabButton(Graphics2D g2d, String title, int x, int y, int w, int h, boolean active) {
        if (active) {
            g2d.setColor(new Color(246, 226, 174, 55));
            g2d.fillRoundRect(x, y, w, h, 12, 12);
            g2d.setColor(new Color(246, 226, 174));
            g2d.drawRoundRect(x, y, w, h, 12, 12);
            g2d.fillRect(x + 20, y + h - 3, w - 40, 3);
            drawCenteredText(g2d, title, y + 27, 20, new Color(255, 238, 185));
        } else {
            g2d.setColor(new Color(20, 26, 45, 140));
            g2d.fillRoundRect(x, y, w, h, 12, 12);
            g2d.setColor(new Color(110, 130, 170, 120));
            g2d.drawRoundRect(x, y, w, h, 12, 12);
            drawCenteredText(g2d, title, y + 26, 18, new Color(170, 185, 215));
        }
    }

    private void drawPlayTab(Graphics2D g2d) {
        drawMenuOption(g2d, "Iniciar jornada", PANEL_HEIGHT / 2 + 15, mainMenuSelection == 0);
        if (letterUnlocked) {
            drawMenuOption(g2d, "Rever a carta", PANEL_HEIGHT / 2 + 70, mainMenuSelection == 1);
        }
        drawCenteredText(
                g2d,
                "Setas Cima/Baixo para navegar - Enter para confirmar - Direita ou Tab para Opções",
                PANEL_HEIGHT / 2 + 200,
                15,
                new Color(210, 224, 245)
        );
    }

    private void drawOptionsTab(Graphics2D g2d) {
        boolean volumeSelected = optionsMenuSelection == 0;
        boolean fullScreenSelected = optionsMenuSelection == 1;
        boolean backSelected = optionsMenuSelection == 2;

        int volumeY = PANEL_HEIGHT / 2 - 8;
        String volumeLabel = (volumeSelected ? "> " : "  ") + "Volume do Jogo:  <  " + audioManager.getVolumePercentage() + "%  >";
        drawCenteredText(g2d, volumeLabel, volumeY, 19, volumeSelected ? new Color(255, 230, 164) : new Color(190, 204, 227));

        int barW = 280;
        int barH = 14;
        int barX = PANEL_WIDTH / 2 - barW / 2;
        int barY = volumeY + 12;

        g2d.setColor(new Color(25, 32, 50));
        g2d.fillRoundRect(barX, barY, barW, barH, 8, 8);
        int fillW = (int) Math.round(barW * audioManager.getVolume());
        if (fillW > 0) {
            g2d.setColor(volumeSelected ? new Color(246, 226, 174) : new Color(160, 195, 240));
            g2d.fillRoundRect(barX, barY, fillW, barH, 8, 8);
        }
        g2d.setColor(new Color(110, 130, 170, 160));
        g2d.drawRoundRect(barX, barY, barW, barH, 8, 8);

        g2d.setColor(volumeSelected ? new Color(255, 230, 164) : new Color(170, 185, 215));
        g2d.drawString("<", barX - 22, barY + 12);
        g2d.drawString(">", barX + barW + 12, barY + 12);

        int fsY = PANEL_HEIGHT / 2 + 62;
        String fsStatus = windowController.isFullScreen() ? "Ativado" : "Desativado";
        String fsLabel = (fullScreenSelected ? "> " : "  ") + "Tela Cheia:  <  " + fsStatus + "  >";
        drawCenteredText(g2d, fsLabel, fsY, 19, fullScreenSelected ? new Color(255, 230, 164) : new Color(190, 204, 227));

        int backY = PANEL_HEIGHT / 2 + 115;
        drawCenteredText(g2d, (backSelected ? "> " : "  ") + "Voltar para a aba Jogar", backY, 19, backSelected ? new Color(255, 230, 164) : new Color(190, 204, 227));

        drawCenteredText(
                g2d,
                "Setas Esquerda/Direita para ajustar - Enter para alternar - Esc ou Voltar para a aba Jogar",
                PANEL_HEIGHT / 2 + 200,
                15,
                new Color(210, 224, 245)
        );
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
        g2d.setColor(new Color(8, 12, 27, 205));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawCenteredText(g2d, replayingLetter ? "A carta do Cavaleiro" : "O amanhecer chegou.", PANEL_HEIGHT / 2 - 112, 30, new Color(255, 230, 164));

        if (endingElapsedSeconds >= 1.2) {
            drawCenteredText(
                    g2d,
                    "Voce encontrou a saida porque continuou caminhando.",
                    PANEL_HEIGHT / 2 - 42,
                    20,
                    new Color(233, 235, 244)
            );
        }
        if (endingElapsedSeconds >= 3.2) {
            drawCenteredText(
                    g2d,
                    "Quando a escuridao chamar seu nome, procure a primeira luz.",
                    PANEL_HEIGHT / 2 - 6,
                    20,
                    new Color(233, 235, 244)
            );
        }
        if (endingElapsedSeconds >= 5.2) {
            drawCenteredText(g2d, "Fim.", PANEL_HEIGHT / 2 + 58, 30, new Color(255, 230, 164));
        }
        if (endingElapsedSeconds >= 6.8) {
            drawCenteredText(g2d, "Obrigado por caminhar comigo.", PANEL_HEIGHT / 2 + 102, 22, new Color(218, 228, 247));
        }
        if (endingElapsedSeconds >= 8.0) {
            drawCenteredText(g2d, "Pressione Enter para voltar ao menu", PANEL_HEIGHT / 2 + 150, 16, new Color(174, 197, 225));
        }
    }

    private void drawMenuOption(Graphics2D g2d, String label, int baselineY, boolean selected) {
        Color color = selected ? new Color(255, 230, 164) : new Color(190, 204, 227);
        String prefix = selected ? "> " : "  ";
        drawCenteredText(g2d, prefix + label, baselineY, 22, color);
    }

    private void drawRunePuzzle(Graphics2D g2d) {
        if (activeRuneConsole == null) {
            return;
        }

        g2d.setColor(new Color(8, 11, 25, 220));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawCenteredText(g2d, "As Imagens da Parede", PANEL_HEIGHT / 2 - 130, 30, new Color(255, 226, 164));
        drawCenteredText(
                g2d,
                "Escolha os simbolos na ordem encontrada.",
                PANEL_HEIGHT / 2 - 88,
                19,
                new Color(219, 230, 248)
        );
        drawCenteredText(
                g2d,
                "Simbolo: " + activeRuneConsole.getSelectedRune().getDisplayName().toUpperCase(),
                PANEL_HEIGHT / 2 - 8,
                28,
                activeRuneConsole.getSelectedRune().getColor()
        );
        drawCenteredText(
                g2d,
                "Progresso: " + activeRuneConsole.getSolvedSymbols() + " / " + activeRuneConsole.getSolutionLength(),
                PANEL_HEIGHT / 2 + 40,
                19,
                new Color(217, 224, 244)
        );
        drawCenteredText(g2d, "A/D ou Setas: escolher - E: confirmar", PANEL_HEIGHT / 2 + 100, 17, new Color(180, 202, 235));
    }

    private void drawMemorySequence(Graphics2D g2d) {
        if (memorySequence == null) {
            return;
        }

        g2d.setColor(new Color(10, 12, 28, 225));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawCenteredText(g2d, "O Caminho dos Olhos", PANEL_HEIGHT / 2 - 142, 30, new Color(231, 183, 255));
        drawCenteredText(g2d, "Repita a sequencia antes que a floresta acorde.", PANEL_HEIGHT / 2 - 100, 19, new Color(224, 230, 248));

        int tokenWidth = 132;
        int tokenHeight = 48;
        int gap = 14;
        int totalWidth = memorySequence.getSequence().size() * tokenWidth
                + (memorySequence.getSequence().size() - 1) * gap;
        int startX = (PANEL_WIDTH - totalWidth) / 2;
        int tokenY = PANEL_HEIGHT / 2 - tokenHeight / 2;
        for (int index = 0; index < memorySequence.getSequence().size(); index++) {
            boolean completed = index < memorySequence.getCurrentIndex();
            g2d.setColor(completed ? new Color(104, 184, 132) : new Color(58, 65, 98));
            int tokenX = startX + index * (tokenWidth + gap);
            g2d.fillRoundRect(tokenX, tokenY, tokenWidth, tokenHeight, 12, 12);
            g2d.setColor(completed ? new Color(222, 255, 228) : new Color(217, 226, 248));
            g2d.drawRoundRect(tokenX, tokenY, tokenWidth, tokenHeight, 12, 12);
            drawTextAt(g2d, memorySequence.getSequence().get(index).getLabel(), tokenX + 16, tokenY + 30, 16, new Color(244, 247, 255));
        }

        int seconds = (int) Math.ceil(memorySequence.getRemainingSeconds());
        drawCenteredText(g2d, "Tempo: " + seconds + "s", PANEL_HEIGHT / 2 + 96, 22, new Color(255, 205, 145));
        drawCenteredText(g2d, "Use W/A/S/D ou as setas", PANEL_HEIGHT / 2 + 138, 17, new Color(180, 202, 235));
    }

    private void drawCollapse(Graphics2D g2d) {
        if (collapsePhase == null) {
            return;
        }

        float progress = (float) Math.min(1.0, collapseElapsedSeconds / 8.0);
        int alpha = Math.min(220, 50 + Math.round(progress * 170.0f));
        g2d.setColor(new Color(63, 21, 70, alpha));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        if (collapsePhase != CollapsePhase.DAWN) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int index = 0; index < 12; index++) {
                int y = random.nextInt(PANEL_HEIGHT);
                int width = random.nextInt(80, 280);
                g2d.setColor(new Color(227, 104, 195, random.nextInt(30, 100)));
                g2d.fillRect(random.nextInt(PANEL_WIDTH - width), y, width, random.nextInt(2, 8));
            }
        }

        if (collapsePhase == CollapsePhase.KNIGHT_ARRIVAL
                || collapsePhase == CollapsePhase.STRIKE
                || collapsePhase == CollapsePhase.DAWN) {
            drawCinematicKnight(g2d);
        }

        if (collapsePhase == CollapsePhase.CORRUPTION) {
            drawCenteredText(g2d, "A floresta ainda esta dentro de voce.", 90, 22, new Color(246, 196, 238));
        } else if (collapsePhase == CollapsePhase.KNIGHT_ARRIVAL) {
            drawCenteredText(g2d, "Passos.", 90, 22, new Color(238, 236, 247));
        } else if (collapsePhase == CollapsePhase.STRIKE) {
            drawCenteredText(g2d, "Um unico golpe.", 90, 24, new Color(255, 230, 164));
        } else if (collapsePhase == CollapsePhase.DAWN) {
            drawCenteredText(g2d, "O primeiro raio de sol.", 90, 24, new Color(255, 230, 164));
        }
    }

    private void drawCinematicKnight(Graphics2D g2d) {
        double arrivalProgress = Math.min(1.0, Math.max(0.0, (collapseElapsedSeconds - 3.6) / 1.6));
        int knightX = (int) Math.round(PANEL_WIDTH - 140 - arrivalProgress * 330.0);
        int knightY = PANEL_HEIGHT - 270;

        g2d.setColor(new Color(37, 39, 62));
        g2d.fillRoundRect(knightX, knightY, 62, 150, 18, 18);
        g2d.setColor(new Color(169, 183, 212));
        g2d.fillRoundRect(knightX + 12, knightY + 12, 38, 44, 14, 14);
        g2d.setColor(new Color(246, 223, 149));
        g2d.fillOval(knightX + 22, knightY + 26, 7, 7);
        g2d.fillOval(knightX + 34, knightY + 26, 7, 7);

        if (collapsePhase == CollapsePhase.STRIKE || collapsePhase == CollapsePhase.DAWN) {
            g2d.setColor(new Color(255, 242, 189));
            g2d.fillRect(knightX - 130, knightY + 34, 142, 8);
        }
    }

    private void drawTextAt(Graphics2D g2d, String text, int x, int baselineY, int fontSize, Color color) {
        Font previousFont = g2d.getFont();
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        try {
            g2d.setColor(color);
            g2d.drawString(text, x, baselineY);
        } finally {
            g2d.setFont(previousFont);
        }
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

    private Color interpolateColor(Color from, Color to, float progress) {
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));
        int red = Math.round(from.getRed() + (to.getRed() - from.getRed()) * clampedProgress);
        int green = Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * clampedProgress);
        int blue = Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * clampedProgress);
        return new Color(red, green, blue);
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

    private void drawBrambleBarriers(Graphics2D g2d) {
        for (BrambleBarrier brambleBarrier : brambleBarriers) {
            brambleBarrier.render(g2d);
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

    private void drawPotionPickups(Graphics2D g2d) {
        for (PotionPickup potionPickup : potionPickups) {
            potionPickup.render(g2d);
        }
    }

    private void drawCollectibles(Graphics2D g2d) {
        for (Collectible collectible : collectibles) {
            collectible.render(g2d);
        }
    }

    private void drawLightBeacons(Graphics2D g2d) {
        for (LightBeacon lightBeacon : lightBeacons) {
            lightBeacon.render(g2d);
        }
    }

    private void drawMemoryKeys(Graphics2D g2d) {
        for (MemoryKey memoryKey : memoryKeys) {
            memoryKey.render(g2d);
        }
    }

    private void drawForestWatchers(Graphics2D g2d) {
        for (ForestWatcher forestWatcher : forestWatchers) {
            forestWatcher.render(g2d);
        }
    }

    private void drawCorruptionZones(Graphics2D g2d) {
        for (CorruptionZone corruptionZone : corruptionZones) {
            corruptionZone.render(g2d);
        }
    }

    private void drawEnemies(Graphics2D g2d) {
        for (Enemy enemy : enemies) {
            enemy.render(g2d, interpolationAlpha);
        }
    }

    private void drawBossProjectiles(Graphics2D g2d) {
        for (BossProjectile bossProjectile : bossProjectiles) {
            bossProjectile.render(g2d);
        }
    }

    private void drawMagicProjectiles(Graphics2D g2d) {
        for (MagicProjectile magicProjectile : magicProjectiles) {
            magicProjectile.render(g2d);
        }
    }

    private void drawParticles(Graphics2D g2d) {
        for (Particle particle : particles) {
            particle.render(g2d);
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

        for (RuneSymbol runeSymbol : runeSymbols) {
            runeSymbol.render(g2d);
        }

        for (RuneConsole runeConsole : runeConsoles) {
            runeConsole.render(g2d);
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

    private List<RuneSymbol> extractRuneSymbols(List<Interactable> interactables) {
        List<RuneSymbol> foundSymbols = new ArrayList<>();

        for (Interactable interactable : interactables) {
            if (interactable instanceof RuneSymbol runeSymbol) {
                foundSymbols.add(runeSymbol);
            }
        }

        return List.copyOf(foundSymbols);
    }

    private List<RuneConsole> extractRuneConsoles(List<Interactable> interactables) {
        List<RuneConsole> foundConsoles = new ArrayList<>();

        for (Interactable interactable : interactables) {
            if (interactable instanceof RuneConsole runeConsole) {
                foundConsoles.add(runeConsole);
            }
        }

        return List.copyOf(foundConsoles);
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
        enemies = new ArrayList<>(level.getEnemies());
        gates = level.getGates();
        levelExits = level.getExits();
        manaPickups = level.getManaPickups();
        interactables = level.getInteractables();
        signposts = extractSignposts(interactables);
        levers = extractLevers(interactables);
        mysteriousKnights = extractMysteriousKnights(interactables);
        runeSymbols = extractRuneSymbols(interactables);
        runeConsoles = extractRuneConsoles(interactables);
        memoryKeys = level.getMemoryKeys();
        forestWatchers = level.getForestWatchers();
        potionPickups = level.getPotionPickups();
        collectibles = level.getCollectibles();
        lightBeacons = level.getLightBeacons();
        brambleBarriers = level.getBrambleBarriers();
        restoreCollectedWorldItems();

        physicsWorld = new PhysicsWorld(worldWidth, worldHeight, platforms, gates, brambleBarriers);
        magicProjectiles.clear();
        bossProjectiles.clear();
        corruptionZones.clear();
        particles.clear();
        observedRunes.clear();
        activeRuneConsole = null;
        memorySequence = null;
        stealthState = index == PHASE_THREE_INDEX ? StealthState.SNEAKING : StealthState.INACTIVE;
        forestReactionRemainingSeconds = 0.0;
        collapsePhase = null;
        collapseElapsedSeconds = 0.0;
        hitstopFrames = 0;
        player.respawn(level.getPlayerStartX(), level.getPlayerStartY());
        physicsWorld.resolve(player);
        player.refreshState();

        camera = new Camera(PANEL_WIDTH, PANEL_HEIGHT, worldWidth, worldHeight);
        camera.update(player);
        wasInteractPressed = false;
        dialogManager.close();
        startBackgroundMusic(index);
    }

    private void beginNewJourney() {
        player.resetJourneyInventory();
        collectedWorldItemIds.clear();
        loadLevel(0);
    }

    private void restoreCollectedWorldItems() {
        for (int index = 0; index < potionPickups.size(); index++) {
            if (collectedWorldItemIds.contains(getWorldItemId("potion", index))) {
                potionPickups.get(index).markCollected();
            }
        }

        for (int index = 0; index < collectibles.size(); index++) {
            if (collectedWorldItemIds.contains(getWorldItemId("collectible", index))) {
                collectibles.get(index).markCollected();
            }
        }
    }

    private void startBackgroundMusic(int levelIndex) {
        String trackId = "level-" + (levelIndex + 1);
        String resourcePath = LEVEL_BGM_RESOURCES.get(levelIndex);
        if (!audioManager.playBackgroundLoop(trackId, resourcePath)) {
            System.err.println("BGM resource not found; procedural fallback enabled: " + resourcePath);
        }
    }

    private void updateMainMenu(InputFrame inputFrame) {
        if (inputFrame.tabJustPressed()) {
            activeMenuTab = (activeMenuTab == MENU_TAB_PLAY) ? MENU_TAB_OPTIONS : MENU_TAB_PLAY;
            return;
        }

        // Mouse click handling
        if (inputFrame.mouseJustPressed()) {
            int mx = getVirtualMouseX();
            int my = getVirtualMouseY();

            // Tab 0 (Jogar) header: [470, 260, 160, 44]
            if (mx >= 470 && mx <= 630 && my >= 250 && my <= 305) {
                activeMenuTab = MENU_TAB_PLAY;
                return;
            }
            // Tab 1 (Opções) header: [650, 260, 160, 44]
            if (mx >= 650 && mx <= 810 && my >= 250 && my <= 305) {
                activeMenuTab = MENU_TAB_OPTIONS;
                return;
            }

            if (activeMenuTab == MENU_TAB_PLAY) {
                // Iniciar jornada: [420, 350, 440, 45]
                if (mx >= 420 && mx <= 860 && my >= 350 && my <= 395) {
                    mainMenuSelection = 0;
                    replayingLetter = false;
                    beginNewJourney();
                    gameState = GameState.PLAYING;
                    return;
                }
                // Rever a carta: [420, 405, 440, 45]
                if (letterUnlocked && mx >= 420 && mx <= 860 && my >= 405 && my <= 450) {
                    mainMenuSelection = 1;
                    replayingLetter = true;
                    endingElapsedSeconds = 0.0;
                    gameState = GameState.ENDING;
                    return;
                }
            } else {
                // Volume row: [420, 335, 440, 55]
                if (my >= 335 && my <= 395) {
                    optionsMenuSelection = 0;
                    // Volume decrease button: [465, 345, 35, 40]
                    if (mx >= 465 && mx <= 505) {
                        audioManager.decreaseVolume(0.05f);
                    }
                    // Volume increase button: [775, 345, 35, 40]
                    else if (mx >= 775 && mx <= 815) {
                        audioManager.increaseVolume(0.05f);
                    }
                    // Volume slider bar: [500, 350, 280, 30]
                    else if (mx >= 500 && mx <= 780) {
                        float newVol = (float) (mx - 500) / 280.0f;
                        audioManager.setVolume(newVol);
                    }
                    return;
                }
                // Tela cheia row: [420, 400, 440, 45]
                if (mx >= 420 && mx <= 860 && my >= 400 && my <= 445) {
                    optionsMenuSelection = 1;
                    windowController.toggleFullScreen();
                    return;
                }
                // Voltar row: [420, 455, 440, 45]
                if (mx >= 420 && mx <= 860 && my >= 455 && my <= 500) {
                    activeMenuTab = MENU_TAB_PLAY;
                    return;
                }
            }
        }

        // Cancel / Escape key
        if (inputFrame.cancelJustPressed()) {
            if (activeMenuTab == MENU_TAB_OPTIONS) {
                activeMenuTab = MENU_TAB_PLAY;
                return;
            }
        }

        // Keyboard navigation
        if (activeMenuTab == MENU_TAB_PLAY) {
            int optionCount = letterUnlocked ? 2 : 1;
            if (inputFrame.movingUpJustPressed()) {
                mainMenuSelection = Math.floorMod(mainMenuSelection - 1, optionCount);
            } else if (inputFrame.movingDownJustPressed()) {
                mainMenuSelection = Math.floorMod(mainMenuSelection + 1, optionCount);
            } else if (inputFrame.movingRightJustPressed()) {
                activeMenuTab = MENU_TAB_OPTIONS;
                return;
            }

            if (!inputFrame.confirmJustPressed()) {
                return;
            }

            if (mainMenuSelection == 0) {
                replayingLetter = false;
                beginNewJourney();
                gameState = GameState.PLAYING;
                return;
            }

            replayingLetter = true;
            endingElapsedSeconds = 0.0;
            gameState = GameState.ENDING;
        } else {
            // Options tab
            if (inputFrame.movingUpJustPressed()) {
                optionsMenuSelection = Math.floorMod(optionsMenuSelection - 1, 3);
            } else if (inputFrame.movingDownJustPressed()) {
                optionsMenuSelection = Math.floorMod(optionsMenuSelection + 1, 3);
            }

            if (optionsMenuSelection == 0) { // Volume
                if (inputFrame.movingLeftJustPressed()) {
                    audioManager.decreaseVolume(0.05f);
                } else if (inputFrame.movingRightJustPressed()) {
                    audioManager.increaseVolume(0.05f);
                } else if (inputFrame.confirmJustPressed()) {
                    if (audioManager.getVolume() >= 0.99f) {
                        audioManager.setVolume(0.0f);
                    } else {
                        audioManager.increaseVolume(0.10f);
                    }
                }
            } else if (optionsMenuSelection == 1) { // Tela Cheia
                if (inputFrame.confirmJustPressed()
                        || inputFrame.movingLeftJustPressed()
                        || inputFrame.movingRightJustPressed()) {
                    windowController.toggleFullScreen();
                }
            } else if (optionsMenuSelection == 2) { // Voltar
                if (inputFrame.confirmJustPressed()
                        || inputFrame.movingLeftJustPressed()) {
                    activeMenuTab = MENU_TAB_PLAY;
                }
            }
        }
    }

    private void updateGameplay(double deltaSeconds) {
        updateAttackAimFromMouse();
        boolean wasAirborne = !player.isOnGround();
        player.fixedUpdate(inputManager, deltaSeconds);
        playPendingPlayerSoundEvents();
        physicsWorld.resolve(player);
        if (wasAirborne && player.isOnGround()) {
            spawnLandingDust();
        }
        spawnPendingMagicProjectile();
        updateMagicProjectiles(deltaSeconds);
        if (gameState != GameState.PLAYING) {
            camera.update(player, deltaSeconds);
            return;
        }
        updateEnemies(deltaSeconds);
        collectBossAttackEvents();
        updateBossProjectiles(deltaSeconds);
        updateCorruptionZones(deltaSeconds);
        handlePlayerAttacks();
        if (gameState != GameState.PLAYING) {
            camera.update(player, deltaSeconds);
            return;
        }
        handleEnemyContactDamage();
        handleHazards();
        updateParticles(deltaSeconds);
        playPendingPlayerSoundEvents();
        if (player.isDead()) {
            camera.update(player, deltaSeconds);
            gameState = GameState.GAME_OVER;
            return;
        }

        handleManaPickups();
        handlePotionPickups();
        handleCollectibles();
        handleMemoryKeys();
        if (gameState != GameState.PLAYING) {
            camera.update(player, deltaSeconds);
            return;
        }
        updateStealthEncounter(deltaSeconds);
        if (gameState != GameState.PLAYING) {
            camera.update(player, deltaSeconds);
            return;
        }
        player.refreshState();
        handleInteraction();
        handleLevelTransition();
        camera.update(player, deltaSeconds);
    }

    private void updateAttackAimFromMouse() {
        if (!inputManager.hasMousePosition()) {
            return;
        }

        player.updateAttackAim(
                getVirtualMouseX() + camera.getX(),
                getVirtualMouseY() + camera.getY()
        );
    }

    private void updateGameOver(double deltaSeconds, boolean confirmJustPressed) {
        camera.update(player);
        updateParticles(deltaSeconds);
        if (!confirmJustPressed) {
            return;
        }

        loadLevel(currentLevelIndex);
        gameState = GameState.PLAYING;
    }

    private void updateEnding(double deltaSeconds, boolean confirmJustPressed) {
        endingElapsedSeconds += deltaSeconds;
        if (endingElapsedSeconds < 8.0 || (!confirmJustPressed && endingElapsedSeconds < ENDING_DURATION)) {
            return;
        }

        replayingLetter = false;
        mainMenuSelection = 0;
        activeMenuTab = MENU_TAB_PLAY;
        optionsMenuSelection = 0;
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

    private void collectBossAttackEvents() {
        for (Enemy enemy : enemies) {
            if (!(enemy instanceof BossEnemy bossEnemy) || bossEnemy.isDead()) {
                continue;
            }

            BossProjectile projectile;
            while ((projectile = bossEnemy.consumePendingProjectile()) != null) {
                bossProjectiles.add(projectile);
            }

            CorruptionZone corruptionZone;
            while ((corruptionZone = bossEnemy.consumePendingCorruptionZone()) != null) {
                corruptionZones.add(corruptionZone);
            }
        }
    }

    private void updateBossProjectiles(double deltaSeconds) {
        Iterator<BossProjectile> iterator = bossProjectiles.iterator();
        while (iterator.hasNext()) {
            BossProjectile projectile = iterator.next();
            projectile.fixedUpdate(deltaSeconds);
            if (projectile.isOutsideWorld(worldWidth, worldHeight)) {
                iterator.remove();
                continue;
            }

            if (projectile.getBounds().intersects(player.getBounds())) {
                if (damagePlayer(1)) {
                    player.applyKnockbackFrom(projectile.getBounds().getLeft());
                }
                projectile.deactivate();
            }

            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    private void updateCorruptionZones(double deltaSeconds) {
        Iterator<CorruptionZone> iterator = corruptionZones.iterator();
        while (iterator.hasNext()) {
            CorruptionZone corruptionZone = iterator.next();
            corruptionZone.fixedUpdate(deltaSeconds);
            if (!corruptionZone.isActive()) {
                iterator.remove();
                continue;
            }

            if (corruptionZone.isDangerous() && corruptionZone.getBounds().intersects(player.getBounds())) {
                damagePlayer(1);
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

            if (handleMagicEnvironmentImpact(projectile)) {
                iterator.remove();
                continue;
            }

            for (Enemy enemy : enemies) {
                if (enemy.isDead() || !projectile.getBounds().intersects(enemy.getBounds())) {
                    continue;
                }

                damageEnemy(enemy, projectile.getDamage());
                projectile.deactivate();
                break;
            }

            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    private boolean handleMagicEnvironmentImpact(MagicProjectile projectile) {
        for (LightBeacon lightBeacon : lightBeacons) {
            if (!projectile.getBounds().intersects(lightBeacon.getBounds()) || !lightBeacon.ignite()) {
                continue;
            }

            projectile.deactivate();
            spawnEnvironmentParticles(
                    lightBeacon.getLightX(),
                    lightBeacon.getLightY(),
                    new Color(255, 222, 119),
                    18
            );
            audioManager.playEvent(AudioManager.SoundEffect.AURORA);
            camera.shake(4.0, 0.08);
            requestHitstop(2);
            return true;
        }

        for (BrambleBarrier brambleBarrier : brambleBarriers) {
            if (brambleBarrier.isBurned() || !projectile.getBounds().intersects(brambleBarrier.getBounds())) {
                continue;
            }
            if (!brambleBarrier.burn()) {
                continue;
            }

            projectile.deactivate();
            spawnEnvironmentParticles(
                    brambleBarrier.getCenterX(),
                    brambleBarrier.getCenterY(),
                    new Color(132, 166, 92),
                    22
            );
            audioManager.playEvent(AudioManager.SoundEffect.BRAMBLE);
            camera.shake(5.0, 0.10);
            requestHitstop(2);
            return true;
        }

        return false;
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

            damageEnemy(enemy, 1);
        }
    }

    private void handleEnemyContactDamage() {
        AABB playerBounds = player.getBounds();
        for (Enemy enemy : enemies) {
            if (enemy.isDead() || !playerBounds.intersects(enemy.getBounds())) {
                continue;
            }

            if (enemy instanceof FlyingEnemy && isStompingFlyingEnemy(playerBounds, enemy.getBounds())) {
                player.setPosition(player.getX(), enemy.getY() - player.getHeight());
                damageEnemy(enemy, 1);
                player.bounceFromEnemy();
                camera.shake(6.0, 0.10);
                return;
            }

            if (damagePlayer(1)) {
                player.applyKnockbackFrom(enemy.getX() + enemy.getWidth() * 0.5);
            }
            physicsWorld.resolve(player);
            break;
        }
    }

    private boolean isStompingFlyingEnemy(AABB playerBounds, AABB enemyBounds) {
        double allowedOverlap = Math.max(12.0, enemyBounds.getBottom() - enemyBounds.getTop()) * 0.45;
        return player.getVelocityY() > 0.0
                && playerBounds.getTop() < enemyBounds.getTop()
                && playerBounds.getBottom() <= enemyBounds.getTop() + allowedOverlap;
    }

    private void handleHazards() {
        AABB playerBounds = player.getBounds();
        for (Spike spike : spikes) {
            if (!playerBounds.intersects(spike.getBounds())) {
                continue;
            }

            damagePlayer(1);
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

    private void handlePotionPickups() {
        AABB playerBounds = player.getBounds();
        for (int index = 0; index < potionPickups.size(); index++) {
            PotionPickup potionPickup = potionPickups.get(index);
            if (!playerBounds.intersects(potionPickup.getBounds()) || !potionPickup.tryCollect(player)) {
                continue;
            }

            collectedWorldItemIds.add(getWorldItemId("potion", index));
            spawnItemCollectionParticles(potionPickup.getType().getHighlightColor());
        }
    }

    private void handleCollectibles() {
        AABB playerBounds = player.getBounds();
        for (int index = 0; index < collectibles.size(); index++) {
            Collectible collectible = collectibles.get(index);
            if (!playerBounds.intersects(collectible.getBounds()) || !collectible.tryCollect(player)) {
                continue;
            }

            collectedWorldItemIds.add(getWorldItemId("collectible", index));
            spawnItemCollectionParticles(new Color(255, 226, 123));
        }
    }

    private String getWorldItemId(String type, int index) {
        return currentLevelIndex + ":" + type + ":" + index;
    }

    private void spawnItemCollectionParticles(Color color) {
        double originX = player.getX() + player.getWidth() * 0.5;
        double originY = player.getY() + player.getHeight() * 0.5;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < 10; index++) {
            addParticle(new Particle(
                    originX,
                    originY,
                    random.nextDouble(-120.0, 120.0),
                    random.nextDouble(-180.0, -40.0),
                    random.nextDouble(0.22, 0.48),
                    color,
                    random.nextInt(3, 6)
            ));
        }
    }

    private void handleMemoryKeys() {
        if (currentLevelIndex != PHASE_THREE_INDEX || stealthState != StealthState.SNEAKING) {
            return;
        }

        AABB playerBounds = player.getBounds();
        for (MemoryKey memoryKey : memoryKeys) {
            if (!playerBounds.intersects(memoryKey.getBounds()) || !memoryKey.collect()) {
                continue;
            }

            memorySequence = new ArrowSequence(
                    List.of(
                            ArrowSequence.Direction.UP,
                            ArrowSequence.Direction.RIGHT,
                            ArrowSequence.Direction.RIGHT,
                            ArrowSequence.Direction.DOWN,
                            ArrowSequence.Direction.LEFT
                    ),
                    6.0
            );
            gameState = GameState.MEMORY_SEQUENCE;
            return;
        }
    }

    private void updateStealthEncounter(double deltaSeconds) {
        if (currentLevelIndex != PHASE_THREE_INDEX) {
            return;
        }

        if (stealthState == StealthState.SNEAKING) {
            for (ForestWatcher forestWatcher : forestWatchers) {
                if (forestWatcher.canSee(player)) {
                    startForestReaction();
                    return;
                }
            }
            return;
        }

        if (stealthState != StealthState.ESCAPE) {
            return;
        }

        forestReactionRemainingSeconds = Math.max(0.0, forestReactionRemainingSeconds - deltaSeconds);
        if (forestReactionRemainingSeconds > 0.0) {
            return;
        }

        for (Gate gate : gates) {
            gate.close();
        }
        loadLevel(currentLevelIndex);
        gameState = GameState.GAME_OVER;
    }

    private void updateMemorySequence(double deltaSeconds, InputFrame inputFrame) {
        if (memorySequence == null) {
            gameState = GameState.PLAYING;
            return;
        }

        memorySequence.fixedUpdate(deltaSeconds);
        ArrowSequence.Direction direction = resolveSequenceDirection(inputFrame);
        if (direction != null) {
            memorySequence.submit(direction);
        }

        if (memorySequence.getStatus() == ArrowSequence.Status.COMPLETED) {
            completeStealthEncounter();
            memorySequence = null;
            gameState = GameState.PLAYING;
            return;
        }

        if (memorySequence.getStatus() == ArrowSequence.Status.FAILED) {
            memorySequence = null;
            startForestReaction();
            gameState = GameState.PLAYING;
        }
    }

    private void startForestReaction() {
        if (stealthState != StealthState.SNEAKING) {
            return;
        }

        stealthState = StealthState.ESCAPE;
        forestReactionRemainingSeconds = FOREST_REACTION_DURATION;
        for (Gate gate : gates) {
            gate.open();
        }
        spawnForestReinforcements();
        camera.shake(14.0, 0.34);
    }

    private void completeStealthEncounter() {
        stealthState = StealthState.COMPLETE;
        for (Gate gate : gates) {
            gate.open();
        }

        for (int index = 0; index < 18; index++) {
            addParticle(new Particle(
                    player.getX() + player.getWidth() * 0.5,
                    player.getY() + player.getHeight() * 0.5,
                    ThreadLocalRandom.current().nextDouble(-150.0, 150.0),
                    ThreadLocalRandom.current().nextDouble(-170.0, -30.0),
                    ThreadLocalRandom.current().nextDouble(0.30, 0.65),
                    new Color(155, 226, 255),
                    ThreadLocalRandom.current().nextInt(3, 7)
            ));
        }
    }

    private void spawnForestReinforcements() {
        double enemyWidth = TILE_SIZE * 0.6875;
        double enemyHeight = TILE_SIZE * 0.8125;
        double floorY = physicsWorld.getFloorY();
        double[] spawnPositions = {
                Math.max(0.0, player.getX() - TILE_SIZE * 3.0),
                Math.min(worldWidth - enemyWidth, player.getX() + TILE_SIZE * 4.0)
        };

        for (int index = 0; index < spawnPositions.length; index++) {
            enemies.add(new PatrolEnemy(
                    spawnPositions[index],
                    floorY - enemyHeight,
                    enemyWidth,
                    enemyHeight,
                    2,
                    index == 0 ? 1 : -1
            ));
        }
    }

    private ArrowSequence.Direction resolveSequenceDirection(InputFrame inputFrame) {
        if (inputFrame.movingUpJustPressed()) {
            return ArrowSequence.Direction.UP;
        }
        if (inputFrame.movingRightJustPressed()) {
            return ArrowSequence.Direction.RIGHT;
        }
        if (inputFrame.movingDownJustPressed()) {
            return ArrowSequence.Direction.DOWN;
        }
        if (inputFrame.movingLeftJustPressed()) {
            return ArrowSequence.Direction.LEFT;
        }
        return null;
    }

    private void updateRunePuzzle(InputFrame inputFrame) {
        if (activeRuneConsole == null || activeRuneConsole.isSolved()) {
            activeRuneConsole = null;
            gameState = GameState.PLAYING;
            return;
        }

        if (inputFrame.movingLeftJustPressed()) {
            activeRuneConsole.moveSelection(-1);
        } else if (inputFrame.movingRightJustPressed()) {
            activeRuneConsole.moveSelection(1);
        }

        boolean interactPressed = inputManager.isInteracting();
        if (interactPressed && !wasInteractPressed) {
            RuneConsole.SubmissionResult result = activeRuneConsole.submitSelectedRune();
            if (result == RuneConsole.SubmissionResult.SOLVED) {
                activeRuneConsole = null;
                gameState = GameState.PLAYING;
            } else if (result == RuneConsole.SubmissionResult.INCORRECT) {
                camera.shake(7.0, 0.12);
            }
        }
        wasInteractPressed = interactPressed;
    }

    private void beginCollapse() {
        if (currentLevelIndex != FINAL_LEVEL_INDEX || gameState != GameState.PLAYING) {
            return;
        }

        bossProjectiles.clear();
        corruptionZones.clear();
        magicProjectiles.clear();
        collapseElapsedSeconds = 0.0;
        collapsePhase = CollapsePhase.CORRUPTION;
        gameState = GameState.COLLAPSE;
        camera.shake(16.0, 0.50);
    }

    private void updateCollapse(double deltaSeconds) {
        collapseElapsedSeconds += deltaSeconds;
        if (collapseElapsedSeconds < 2.2) {
            collapsePhase = CollapsePhase.CORRUPTION;
            player.fixedUpdateCinematic(deltaSeconds, 72.0);
            physicsWorld.resolve(player);
        } else if (collapseElapsedSeconds < 3.6) {
            collapsePhase = CollapsePhase.FALL;
            player.fixedUpdateCinematic(deltaSeconds, 0.0);
            physicsWorld.resolve(player);
        } else if (collapseElapsedSeconds < 5.4) {
            collapsePhase = CollapsePhase.KNIGHT_ARRIVAL;
        } else if (collapseElapsedSeconds < 6.5) {
            collapsePhase = CollapsePhase.STRIKE;
            camera.shake(8.0, 0.10);
        } else {
            collapsePhase = CollapsePhase.DAWN;
        }

        updateParticles(deltaSeconds);
        camera.update(player, deltaSeconds);
        if (collapseElapsedSeconds < 8.8) {
            return;
        }

        letterUnlocked = true;
        replayingLetter = false;
        endingElapsedSeconds = 0.0;
        gameState = GameState.ENDING;
    }

    private InputFrame pollInputFrame() {
        boolean movingLeft = inputManager.isMovingLeft();
        boolean movingRight = inputManager.isMovingRight();
        boolean movingUp = inputManager.isMovingUp();
        boolean movingDown = inputManager.isMovingDown();
        boolean confirmPressed = inputManager.isConfirmPressed();
        boolean cancelPressed = inputManager.isEscapePressed();
        boolean tabPressed = inputManager.isTabPressed();
        boolean fullScreenTogglePressed = inputManager.isFullScreenTogglePressed();
        boolean mousePressed = inputManager.isLeftMousePressed();

        InputFrame inputFrame = new InputFrame(
                movingLeft && !wasMovingLeftPressed,
                movingRight && !wasMovingRightPressed,
                movingUp && !wasMovingUpPressed,
                movingDown && !wasMovingDownPressed,
                confirmPressed && !wasConfirmPressed,
                cancelPressed && !wasCancelPressed,
                tabPressed && !wasTabPressed,
                fullScreenTogglePressed && !wasFullScreenTogglePressed,
                mousePressed && !wasMousePressed
        );

        wasMovingLeftPressed = movingLeft;
        wasMovingRightPressed = movingRight;
        wasMovingUpPressed = movingUp;
        wasMovingDownPressed = movingDown;
        wasConfirmPressed = confirmPressed;
        wasCancelPressed = cancelPressed;
        wasTabPressed = tabPressed;
        wasFullScreenTogglePressed = fullScreenTogglePressed;
        wasMousePressed = mousePressed;
        return inputFrame;
    }

    private void updateParticles(double deltaSeconds) {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.fixedUpdate(deltaSeconds);
            if (!particle.isAlive()) {
                iterator.remove();
            }
        }
    }

    private boolean damagePlayer(int amount) {
        int healthBeforeDamage = player.getCurrentHealth();
        player.takeDamage(amount);
        if (player.getCurrentHealth() < healthBeforeDamage) {
            camera.shake(9.0, 0.16);
            requestHitstop(2);
            return true;
        }
        return false;
    }

    private void damageEnemy(Enemy enemy, int amount) {
        int healthBeforeDamage = enemy.getCurrentHealth();
        enemy.takeDamage(amount);
        if (enemy.getCurrentHealth() == healthBeforeDamage) {
            return;
        }

        spawnHitSparks(enemy);
        requestHitstop(enemy instanceof BossEnemy ? 5 : 3);
        if (enemy instanceof BossEnemy bossEnemy) {
            camera.shake(12.0, 0.22);
            if (bossEnemy.isDead()) {
                beginCollapse();
            }
        }
    }

    private void spawnLandingDust() {
        double originX = player.getX() + player.getWidth() * 0.5;
        double originY = player.getY() + player.getHeight();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < 8; index++) {
            addParticle(new Particle(
                    originX + random.nextDouble(-14.0, 14.0),
                    originY,
                    random.nextDouble(-95.0, 95.0),
                    random.nextDouble(-105.0, -35.0),
                    random.nextDouble(0.28, 0.52),
                    new Color(192, 164, 122),
                    random.nextInt(3, 7)
            ));
        }
    }

    private void spawnHitSparks(Enemy enemy) {
        double originX = enemy.getX() + enemy.getWidth() * 0.5;
        double originY = enemy.getY() + enemy.getHeight() * 0.5;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < 7; index++) {
            addParticle(new Particle(
                    originX,
                    originY,
                    random.nextDouble(-175.0, 175.0),
                    random.nextDouble(-175.0, 175.0),
                    random.nextDouble(0.16, 0.34),
                    new Color(255, 224, 121),
                    random.nextInt(3, 6)
            ));
        }
    }

    private void spawnEnvironmentParticles(double originX, double originY, Color color, int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < count; index++) {
            addParticle(new Particle(
                    originX,
                    originY,
                    random.nextDouble(-165.0, 165.0),
                    random.nextDouble(-190.0, -30.0),
                    random.nextDouble(0.26, 0.58),
                    color,
                    random.nextInt(3, 7)
            ));
        }
    }

    private void addParticle(Particle particle) {
        if (particles.size() == MAX_PARTICLES) {
            particles.remove(0);
        }
        particles.add(particle);
    }

    private void requestHitstop(int frames) {
        if (frames <= 0) {
            throw new IllegalArgumentException("Hitstop frames must be greater than zero.");
        }

        hitstopFrames = Math.max(hitstopFrames, frames);
    }

    private void handleLevelTransition() {
        if (currentLevelIndex == 0 && !isRunePuzzleSolved()) {
            return;
        }
        if (currentLevelIndex == PHASE_THREE_INDEX && stealthState == StealthState.SNEAKING) {
            return;
        }
        if (currentLevelIndex == FINAL_LEVEL_INDEX && hasLivingBoss()) {
            return;
        }

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
            beginCollapse();
            return;
        }

        loadLevel(nextLevelIndex);
    }

    private boolean hasLivingBoss() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof BossEnemy && !enemy.isDead()) {
                return true;
            }
        }
        return false;
    }

    private boolean isRunePuzzleSolved() {
        if (runeConsoles.isEmpty()) {
            return true;
        }

        for (RuneConsole runeConsole : runeConsoles) {
            if (!runeConsole.isSolved()) {
                return false;
            }
        }
        return true;
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

            if (interactable instanceof RuneSymbol runeSymbol) {
                observedRunes.add(runeSymbol.getRune());
                dialogManager.open(runeSymbol.getDialogMessage());
                gameState = GameState.DIALOGUE;
            } else if (interactable instanceof RuneConsole runeConsole) {
                if (observedRunes.size() < runeSymbols.size()) {
                    dialogManager.open("As imagens espalhadas pela floresta ainda escondem parte da sequencia.");
                    gameState = GameState.DIALOGUE;
                } else {
                    activeRuneConsole = runeConsole;
                    gameState = GameState.RUNE_PUZZLE;
                }
            } else if (interactable instanceof DialogInteractable dialogInteractable) {
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

    private enum StealthState {
        INACTIVE,
        SNEAKING,
        ESCAPE,
        COMPLETE
    }

    private enum CollapsePhase {
        CORRUPTION,
        FALL,
        KNIGHT_ARRIVAL,
        STRIKE,
        DAWN
    }

    private record InputFrame(
            boolean movingLeftJustPressed,
            boolean movingRightJustPressed,
            boolean movingUpJustPressed,
            boolean movingDownJustPressed,
            boolean confirmJustPressed,
            boolean cancelJustPressed,
            boolean tabJustPressed,
            boolean fullScreenToggleJustPressed,
            boolean mouseJustPressed
    ) {
    }
}
