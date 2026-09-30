package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class ForestWatcher {
    private static final double VISION_RANGE = 310.0;
    private static final double VISION_HALF_HEIGHT = 100.0;

    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final int direction;

    public ForestWatcher(double x, double y, double width, double height, int direction) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Watcher dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.direction = direction >= 0 ? 1 : -1;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public boolean canSee(Player player) {
        AABB playerBounds = player.getBounds();
        double watcherCenterX = x + width * 0.5;
        double watcherCenterY = y + height * 0.5;
        double playerCenterX = (playerBounds.getLeft() + playerBounds.getRight()) * 0.5;
        double playerCenterY = (playerBounds.getTop() + playerBounds.getBottom()) * 0.5;
        double horizontalDistance = playerCenterX - watcherCenterX;

        return horizontalDistance * direction > 0.0
                && Math.abs(horizontalDistance) <= VISION_RANGE
                && Math.abs(playerCenterY - watcherCenterY) <= VISION_HALF_HEIGHT;
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);
        int eyeCenterX = direction > 0 ? renderX + renderWidth * 2 / 3 : renderX + renderWidth / 3;
        int eyeCenterY = renderY + renderHeight / 3;

        g2d.setColor(new Color(40, 32, 60));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 14, 14);
        g2d.setColor(new Color(115, 83, 138));
        g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 14, 14);
        g2d.setColor(new Color(246, 193, 92));
        g2d.fillOval(eyeCenterX - 7, eyeCenterY - 7, 14, 14);
        g2d.setColor(new Color(45, 24, 53));
        g2d.fillOval(eyeCenterX - 2, eyeCenterY - 6, 4, 12);
    }
}
