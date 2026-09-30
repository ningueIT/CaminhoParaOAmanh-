package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

public final class Particle {
    private final double lifetimeSeconds;
    private final Color color;
    private final int size;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double remainingLifetimeSeconds;

    public Particle(
            double x,
            double y,
            double velocityX,
            double velocityY,
            double lifetimeSeconds,
            Color color
    ) {
        this(x, y, velocityX, velocityY, lifetimeSeconds, color, 5);
    }

    public Particle(
            double x,
            double y,
            double velocityX,
            double velocityY,
            double lifetimeSeconds,
            Color color,
            int size
    ) {
        if (lifetimeSeconds <= 0.0) {
            throw new IllegalArgumentException("lifetimeSeconds must be greater than zero.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.lifetimeSeconds = lifetimeSeconds;
        this.remainingLifetimeSeconds = lifetimeSeconds;
        this.color = Objects.requireNonNull(color, "color");
        this.size = size;
    }

    public void fixedUpdate(double deltaSeconds) {
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative.");
        }
        if (!isAlive()) {
            return;
        }

        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
        remainingLifetimeSeconds = Math.max(0.0, remainingLifetimeSeconds - deltaSeconds);
    }

    public boolean isAlive() {
        return remainingLifetimeSeconds > 0.0;
    }

    public void render(Graphics2D g2d) {
        if (!isAlive()) {
            return;
        }

        int alpha = (int) Math.round(255.0 * (remainingLifetimeSeconds / lifetimeSeconds));
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.fillOval(
                (int) Math.round(x - size * 0.5),
                (int) Math.round(y - size * 0.5),
                size,
                size
        );
    }
}
