package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class Collectible {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean collected;

    public Collectible(double x, double y, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Collectible dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean tryCollect(Player player) {
        if (collected) {
            return false;
        }

        collected = true;
        player.addCollectible();
        return true;
    }

    public boolean isCollected() {
        return collected;
    }

    public void markCollected() {
        collected = true;
    }

    public void render(Graphics2D g2d) {
        if (collected) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);
        int centerX = renderX + renderWidth / 2;
        int centerY = renderY + renderHeight / 2;

        g2d.setColor(new Color(255, 213, 102));
        int[] xPoints = {centerX, renderX + renderWidth, centerX, renderX};
        int[] yPoints = {renderY, centerY, renderY + renderHeight, centerY};
        g2d.fillPolygon(xPoints, yPoints, 4);
        g2d.setColor(new Color(255, 245, 190));
        g2d.fillOval(centerX - Math.max(2, renderWidth / 7), centerY - Math.max(2, renderHeight / 7), Math.max(4, renderWidth / 4), Math.max(4, renderHeight / 4));
    }
}
