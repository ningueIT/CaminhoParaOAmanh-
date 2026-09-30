package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

public final class RuneConsole implements Interactable {
    public enum SubmissionResult {
        IN_PROGRESS,
        SOLVED,
        INCORRECT
    }

    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Gate targetGate;
    private final List<RuneSymbol.Rune> solution;
    private int selectedIndex;
    private int solvedSymbols;
    private boolean solved;

    public RuneConsole(
            double x,
            double y,
            double width,
            double height,
            Gate targetGate,
            List<RuneSymbol.Rune> solution
    ) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Console dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetGate = Objects.requireNonNull(targetGate, "targetGate");
        this.solution = List.copyOf(Objects.requireNonNull(solution, "solution"));
        if (this.solution.isEmpty()) {
            throw new IllegalArgumentException("Console solution must not be empty.");
        }
        for (RuneSymbol.Rune rune : this.solution) {
            Objects.requireNonNull(rune, "solution rune");
        }
    }

    @Override
    public AABB getInteractionBounds() {
        return new AABB(x, y, width, height);
    }

    @Override
    public void onInteract(Player player) {
    }

    public void moveSelection(int direction) {
        if (direction == 0 || solved) {
            return;
        }

        RuneSymbol.Rune[] runes = RuneSymbol.Rune.values();
        selectedIndex = Math.floorMod(selectedIndex + direction, runes.length);
    }

    public SubmissionResult submitSelectedRune() {
        if (solved) {
            return SubmissionResult.SOLVED;
        }

        RuneSymbol.Rune selectedRune = getSelectedRune();
        if (solution.get(solvedSymbols) != selectedRune) {
            solvedSymbols = 0;
            return SubmissionResult.INCORRECT;
        }

        solvedSymbols++;
        if (solvedSymbols < solution.size()) {
            return SubmissionResult.IN_PROGRESS;
        }

        solved = true;
        targetGate.open();
        return SubmissionResult.SOLVED;
    }

    public RuneSymbol.Rune getSelectedRune() {
        return RuneSymbol.Rune.values()[selectedIndex];
    }

    public int getSolvedSymbols() {
        return solvedSymbols;
    }

    public int getSolutionLength() {
        return solution.size();
    }

    public boolean isSolved() {
        return solved;
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        g2d.setColor(solved ? new Color(77, 134, 105) : new Color(71, 66, 96));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);
        g2d.setColor(solved ? new Color(175, 244, 183) : new Color(180, 168, 234));
        g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);

        int nodeSize = Math.max(6, Math.min(renderWidth / 5, renderHeight / 4));
        int spacing = Math.max(nodeSize + 3, (renderWidth - nodeSize) / (solution.size() + 1));
        for (int index = 0; index < solution.size(); index++) {
            int nodeX = renderX + spacing * (index + 1) - nodeSize / 2;
            int nodeY = renderY + renderHeight / 2 - nodeSize / 2;
            g2d.setColor(index < solvedSymbols ? new Color(255, 224, 119) : new Color(43, 39, 62));
            g2d.fillOval(nodeX, nodeY, nodeSize, nodeSize);
        }
    }
}
