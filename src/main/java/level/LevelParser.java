package level;

import entities.Enemy;
import entities.BossEnemy;
import entities.Collectible;
import entities.FlyingEnemy;
import entities.ForestWatcher;
import entities.Gate;
import entities.Interactable;
import entities.Lever;
import entities.LevelExit;
import entities.ManaPickup;
import entities.MemoryKey;
import entities.MysteriousKnight;
import entities.PatrolEnemy;
import entities.Platform;
import entities.PotionPickup;
import entities.PotionType;
import entities.RuneConsole;
import entities.RuneSymbol;
import entities.Signpost;
import entities.Spike;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LevelParser {
    private static final double PLAYER_WIDTH_RATIO = 0.75;
    private static final double PLAYER_HEIGHT_RATIO = 1.0;
    private static final double ENEMY_WIDTH_RATIO = 0.6875;
    private static final double ENEMY_HEIGHT_RATIO = 0.8125;
    private static final double FLYING_ENEMY_SIZE_RATIO = 0.625;
    private static final double BOSS_WIDTH_RATIO = 1.15;
    private static final double BOSS_HEIGHT_RATIO = 1.25;
    private static final double GATE_WIDTH_RATIO = 0.42;
    private static final double GATE_HEIGHT_RATIO = 1.45;
    private static final double LEVER_WIDTH_RATIO = 0.30;
    private static final double LEVER_HEIGHT_RATIO = 0.65;
    private static final double EXIT_WIDTH_RATIO = 0.625;
    private static final double EXIT_HEIGHT_RATIO = 0.90;
    private static final double MANA_PICKUP_SIZE_RATIO = 0.42;
    private static final double SIGNPOST_WIDTH_RATIO = 0.58;
    private static final double SIGNPOST_HEIGHT_RATIO = 0.75;
    private static final double KNIGHT_WIDTH_RATIO = 0.70;
    private static final double KNIGHT_HEIGHT_RATIO = 1.0;
    private static final double RUNE_SYMBOL_SIZE_RATIO = 0.58;
    private static final double RUNE_CONSOLE_WIDTH_RATIO = 0.70;
    private static final double RUNE_CONSOLE_HEIGHT_RATIO = 0.70;
    private static final double WATCHER_WIDTH_RATIO = 0.70;
    private static final double WATCHER_HEIGHT_RATIO = 0.90;
    private static final double MEMORY_KEY_SIZE_RATIO = 0.50;
    private static final double POTION_SIZE_RATIO = 0.48;
    private static final double COLLECTIBLE_SIZE_RATIO = 0.38;
    private static final int ENEMY_HEALTH = 2;
    private static final int FLYING_ENEMY_HEALTH = 2;
    private static final int BOSS_HEALTH = 10;
    private static final int MANA_RESTORE_AMOUNT = 50;
    private static final String SIGNPOST_MESSAGE = "Pressione E perto de alavancas, placas e viajantes.";
    private static final String KNIGHT_MESSAGE = "Nao deixe os guardas verem voce. Corra apenas se a floresta acordar.";

    private LevelParser() {
    }

    public static Level parse(String[] mapRows, int tileSize) {
        Objects.requireNonNull(mapRows, "mapRows");

        if (mapRows.length == 0) {
            throw new IllegalArgumentException("Map must contain at least one row.");
        }
        if (tileSize <= 0) {
            throw new IllegalArgumentException("tileSize must be greater than zero.");
        }

        List<Platform> platforms = new ArrayList<>();
        List<Spike> spikes = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();
        List<Gate> gates = new ArrayList<>();
        List<LevelExit> exits = new ArrayList<>();
        List<ManaPickup> manaPickups = new ArrayList<>();
        List<Interactable> interactables = new ArrayList<>();
        List<MemoryKey> memoryKeys = new ArrayList<>();
        List<ForestWatcher> forestWatchers = new ArrayList<>();
        List<PotionPickup> potionPickups = new ArrayList<>();
        List<Collectible> collectibles = new ArrayList<>();
        List<GridPosition> gatePositions = new ArrayList<>();
        List<GridPosition> leverPositions = new ArrayList<>();
        List<GridPosition> runeConsolePositions = new ArrayList<>();

        double playerStartX = Double.NaN;
        double playerStartY = Double.NaN;

        for (int row = 0; row < mapRows.length; row++) {
            String line = Objects.requireNonNull(mapRows[row], "map row " + row);

            for (int col = 0; col < line.length(); col++) {
                char tile = line.charAt(col);
                double tileX = col * (double) tileSize;
                double tileY = row * (double) tileSize;

                switch (tile) {
                    case '#':
                        platforms.add(new Platform(tileX, tileY, tileSize, tileSize));
                        break;
                    case '^':
                        spikes.add(createSpike(tileX, tileY, tileSize));
                        break;
                    case 'E':
                        enemies.add(createPatrolEnemy(tileX, tileY, tileSize));
                        break;
                    case 'V':
                        enemies.add(createFlyingEnemy(tileX, tileY, tileSize));
                        break;
                    case 'B':
                        enemies.add(createBossEnemy(tileX, tileY, tileSize));
                        break;
                    case 'G':
                        gatePositions.add(new GridPosition(col, row));
                        break;
                    case 'L':
                        leverPositions.add(new GridPosition(col, row));
                        break;
                    case 'X':
                        exits.add(createLevelExit(tileX, tileY, tileSize));
                        break;
                    case 'M':
                        manaPickups.add(createManaPickup(tileX, tileY, tileSize));
                        break;
                    case 'N':
                        interactables.add(createMysteriousKnight(tileX, tileY, tileSize));
                        break;
                    case 'S':
                        interactables.add(createSignpost(tileX, tileY, tileSize));
                        break;
                    case '1':
                        interactables.add(createRuneSymbol(tileX, tileY, tileSize, RuneSymbol.Rune.MOON, 1));
                        break;
                    case '2':
                        interactables.add(createRuneSymbol(tileX, tileY, tileSize, RuneSymbol.Rune.LEAF, 2));
                        break;
                    case '3':
                        interactables.add(createRuneSymbol(tileX, tileY, tileSize, RuneSymbol.Rune.SUN, 3));
                        break;
                    case 'R':
                        runeConsolePositions.add(new GridPosition(col, row));
                        break;
                    case 'W':
                        forestWatchers.add(createForestWatcher(tileX, tileY, tileSize, -1));
                        break;
                    case 'w':
                        forestWatchers.add(createForestWatcher(tileX, tileY, tileSize, 1));
                        break;
                    case 'K':
                        memoryKeys.add(createMemoryKey(tileX, tileY, tileSize));
                        break;
                    case 'H':
                        potionPickups.add(createPotionPickup(tileX, tileY, tileSize, PotionType.HEALTH));
                        break;
                    case 'O':
                        potionPickups.add(createPotionPickup(tileX, tileY, tileSize, PotionType.MANA));
                        break;
                    case 'C':
                        collectibles.add(createCollectible(tileX, tileY, tileSize));
                        break;
                    case 'P':
                        playerStartX = tileX + ((tileSize - (tileSize * PLAYER_WIDTH_RATIO)) * 0.5);
                        playerStartY = tileY + (tileSize - (tileSize * PLAYER_HEIGHT_RATIO));
                        break;
                    default:
                        break;
                }
            }
        }

        if (Double.isNaN(playerStartX) || Double.isNaN(playerStartY)) {
            throw new IllegalArgumentException("Map must contain a player start tile 'P'.");
        }
        if (!leverPositions.isEmpty() && gatePositions.isEmpty()) {
            throw new IllegalArgumentException("Map contains a lever but no gate.");
        }
        if (!runeConsolePositions.isEmpty() && gatePositions.isEmpty()) {
            throw new IllegalArgumentException("Map contains a rune console but no gate.");
        }

        for (GridPosition gatePosition : gatePositions) {
            gates.add(createGate(gatePosition, tileSize));
        }

        for (int index = 0; index < leverPositions.size(); index++) {
            GridPosition leverPosition = leverPositions.get(index);
            Gate targetGate = gates.get(Math.min(index, gates.size() - 1));
            interactables.add(createLever(leverPosition, tileSize, targetGate));
        }

        for (int index = 0; index < runeConsolePositions.size(); index++) {
            GridPosition consolePosition = runeConsolePositions.get(index);
            Gate targetGate = gates.get(Math.min(index, gates.size() - 1));
            interactables.add(createRuneConsole(consolePosition, tileSize, targetGate));
        }

        return new Level(
                platforms,
                spikes,
                enemies,
                gates,
                exits,
                manaPickups,
                interactables,
                memoryKeys,
                forestWatchers,
                potionPickups,
                collectibles,
                playerStartX,
                playerStartY
        );
    }

    private static Spike createSpike(double tileX, double tileY, int tileSize) {
        double spikeHeight = tileSize * 0.5;
        return new Spike(tileX, tileY + tileSize - spikeHeight, tileSize, spikeHeight);
    }

    private static PatrolEnemy createPatrolEnemy(double tileX, double tileY, int tileSize) {
        double enemyWidth = tileSize * ENEMY_WIDTH_RATIO;
        double enemyHeight = tileSize * ENEMY_HEIGHT_RATIO;
        double enemyX = tileX + ((tileSize - enemyWidth) * 0.5);
        double enemyY = tileY + (tileSize - enemyHeight);

        return new PatrolEnemy(enemyX, enemyY, enemyWidth, enemyHeight, ENEMY_HEALTH, 1);
    }

    private static FlyingEnemy createFlyingEnemy(double tileX, double tileY, int tileSize) {
        double enemySize = tileSize * FLYING_ENEMY_SIZE_RATIO;
        double enemyX = tileX + ((tileSize - enemySize) * 0.5);
        double enemyY = tileY + ((tileSize - enemySize) * 0.5);
        return new FlyingEnemy(enemyX, enemyY, enemySize, enemySize, FLYING_ENEMY_HEALTH, 1);
    }

    private static BossEnemy createBossEnemy(double tileX, double tileY, int tileSize) {
        double bossWidth = tileSize * BOSS_WIDTH_RATIO;
        double bossHeight = tileSize * BOSS_HEIGHT_RATIO;
        double bossX = tileX + ((tileSize - bossWidth) * 0.5);
        double bossY = tileY + tileSize - bossHeight;
        return new BossEnemy(bossX, bossY, bossWidth, bossHeight, BOSS_HEALTH, -1);
    }

    private static LevelExit createLevelExit(double tileX, double tileY, int tileSize) {
        double exitWidth = tileSize * EXIT_WIDTH_RATIO;
        double exitHeight = tileSize * EXIT_HEIGHT_RATIO;
        double exitX = tileX + ((tileSize - exitWidth) * 0.5);
        double exitY = tileY + (tileSize - exitHeight);

        return new LevelExit(exitX, exitY, exitWidth, exitHeight);
    }

    private static ManaPickup createManaPickup(double tileX, double tileY, int tileSize) {
        double pickupSize = tileSize * MANA_PICKUP_SIZE_RATIO;
        double pickupX = tileX + ((tileSize - pickupSize) * 0.5);
        double pickupY = tileY + ((tileSize - pickupSize) * 0.5);
        return new ManaPickup(pickupX, pickupY, pickupSize, pickupSize, MANA_RESTORE_AMOUNT);
    }

    private static MysteriousKnight createMysteriousKnight(double tileX, double tileY, int tileSize) {
        double knightWidth = tileSize * KNIGHT_WIDTH_RATIO;
        double knightHeight = tileSize * KNIGHT_HEIGHT_RATIO;
        double knightX = tileX + ((tileSize - knightWidth) * 0.5);
        double knightY = tileY + tileSize - knightHeight;
        return new MysteriousKnight(knightX, knightY, knightWidth, knightHeight, KNIGHT_MESSAGE);
    }

    private static RuneSymbol createRuneSymbol(
            double tileX,
            double tileY,
            int tileSize,
            RuneSymbol.Rune rune,
            int order
    ) {
        double symbolSize = tileSize * RUNE_SYMBOL_SIZE_RATIO;
        double symbolX = tileX + (tileSize - symbolSize) * 0.5;
        double symbolY = tileY + (tileSize - symbolSize) * 0.5;
        return new RuneSymbol(symbolX, symbolY, symbolSize, symbolSize, rune, order);
    }

    private static RuneConsole createRuneConsole(GridPosition consolePosition, int tileSize, Gate targetGate) {
        double consoleWidth = tileSize * RUNE_CONSOLE_WIDTH_RATIO;
        double consoleHeight = tileSize * RUNE_CONSOLE_HEIGHT_RATIO;
        double consoleX = (consolePosition.column * (double) tileSize) + (tileSize - consoleWidth) * 0.5;
        double consoleY = ((consolePosition.row + 1) * (double) tileSize) - consoleHeight;
        return new RuneConsole(
                consoleX,
                consoleY,
                consoleWidth,
                consoleHeight,
                targetGate,
                List.of(RuneSymbol.Rune.MOON, RuneSymbol.Rune.LEAF, RuneSymbol.Rune.SUN)
        );
    }

    private static ForestWatcher createForestWatcher(double tileX, double tileY, int tileSize, int direction) {
        double watcherWidth = tileSize * WATCHER_WIDTH_RATIO;
        double watcherHeight = tileSize * WATCHER_HEIGHT_RATIO;
        double watcherX = tileX + (tileSize - watcherWidth) * 0.5;
        double watcherY = tileY + tileSize - watcherHeight;
        return new ForestWatcher(watcherX, watcherY, watcherWidth, watcherHeight, direction);
    }

    private static MemoryKey createMemoryKey(double tileX, double tileY, int tileSize) {
        double keySize = tileSize * MEMORY_KEY_SIZE_RATIO;
        double keyX = tileX + (tileSize - keySize) * 0.5;
        double keyY = tileY + (tileSize - keySize) * 0.5;
        return new MemoryKey(keyX, keyY, keySize, keySize);
    }

    private static PotionPickup createPotionPickup(double tileX, double tileY, int tileSize, PotionType type) {
        double potionSize = tileSize * POTION_SIZE_RATIO;
        double potionX = tileX + (tileSize - potionSize) * 0.5;
        double potionY = tileY + tileSize - potionSize;
        return new PotionPickup(potionX, potionY, potionSize, potionSize, type);
    }

    private static Collectible createCollectible(double tileX, double tileY, int tileSize) {
        double collectibleSize = tileSize * COLLECTIBLE_SIZE_RATIO;
        double collectibleX = tileX + (tileSize - collectibleSize) * 0.5;
        double collectibleY = tileY + (tileSize - collectibleSize) * 0.5;
        return new Collectible(collectibleX, collectibleY, collectibleSize, collectibleSize);
    }

    private static Signpost createSignpost(double tileX, double tileY, int tileSize) {
        double signpostWidth = tileSize * SIGNPOST_WIDTH_RATIO;
        double signpostHeight = tileSize * SIGNPOST_HEIGHT_RATIO;
        double signpostX = tileX + ((tileSize - signpostWidth) * 0.5);
        double signpostY = tileY + tileSize - signpostHeight;
        return new Signpost(signpostX, signpostY, signpostWidth, signpostHeight, SIGNPOST_MESSAGE);
    }

    private static Gate createGate(GridPosition gatePosition, int tileSize) {
        double gateWidth = tileSize * GATE_WIDTH_RATIO;
        double gateHeight = tileSize * GATE_HEIGHT_RATIO;
        double gateX = (gatePosition.column * (double) tileSize) + ((tileSize - gateWidth) * 0.5);
        double gateY = ((gatePosition.row + 1) * (double) tileSize) - gateHeight;

        return new Gate(gateX, gateY, gateWidth, gateHeight);
    }

    private static Lever createLever(GridPosition leverPosition, int tileSize, Gate targetGate) {
        double leverWidth = tileSize * LEVER_WIDTH_RATIO;
        double leverHeight = tileSize * LEVER_HEIGHT_RATIO;
        double leverX = (leverPosition.column * (double) tileSize) + ((tileSize - leverWidth) * 0.5);
        double leverY = ((leverPosition.row + 1) * (double) tileSize) - leverHeight;

        return new Lever(leverX, leverY, leverWidth, leverHeight, targetGate);
    }

    private record GridPosition(int column, int row) {
    }
}
