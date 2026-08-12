package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class MagicProjectile {
    public static final double WIDTH = 20.0;
    public static final double HEIGHT = 14.0;
    private static final double SPEED = 620.0;

    private double x;
    private final double y;
    private final int direction;
    private final int damage;
    private boolean active = true;

    public MagicProjectile(double x, double y, int direction, int damage) {
        if (damage <= 0) {
            throw new IllegalArgumentException("damage must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.direction = direction >= 0 ? 1 : -1;
        this.damage = damage;
    }

    public void fixedUpdate(double deltaSeconds) {
        if (!active) {
            return;
        }

        x += direction * SPEED * deltaSeconds;
    }

    public AABB getBounds() {
        return new AABB(x, y, WIDTH, HEIGHT);
    }

    public int getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOutsideWorld(double worldWidth) {
        return x + WIDTH < 0.0 || x > worldWidth;
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
        int renderWidth = (int) Math.round(WIDTH);
        int renderHeight = (int) Math.round(HEIGHT);

        g2d.setColor(new Color(102, 221, 255));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, renderHeight, renderHeight);

        g2d.setColor(new Color(232, 252, 255));
        int shineX = direction > 0 ? renderX + 4 : renderX + renderWidth - 8;
        g2d.fillOval(shineX, renderY + 3, 5, 5);
    }
}
