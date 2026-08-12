package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class Gate {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private boolean open;

    public Gate(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void render(Graphics2D g2d) {
        if (open) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        g2d.setColor(new Color(86, 95, 115));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 8, 8);

        g2d.setColor(new Color(158, 166, 184));
        int barSpacing = Math.max(8, renderWidth / 3);
        for (int offset = barSpacing; offset < renderWidth; offset += barSpacing) {
            g2d.drawLine(renderX + offset, renderY + 4, renderX + offset, renderY + renderHeight - 4);
        }

        g2d.drawLine(renderX + 4, renderY + 14, renderX + renderWidth - 4, renderY + 14);
        g2d.drawLine(renderX + 4, renderY + renderHeight - 14, renderX + renderWidth - 4, renderY + renderHeight - 14);
    }
}
