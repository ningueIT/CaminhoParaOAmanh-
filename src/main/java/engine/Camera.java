package engine;

import entities.Entity;

public final class Camera {
    private static final double LERP_FACTOR = 0.12;

    private final double viewportWidth;
    private final double viewportHeight;
    private final double worldWidth;
    private final double worldHeight;

    private volatile double x;
    private volatile double y;
    private boolean initialized;

    public Camera(double viewportWidth, double viewportHeight, double worldWidth, double worldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void update(Entity target) {
        double desiredX = target.getX() + (target.getWidth() * 0.5) - (viewportWidth * 0.5);
        double desiredY = target.getY() + (target.getHeight() * 0.5) - (viewportHeight * 0.5);

        desiredX = clamp(desiredX, 0.0, Math.max(0.0, worldWidth - viewportWidth));
        desiredY = clamp(desiredY, 0.0, Math.max(0.0, worldHeight - viewportHeight));

        if (!initialized) {
            x = desiredX;
            y = desiredY;
            initialized = true;
            return;
        }

        // Lerp suaviza a perseguicao da camera sem perder o alvo do centro da tela.
        x += (desiredX - x) * LERP_FACTOR;
        y += (desiredY - y) * LERP_FACTOR;

        x = clamp(x, 0.0, Math.max(0.0, worldWidth - viewportWidth));
        y = clamp(y, 0.0, Math.max(0.0, worldHeight - viewportHeight));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
