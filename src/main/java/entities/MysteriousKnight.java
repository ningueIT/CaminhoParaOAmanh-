package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

public final class MysteriousKnight implements DialogInteractable {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final String message;

    public MysteriousKnight(double x, double y, double width, double height, String message) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Knight dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = Objects.requireNonNull(message, "message");
    }

    @Override
    public AABB getInteractionBounds() {
        return new AABB(x, y, width, height);
    }

    @Override
    public void onInteract(Player player) {
    }

    @Override
    public String getDialogMessage() {
        return message;
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        g2d.setColor(new Color(47, 52, 76));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 14, 14);

        g2d.setColor(new Color(153, 167, 193));
        g2d.fillRoundRect(renderX + renderWidth / 5, renderY + 8, (renderWidth * 3) / 5, renderHeight / 3, 12, 12);

        g2d.setColor(new Color(237, 215, 150));
        g2d.fillRect(renderX + renderWidth / 3, renderY + renderHeight / 4, renderWidth / 8, 6);
        g2d.fillRect(renderX + (renderWidth * 13) / 24, renderY + renderHeight / 4, renderWidth / 8, 6);

        g2d.setColor(new Color(31, 29, 46));
        g2d.fillRoundRect(renderX + renderWidth / 4, renderY + renderHeight / 2, renderWidth / 2, renderHeight / 3, 8, 8);
    }
}
