package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class BossProjectile {
    private static final double SIZE = 20.0;

    private double x;
    private double y;
    private final double velocityX;
    private final double velocityY;
    private boolean active = true;

    public BossProjectile(double x, double y, double velocityX, double velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void fixedUpdate(double deltaSeconds) {
        if (!active) {
            return;
        }

        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
    }

    public AABB getBounds() {
        return new AABB(x, y, SIZE, SIZE);
    }

    public boolean isOutsideWorld(double worldWidth, double worldHeight) {
        return x + SIZE < 0.0 || x > worldWidth || y + SIZE < 0.0 || y > worldHeight;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public void render(Graphics2D g2d) {
        if (!active) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int size = (int) Math.round(SIZE);
        g2d.setColor(new Color(73, 36, 94));
        g2d.fillOval(renderX, renderY, size, size);
        g2d.setColor(new Color(234, 101, 191));
        g2d.fillOval(renderX + 4, renderY + 4, size - 8, size - 8);
    }
}
