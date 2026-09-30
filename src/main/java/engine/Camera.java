package engine;

import entities.Entity;

import java.util.concurrent.ThreadLocalRandom;

public final class Camera {
    private static final double LERP_FACTOR = 0.12;
    private static final double DEFAULT_UPDATE_SECONDS = 1.0 / 60.0;

    private final double viewportWidth;
    private final double viewportHeight;
    private final double worldWidth;
    private final double worldHeight;

    private volatile double x;
    private volatile double y;
    private double trackedX;
    private double trackedY;
    private double shakeIntensity;
    private double shakeRemainingSeconds;
    private boolean initialized;

    public Camera(double viewportWidth, double viewportHeight, double worldWidth, double worldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void update(Entity target) {
        update(target, DEFAULT_UPDATE_SECONDS);
    }

    public void update(Entity target, double deltaSeconds) {
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative.");
        }

        double desiredX = target.getX() + (target.getWidth() * 0.5) - (viewportWidth * 0.5);
        double desiredY = target.getY() + (target.getHeight() * 0.5) - (viewportHeight * 0.5);

        desiredX = clamp(desiredX, 0.0, Math.max(0.0, worldWidth - viewportWidth));
        desiredY = clamp(desiredY, 0.0, Math.max(0.0, worldHeight - viewportHeight));

        if (!initialized) {
            trackedX = desiredX;
            trackedY = desiredY;
            initialized = true;
        } else {
            // Lerp suaviza a perseguicao da camera sem perder o alvo do centro da tela.
            trackedX += (desiredX - trackedX) * LERP_FACTOR;
            trackedY += (desiredY - trackedY) * LERP_FACTOR;

            trackedX = clamp(trackedX, 0.0, Math.max(0.0, worldWidth - viewportWidth));
            trackedY = clamp(trackedY, 0.0, Math.max(0.0, worldHeight - viewportHeight));
        }

        applyShake(deltaSeconds);
    }

    public void shake(double intensity, double durationSeconds) {
        if (intensity <= 0.0 || durationSeconds <= 0.0) {
            throw new IllegalArgumentException("Shake intensity and duration must be greater than zero.");
        }

        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeRemainingSeconds = Math.max(shakeRemainingSeconds, durationSeconds);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private void applyShake(double deltaSeconds) {
        if (shakeRemainingSeconds <= 0.0) {
            x = trackedX;
            y = trackedY;
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        x = clamp(
                trackedX + random.nextDouble(-shakeIntensity, shakeIntensity),
                0.0,
                Math.max(0.0, worldWidth - viewportWidth)
        );
        y = clamp(
                trackedY + random.nextDouble(-shakeIntensity, shakeIntensity),
                0.0,
                Math.max(0.0, worldHeight - viewportHeight)
        );

        shakeRemainingSeconds = Math.max(0.0, shakeRemainingSeconds - deltaSeconds);
        if (shakeRemainingSeconds == 0.0) {
            shakeIntensity = 0.0;
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
