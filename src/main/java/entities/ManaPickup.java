package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class ManaPickup {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final int restoreAmount;
    private boolean collected;

    public ManaPickup(double x, double y, double width, double height, int restoreAmount) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Pickup dimensions must be greater than zero.");
        }
        if (restoreAmount <= 0) {
            throw new IllegalArgumentException("restoreAmount must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.restoreAmount = restoreAmount;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean tryCollect(Player player) {
        if (collected || player.restoreMana(restoreAmount) == 0) {
            return false;
        }

        collected = true;
        return true;
    }

    public boolean isCollected() {
        return collected;
    }

    public void render(Graphics2D g2d) {
        if (collected) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        g2d.setColor(new Color(54, 122, 186));
        g2d.fillOval(renderX, renderY, renderWidth, renderHeight);

        g2d.setColor(new Color(175, 238, 255));
        g2d.fillOval(renderX + renderWidth / 4, renderY + renderHeight / 5, renderWidth / 3, renderHeight / 3);
    }
}
