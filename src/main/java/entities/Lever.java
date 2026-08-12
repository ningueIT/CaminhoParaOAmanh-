package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

public final class Lever implements Interactable {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Gate targetGate;
    private boolean pulled;

    public Lever(double x, double y, double width, double height, Gate targetGate) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetGate = Objects.requireNonNull(targetGate, "targetGate");
    }

    @Override
    public AABB getInteractionBounds() {
        return new AABB(x, y, width, height);
    }

    @Override
    public void onInteract(Player player) {
        if (pulled) {
            return;
        }

        pulled = true;
        targetGate.open();
    }

    public boolean isPulled() {
        return pulled;
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);

        g2d.setColor(new Color(68, 56, 42));
        g2d.fillRoundRect(renderX + (renderWidth / 3), renderY + renderHeight - 10, renderWidth / 3, 10, 6, 6);

        g2d.setColor(pulled ? new Color(132, 214, 122) : new Color(227, 190, 73));
        int handleStartX = renderX + (renderWidth / 2);
        int handleStartY = renderY + renderHeight - 10;
        int handleEndX = pulled ? handleStartX + 10 : handleStartX - 10;
        int handleEndY = renderY + 8;
        g2d.drawLine(handleStartX, handleStartY, handleEndX, handleEndY);
        g2d.fillOval(handleEndX - 4, handleEndY - 4, 8, 8);
    }
}
