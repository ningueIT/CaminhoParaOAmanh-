package entities;

import java.awt.Color;

public enum PotionType {
    HEALTH(new Color(224, 82, 107), new Color(255, 186, 199)),
    MANA(new Color(70, 154, 232), new Color(173, 231, 255));

    private final Color bottleColor;
    private final Color highlightColor;

    PotionType(Color bottleColor, Color highlightColor) {
        this.bottleColor = bottleColor;
        this.highlightColor = highlightColor;
    }

    public Color getBottleColor() {
        return bottleColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }
}
