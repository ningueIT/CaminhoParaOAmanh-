package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

public final class PotionPickup {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final PotionType type;
    private boolean collected;

    public PotionPickup(double x, double y, double width, double height, PotionType type) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Potion dimensions must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = Objects.requireNonNull(type, "type");
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public PotionType getType() {
        return type;
    }

    public boolean tryCollect(Player player) {
        if (collected) {
            return false;
        }

        boolean addedToInventory = switch (type) {
            case HEALTH -> player.addHealthPotion();
            case MANA -> player.addManaPotion();
        };
        if (!addedToInventory) {
            return false;
        }

        collected = true;
        return true;
    }

    public boolean isCollected() {
        return collected;
    }

    public void markCollected() {
        collected = true;
    }

    public void render(Graphics2D g2d) {
        if (collected) {
            return;
        }

        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);
        int neckWidth = Math.max(5, renderWidth / 3);
        int neckX = renderX + (renderWidth - neckWidth) / 2;
        int neckHeight = Math.max(5, renderHeight / 4);

        g2d.setColor(new Color(244, 238, 218));
        g2d.fillRoundRect(neckX, renderY, neckWidth, neckHeight + 3, 4, 4);
        g2d.setColor(type.getBottleColor());
        g2d.fillRoundRect(
                renderX,
                renderY + neckHeight,
                renderWidth,
                renderHeight - neckHeight,
                Math.max(8, renderWidth / 2),
                Math.max(8, renderWidth / 2)
        );
        g2d.setColor(type.getHighlightColor());
        g2d.fillOval(
                renderX + renderWidth / 4,
                renderY + neckHeight + renderHeight / 5,
                Math.max(4, renderWidth / 4),
                Math.max(4, renderHeight / 4)
        );
    }
}
