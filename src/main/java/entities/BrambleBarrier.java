package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class BrambleBarrier {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean burned;

    public BrambleBarrier(double x, double y, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Bramble dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean burn() {
        if (burned) {
            return false;
        }

        burned = true;
        return true;
    }

    public boolean isBurned() {
        return burned;
    }

    public double getCenterX() {
        return x + width * 0.5;
    }

    public double getCenterY() {
        return y + height * 0.5;
    }

    public void render(Graphics2D g2d) {
        if (burned) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);
        int vineCount = Math.max(3, renderWidth / 8);

        g2d.setColor(new Color(51, 72, 43));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 14, 14);
        for (int index = 0; index < vineCount; index++) {
            int vineX = renderX + (index * renderWidth) / vineCount + 2;
            int bend = index % 2 == 0 ? 8 : -8;
            g2d.setColor(index % 2 == 0 ? new Color(87, 126, 63) : new Color(63, 99, 57));
            g2d.drawLine(vineX, renderY + renderHeight, vineX + bend, renderY + renderHeight / 2);
            g2d.drawLine(vineX + bend, renderY + renderHeight / 2, vineX, renderY);
        }

        g2d.setColor(new Color(160, 79, 111));
        int thornSize = Math.max(3, renderWidth / 7);
        for (int thornY = renderY + 10; thornY < renderY + renderHeight; thornY += 24) {
            g2d.fillOval(renderX - thornSize / 2, thornY, thornSize, thornSize);
            g2d.fillOval(renderX + renderWidth - thornSize / 2, thornY + 9, thornSize, thornSize);
        }
    }
}
