package level;

import entities.Enemy;
import entities.Gate;
import entities.Interactable;
import entities.LevelExit;
import entities.ManaPickup;
import entities.Platform;
import entities.Spike;

import java.util.List;
import java.util.Objects;

public final class Level {
    private final List<Platform> platforms;
    private final List<Spike> spikes;
    private final List<Enemy> enemies;
    private final List<Gate> gates;
    private final List<LevelExit> exits;
    private final List<ManaPickup> manaPickups;
    private final List<Interactable> interactables;
    private final double playerStartX;
    private final double playerStartY;

    public Level(
            List<Platform> platforms,
            List<Spike> spikes,
            List<Enemy> enemies,
            List<Gate> gates,
            List<LevelExit> exits,
            List<ManaPickup> manaPickups,
            List<Interactable> interactables,
            double playerStartX,
            double playerStartY
    ) {
        this.platforms = List.copyOf(Objects.requireNonNull(platforms, "platforms"));
        this.spikes = List.copyOf(Objects.requireNonNull(spikes, "spikes"));
        this.enemies = List.copyOf(Objects.requireNonNull(enemies, "enemies"));
        this.gates = List.copyOf(Objects.requireNonNull(gates, "gates"));
        this.exits = List.copyOf(Objects.requireNonNull(exits, "exits"));
        this.manaPickups = List.copyOf(Objects.requireNonNull(manaPickups, "manaPickups"));
        this.interactables = List.copyOf(Objects.requireNonNull(interactables, "interactables"));
        this.playerStartX = playerStartX;
        this.playerStartY = playerStartY;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Spike> getSpikes() {
        return spikes;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public List<LevelExit> getExits() {
        return exits;
    }

    public List<ManaPickup> getManaPickups() {
        return manaPickups;
    }

    public List<Interactable> getInteractables() {
        return interactables;
    }

    public double getPlayerStartX() {
        return playerStartX;
    }

    public double getPlayerStartY() {
        return playerStartY;
    }
}
