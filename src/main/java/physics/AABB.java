package physics;

public final class AABB {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public AABB(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getLeft() {
        return x;
    }

    public double getTop() {
        return y;
    }

    public double getRight() {
        return x + width;
    }

    public double getBottom() {
        return y + height;
    }

    public boolean intersects(AABB other) {
        return getRight() > other.getLeft()
                && getLeft() < other.getRight()
                && getBottom() > other.getTop()
                && getTop() < other.getBottom();
    }
}
