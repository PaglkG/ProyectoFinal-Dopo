package domain;

import java.util.Objects;

/** Immutable ball state exposed by session inspection. */
public final class BallView {
    private final int index;
    private final double x;
    private final double y;
    private final double radius;
    private final double velocityX;
    private final double velocityY;

    public BallView(int index, double x, double y, double radius, double velocityX, double velocityY) {
        this.index = index; this.x = x; this.y = y; this.radius = radius;
        this.velocityX = velocityX; this.velocityY = velocityY;
    }
    public int index() { return index; }
    public double x() { return x; }
    public double y() { return y; }
    public double radius() { return radius; }
    public double velocityX() { return velocityX; }
    public double velocityY() { return velocityY; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BallView that)) return false;
        return index == that.index && Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0
                && Double.compare(radius, that.radius) == 0 && Double.compare(velocityX, that.velocityX) == 0
                && Double.compare(velocityY, that.velocityY) == 0;
    }
    @Override public int hashCode() { return Objects.hash(index, x, y, radius, velocityX, velocityY); }
    @Override public String toString() {
        return "BallView[index=" + index + ", x=" + x + ", y=" + y + ", radius=" + radius
                + ", velocityX=" + velocityX + ", velocityY=" + velocityY + "]";
    }
}
