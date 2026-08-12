package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class LevelExit {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public LevelExit(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        g2d.setColor(new Color(105, 76, 168));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 12, 12);

        g2d.setColor(new Color(191, 175, 235));
        g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 12, 12);

        g2d.setColor(new Color(245, 223, 108));
        g2d.fillOval(renderX + renderWidth - 14, renderY + (renderHeight / 2) - 4, 6, 6);
    }
}
