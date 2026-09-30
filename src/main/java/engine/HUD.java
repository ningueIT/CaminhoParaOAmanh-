package engine;

import entities.Player;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Objects;

public final class HUD {
    private static final int MARKER_SIZE = 20;
    private static final int MARKER_SPACING = 8;
    private static final int START_X = 16;
    private static final int START_Y = 16;
    private static final int MANA_BAR_WIDTH = 160;
    private static final int MANA_BAR_HEIGHT = 14;
    private static final int MANA_BAR_Y = START_Y + MARKER_SIZE + 18;
    private static final int INVENTORY_Y = MANA_BAR_Y + MANA_BAR_HEIGHT + 18;

    private final Player player;

    public HUD(Player player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    public void render(Graphics2D g2d) {
        for (int index = 0; index < player.getMaxHealth(); index++) {
            int markerX = START_X + (index * (MARKER_SIZE + MARKER_SPACING));
            int markerY = START_Y;
            boolean filled = index < player.getCurrentHealth();

            g2d.setColor(filled ? new Color(230, 92, 125) : new Color(82, 84, 96));
            g2d.fillRoundRect(markerX, markerY, MARKER_SIZE, MARKER_SIZE, 8, 8);

            g2d.setColor(new Color(245, 228, 235));
            g2d.drawRoundRect(markerX, markerY, MARKER_SIZE, MARKER_SIZE, 8, 8);
        }

        drawManaBar(g2d);
        drawInventory(g2d);
    }

    private void drawManaBar(Graphics2D g2d) {
        int filledWidth = (int) Math.round(
                MANA_BAR_WIDTH * (player.getCurrentMana() / (double) player.getMaxMana())
        );

        g2d.setColor(new Color(36, 53, 78));
        g2d.fillRoundRect(START_X, MANA_BAR_Y, MANA_BAR_WIDTH, MANA_BAR_HEIGHT, 8, 8);

        if (filledWidth > 0) {
            g2d.setColor(new Color(79, 180, 237));
            g2d.fillRoundRect(START_X, MANA_BAR_Y, filledWidth, MANA_BAR_HEIGHT, 8, 8);
        }

        g2d.setColor(new Color(203, 238, 255));
        g2d.drawRoundRect(START_X, MANA_BAR_Y, MANA_BAR_WIDTH, MANA_BAR_HEIGHT, 8, 8);
    }

    private void drawInventory(Graphics2D g2d) {
        Font previousFont = g2d.getFont();
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        try {
            drawPotionCounter(
                    g2d,
                    START_X,
                    INVENTORY_Y,
                    new Color(224, 82, 107),
                    new Color(255, 216, 222),
                    "1",
                    player.getHealthPotionCount()
            );
            drawPotionCounter(
                    g2d,
                    START_X + 74,
                    INVENTORY_Y,
                    new Color(70, 154, 232),
                    new Color(214, 244, 255),
                    "2",
                    player.getManaPotionCount()
            );
            g2d.setColor(new Color(255, 226, 123));
            g2d.drawString("Fragmentos: " + player.getCollectibleCount(), START_X + 148, INVENTORY_Y + 14);
        } finally {
            g2d.setFont(previousFont);
        }
    }

    private void drawPotionCounter(
            Graphics2D g2d,
            int x,
            int y,
            Color bottleColor,
            Color textColor,
            String key,
            int count
    ) {
        g2d.setColor(bottleColor);
        g2d.fillRoundRect(x, y, 20, 18, 7, 7);
        g2d.setColor(textColor);
        g2d.drawString(key + " x" + count, x + 26, y + 14);
    }
}
