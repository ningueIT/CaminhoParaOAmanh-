package entities;

import physics.AABB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

public final class RuneSymbol implements DialogInteractable {
    public enum Rune {
        MOON("lua", new Color(178, 209, 255)),
        LEAF("folha", new Color(141, 222, 142)),
        SUN("sol", new Color(255, 208, 108));

        private final String displayName;
        private final Color color;

        Rune(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getColor() {
            return color;
        }
    }

    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Rune rune;
    private final int order;

    public RuneSymbol(double x, double y, double width, double height, Rune rune, int order) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Rune dimensions must be greater than zero.");
        }
        if (order <= 0) {
            throw new IllegalArgumentException("Rune order must be greater than zero.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rune = Objects.requireNonNull(rune, "rune");
        this.order = order;
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
        return "A imagem " + order + " revela o simbolo da " + rune.getDisplayName() + ".";
    }

    public Rune getRune() {
        return rune;
    }

    public int getOrder() {
        return order;
    }

    public void render(Graphics2D g2d) {
        int renderX = (int) Math.round(x);
        int renderY = (int) Math.round(y);
        int renderWidth = (int) Math.round(width);
        int renderHeight = (int) Math.round(height);
        int inset = Math.max(4, renderWidth / 7);

        g2d.setColor(new Color(50, 45, 68));
        g2d.fillRoundRect(renderX, renderY, renderWidth, renderHeight, 12, 12);
        g2d.setColor(rune.getColor());
        g2d.drawRoundRect(renderX, renderY, renderWidth, renderHeight, 12, 12);

        int symbolSize = Math.min(renderWidth, renderHeight) - inset * 2;
        int symbolX = renderX + (renderWidth - symbolSize) / 2;
        int symbolY = renderY + (renderHeight - symbolSize) / 2;
        g2d.setColor(rune.getColor());
        switch (rune) {
            case MOON -> {
                g2d.fillOval(symbolX, symbolY, symbolSize, symbolSize);
                g2d.setColor(new Color(50, 45, 68));
                g2d.fillOval(symbolX + symbolSize / 3, symbolY - 2, symbolSize, symbolSize);
            }
            case LEAF -> {
                g2d.fillOval(symbolX + symbolSize / 5, symbolY, symbolSize * 3 / 5, symbolSize);
                g2d.setColor(new Color(50, 45, 68));
                g2d.drawLine(symbolX + symbolSize / 2, symbolY + 3, symbolX + symbolSize / 2, symbolY + symbolSize - 3);
            }
            case SUN -> {
                g2d.fillOval(symbolX + symbolSize / 4, symbolY + symbolSize / 4, symbolSize / 2, symbolSize / 2);
                for (int index = 0; index < 8; index++) {
                    double angle = (Math.PI * 2.0 * index) / 8.0;
                    int centerX = symbolX + symbolSize / 2;
                    int centerY = symbolY + symbolSize / 2;
                    int startX = centerX + (int) Math.round(Math.cos(angle) * symbolSize * 0.36);
                    int startY = centerY + (int) Math.round(Math.sin(angle) * symbolSize * 0.36);
                    int endX = centerX + (int) Math.round(Math.cos(angle) * symbolSize * 0.50);
                    int endY = centerY + (int) Math.round(Math.sin(angle) * symbolSize * 0.50);
                    g2d.drawLine(startX, startY, endX, endY);
                }
            }
        }
    }
}
