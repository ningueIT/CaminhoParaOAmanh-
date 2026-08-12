package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Objects;

public final class DialogManager {
    private static final double CHARACTERS_PER_SECOND = 45.0;
    private static final int BOX_MARGIN_X = 40;
    private static final int BOX_MARGIN_BOTTOM = 32;
    private static final int BOX_HEIGHT = 160;
    private static final int BOX_PADDING = 20;
    private static final int LINE_HEIGHT = 24;
    private static final int MAX_LINES = 4;

    private String message = "";
    private int visibleCharacterCount;
    private double characterProgress;
    private boolean open;

    public void open(String message) {
        this.message = Objects.requireNonNull(message, "message");
        if (message.isBlank()) {
            throw new IllegalArgumentException("Dialog message must not be blank.");
        }

        visibleCharacterCount = 0;
        characterProgress = 0.0;
        open = true;
    }

    public void fixedUpdate(double deltaSeconds) {
        if (!open || visibleCharacterCount == message.length()) {
            return;
        }
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative.");
        }

        characterProgress += deltaSeconds * CHARACTERS_PER_SECOND;
        int charactersToReveal = (int) characterProgress;
        if (charactersToReveal == 0) {
            return;
        }

        visibleCharacterCount = Math.min(message.length(), visibleCharacterCount + charactersToReveal);
        characterProgress -= charactersToReveal;
    }

    public void advance() {
        if (!open) {
            return;
        }

        if (visibleCharacterCount < message.length()) {
            visibleCharacterCount = message.length();
            characterProgress = 0.0;
            return;
        }

        close();
    }

    public void close() {
        open = false;
        visibleCharacterCount = 0;
        characterProgress = 0.0;
        message = "";
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isMessageFullyVisible() {
        return open && visibleCharacterCount == message.length();
    }

    public String getVisibleMessage() {
        return message.substring(0, visibleCharacterCount);
    }

    public void render(Graphics2D g2d, int panelWidth, int panelHeight) {
        if (!open) {
            return;
        }

        int boxX = BOX_MARGIN_X;
        int boxY = panelHeight - BOX_MARGIN_BOTTOM - BOX_HEIGHT;
        int boxWidth = panelWidth - BOX_MARGIN_X * 2;

        g2d.setColor(new Color(11, 15, 28, 230));
        g2d.fillRoundRect(boxX, boxY, boxWidth, BOX_HEIGHT, 18, 18);

        g2d.setColor(new Color(166, 202, 240));
        g2d.drawRoundRect(boxX, boxY, boxWidth, BOX_HEIGHT, 18, 18);

        Font previousFont = g2d.getFont();
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        try {
            FontMetrics metrics = g2d.getFontMetrics();
            g2d.setColor(new Color(242, 245, 255));
            drawWrappedText(
                    g2d,
                    metrics,
                    getVisibleMessage(),
                    boxX + BOX_PADDING,
                    boxY + BOX_PADDING + metrics.getAscent(),
                    boxWidth - BOX_PADDING * 2
            );

            if (isMessageFullyVisible()) {
                g2d.setColor(new Color(174, 211, 245));
                g2d.drawString("[E]", boxX + boxWidth - BOX_PADDING - 28, boxY + BOX_HEIGHT - BOX_PADDING);
            }
        } finally {
            g2d.setFont(previousFont);
        }
    }

    private void drawWrappedText(
            Graphics2D g2d,
            FontMetrics metrics,
            String text,
            int startX,
            int startY,
            int maxWidth
    ) {
        StringBuilder line = new StringBuilder();
        int lineY = startY;
        int renderedLines = 0;

        for (int index = 0; index < text.length() && renderedLines < MAX_LINES; index++) {
            char character = text.charAt(index);
            if (character == '\n') {
                g2d.drawString(line.toString(), startX, lineY);
                line.setLength(0);
                lineY += LINE_HEIGHT;
                renderedLines++;
                continue;
            }

            line.append(character);
            if (metrics.stringWidth(line.toString()) <= maxWidth) {
                continue;
            }

            int breakIndex = line.lastIndexOf(" ");
            if (breakIndex > 0) {
                g2d.drawString(line.substring(0, breakIndex), startX, lineY);
                line.delete(0, breakIndex + 1);
            } else {
                g2d.drawString(line.substring(0, line.length() - 1), startX, lineY);
                char overflowCharacter = line.charAt(line.length() - 1);
                line.setLength(0);
                line.append(overflowCharacter);
            }

            lineY += LINE_HEIGHT;
            renderedLines++;
        }

        if (renderedLines < MAX_LINES && !line.isEmpty()) {
            g2d.drawString(line.toString(), startX, lineY);
        }
    }
}
