package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class MemoryKey {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean collected;

    public MemoryKey(double x, double y, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Key dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean collect() {
        if (collected) {
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
        int ringSize = Math.max(8, renderHeight / 2);

        g2d.setColor(new Color(255, 221, 108));
        g2d.fillOval(renderX, renderY, ringSize, ringSize);
        g2d.setColor(new Color(95, 70, 38));
        g2d.fillOval(renderX + ringSize / 3, renderY + ringSize / 3, ringSize / 3, ringSize / 3);
        g2d.setColor(new Color(255, 232, 148));
        g2d.fillRoundRect(
                renderX + ringSize - 3,
                renderY + ringSize / 2 - 3,
                Math.max(6, renderWidth - ringSize + 3),
                6,
                4,
                4
        );
        g2d.fillRect(renderX + renderWidth - 7, renderY + ringSize / 2, 5, Math.max(5, renderHeight / 3));
    }
}
