package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public final class Platform {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public Platform(double x, double y, double width, double height) {
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

        GradientPaint paint = new GradientPaint(
                renderX,
                renderY,
                new Color(98, 105, 117),
                renderX,
                renderY + renderHeight,
                new Color(45, 50, 61)
        );

        g2d.setPaint(paint);
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 10, 10);

        g2d.setColor(new Color(153, 161, 174));
        g2d.fillRoundRect(renderX, renderY, renderWidth, 6, 10, 10);
    }
}
