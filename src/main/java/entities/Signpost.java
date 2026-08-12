package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;

public final class Signpost implements DialogInteractable {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final String message;

    public Signpost(double x, double y, double width, double height, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = message;
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
        int postWidth = Math.max(8, renderWidth / 4);
        int postX = renderX + (renderWidth - postWidth) / 2;
        int postTop = renderY + renderHeight / 2;
        int postHeight = renderHeight / 2;

        g2d.setColor(new Color(117, 80, 49));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight / 2, 8, 8);

        g2d.setColor(new Color(79, 54, 34));
        g2d.fillRect(postX, postTop, postWidth, postHeight);

        g2d.setColor(new Color(226, 214, 179));
        g2d.fillRect(renderX + 8, renderY + 8, renderWidth - 16, 4);
        g2d.fillRect(renderX + 8, renderY + 18, renderWidth - 20, 4);
    }
}
