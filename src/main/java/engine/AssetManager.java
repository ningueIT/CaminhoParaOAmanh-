package engine;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AssetManager {
    private static final ConcurrentMap<String, BufferedImage> IMAGES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<PlaceholderKey, BufferedImage> PLACEHOLDERS = new ConcurrentHashMap<>();

    private AssetManager() {
    }

    public static BufferedImage loadImage(String assetPath) {
        validateAssetPath(assetPath);
        return IMAGES.computeIfAbsent(assetPath, AssetManager::readImage);
    }

    public static BufferedImage loadOrPlaceholder(
            String assetPath,
            String placeholderId,
            int width,
            int height,
            Color color
    ) {
        validateAssetPath(assetPath);
        if (isAvailable(assetPath)) {
            return loadImage(assetPath);
        }

        return getPlaceholder(placeholderId, width, height, color);
    }

    public static BufferedImage getPlaceholder(String placeholderId, int width, int height, Color color) {
        Objects.requireNonNull(placeholderId, "placeholderId");
        Objects.requireNonNull(color, "color");
        if (placeholderId.isBlank()) {
            throw new IllegalArgumentException("placeholderId must not be blank.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Placeholder dimensions must be greater than zero.");
        }

        PlaceholderKey key = new PlaceholderKey(placeholderId, width, height, color.getRGB());
        return PLACEHOLDERS.computeIfAbsent(key, AssetManager::createPlaceholder);
    }

    private static boolean isAvailable(String assetPath) {
        return findResource(assetPath) != null || Files.isRegularFile(Path.of(assetPath));
    }

    private static BufferedImage readImage(String assetPath) {
        URL resource = findResource(assetPath);
        if (resource != null) {
            return readImage(resource, assetPath);
        }

        File file = Path.of(assetPath).toFile();
        if (file.isFile()) {
            return readImage(file, assetPath);
        }

        throw new IllegalArgumentException("Image asset not found: " + assetPath);
    }

    private static BufferedImage readImage(URL resource, String assetPath) {
        try {
            BufferedImage image = ImageIO.read(resource);
            return requireDecodedImage(image, assetPath);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read image asset: " + assetPath, exception);
        }
    }

    private static BufferedImage readImage(File file, String assetPath) {
        try {
            BufferedImage image = ImageIO.read(file);
            return requireDecodedImage(image, assetPath);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read image asset: " + assetPath, exception);
        }
    }

    private static BufferedImage requireDecodedImage(BufferedImage image, String assetPath) {
        if (image == null) {
            throw new IllegalArgumentException("Unsupported image format: " + assetPath);
        }

        return image;
    }

    private static URL findResource(String assetPath) {
        String resourcePath = assetPath.startsWith("/") ? assetPath : "/" + assetPath;
        return AssetManager.class.getResource(resourcePath);
    }

    private static BufferedImage createPlaceholder(PlaceholderKey key) {
        BufferedImage image = new BufferedImage(key.width(), key.height(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            Color fillColor = new Color(key.colorArgb(), true);
            graphics.setColor(fillColor);
            graphics.fillRect(0, 0, key.width(), key.height());
            graphics.setColor(fillColor.darker());
            graphics.drawRect(0, 0, key.width() - 1, key.height() - 1);
        } finally {
            graphics.dispose();
        }

        return image;
    }

    private static void validateAssetPath(String assetPath) {
        Objects.requireNonNull(assetPath, "assetPath");
        if (assetPath.isBlank()) {
            throw new IllegalArgumentException("assetPath must not be blank.");
        }
    }

    private record PlaceholderKey(String id, int width, int height, int colorArgb) {
    }
}
