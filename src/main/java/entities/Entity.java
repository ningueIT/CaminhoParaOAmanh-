package entities;

import physics.AABB;

import java.awt.Graphics2D;

public abstract class Entity {
    private double previousX;
    private double previousY;
    private double x;
    private double y;
    private final double width;
    private final double height;
    private double velocityX;
    private double velocityY;
    private boolean onGround;

    protected Entity(double x, double y, double width, double height) {
        this.x = x;
        this.previousX = x;
        this.y = y;
        this.previousY = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(Graphics2D g2d, double alpha);

    protected void beginFixedUpdate() {
        previousX = x;
        previousY = y;
    }

    protected void integrate(double deltaSeconds) {
        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
    }

    public AABB getBounds() {
        return new AABB(x, y, width, height);
    }

    public double getRenderX(double alpha) {
        return previousX + (x - previousX) * alpha;
    }

    public double getRenderY(double alpha) {
        return previousY + (y - previousY) * alpha;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
