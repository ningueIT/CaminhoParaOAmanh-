package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class LightBeacon {
    private static final float LIGHT_RADIUS = 220.0f;

    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean lit;

    public LightBeacon(double x, double y, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Beacon dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean ignite() {
        if (lit) {
            return false;
        }

        lit = true;
        return true;
    }

    public boolean isLit() {
        return lit;
    }

    public double getLightX() {
        return x + width * 0.5;
    }

    public double getLightY() {
        return y + height * 0.28;
    }

    public float getLightRadius() {
        return LIGHT_RADIUS;
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);
        int flameSize = Math.max(10, renderWidth * 2 / 3);
        int flameX = renderX + (renderWidth - flameSize) / 2;
        int flameY = renderY + renderHeight / 8;

        g2d.setColor(new Color(73, 57, 51));
        g2d.fillRoundRect(
                renderX + renderWidth / 2 - Math.max(3, renderWidth / 8),
                renderY + renderHeight / 3,
                Math.max(6, renderWidth / 4),
                renderHeight * 2 / 3,
                6,
                6
        );
        g2d.setColor(new Color(126, 104, 73));
        g2d.fillRoundRect(renderX + renderWidth / 6, renderY + renderHeight * 3 / 4, renderWidth * 2 / 3, 8, 6, 6);

        if (lit) {
            g2d.setColor(new Color(255, 204, 92, 90));
            g2d.fillOval(flameX - flameSize / 2, flameY - flameSize / 2, flameSize * 2, flameSize * 2);
            g2d.setColor(new Color(255, 224, 123));
            g2d.fillOval(flameX, flameY, flameSize, flameSize);
            g2d.setColor(new Color(255, 248, 204));
            g2d.fillOval(flameX + flameSize / 3, flameY + flameSize / 4, flameSize / 3, flameSize / 2);
            return;
        }

        g2d.setColor(new Color(78, 97, 133));
        g2d.fillOval(flameX, flameY, flameSize, flameSize);
        g2d.setColor(new Color(138, 165, 197));
        g2d.drawOval(flameX, flameY, flameSize, flameSize);
    }
}
