package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class CorruptionZone {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final double lifetimeSeconds;
    private final double telegraphSeconds;
    private double remainingSeconds;
    private double remainingTelegraphSeconds;

    public CorruptionZone(double x, double y, double width, double height, double lifetimeSeconds) {
        this(x, y, width, height, lifetimeSeconds, 0.0);
    }

    public CorruptionZone(
            double x,
            double y,
            double width,
            double height,
            double lifetimeSeconds,
            double telegraphSeconds
    ) {
        if (width <= 0.0 || height <= 0.0 || lifetimeSeconds <= 0.0) {
            throw new IllegalArgumentException("Zone dimensions and lifetime must be greater than zero.");
        }
        if (telegraphSeconds < 0.0) {
            throw new IllegalArgumentException("telegraphSeconds must not be negative.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.lifetimeSeconds = lifetimeSeconds;
        this.telegraphSeconds = telegraphSeconds;
        this.remainingSeconds = lifetimeSeconds;
        this.remainingTelegraphSeconds = telegraphSeconds;
    }

    public void fixedUpdate(double deltaSeconds) {
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative.");
        }
        if (remainingTelegraphSeconds > 0.0) {
            remainingTelegraphSeconds = Math.max(0.0, remainingTelegraphSeconds - deltaSeconds);
            return;
        }

        remainingSeconds = Math.max(0.0, remainingSeconds - deltaSeconds);
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean isActive() {
        return remainingTelegraphSeconds > 0.0 || remainingSeconds > 0.0;
    }

    public boolean isDangerous() {
        return remainingTelegraphSeconds == 0.0 && remainingSeconds > 0.0;
    }

    public void render(Graphics2D g2d) {
        if (!isActive()) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        if (!isDangerous()) {
            double pulse = 0.60 + 0.40 * Math.sin(remainingTelegraphSeconds * 16.0);
            int alpha = (int) Math.round(55 + pulse * 75);
            g2d.setColor(new Color(255, 205, 104, alpha));
            g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);
            g2d.setColor(new Color(255, 237, 165, Math.min(255, alpha + 75)));
            g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);
            return;
        }

        double pulse = 0.55 + 0.45 * Math.sin(remainingSeconds * 14.0);
        int alpha = (int) Math.round(85 + pulse * 95);
        g2d.setColor(new Color(91, 41, 105, alpha));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);
        g2d.setColor(new Color(225, 88, 179, Math.min(255, alpha + 55)));
        g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);
    }

    public double getOpacity() {
        if (telegraphSeconds > 0.0 && remainingTelegraphSeconds > 0.0) {
            return remainingTelegraphSeconds / telegraphSeconds;
        }
        return remainingSeconds / lifetimeSeconds;
    }
}
