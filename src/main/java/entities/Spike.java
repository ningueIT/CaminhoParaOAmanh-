package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class Spike {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public Spike(double x, double y, double width, double height) {
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
        int spikeCount = Math.max(1, renderWidth / 18);
        int spikeWidth = Math.max(1, renderWidth / spikeCount);

        g2d.setColor(new Color(143, 150, 162));
        for (int index = 0; index < spikeCount; index++) {
            int left = renderX + (index * spikeWidth);
            int right = index == spikeCount - 1 ? renderX + renderWidth : left + spikeWidth;
            int centerX = left + ((right - left) / 2);

            int[] xPoints = {left, centerX, right};
            int[] yPoints = {renderY + renderHeight, renderY, renderY + renderHeight};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        g2d.setColor(new Color(92, 99, 109));
        g2d.fillRect(renderX, renderY + renderHeight - 4, renderWidth, 4);
    }
}
