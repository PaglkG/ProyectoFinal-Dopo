package domain;

import java.util.Objects;

/** Immutable rectangle exposed by session inspection. */
public final class RectangleView {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public RectangleView(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public double x() { return x; }
    public double y() { return y; }
    public double width() { return width; }
    public double height() { return height; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof RectangleView that)) return false;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0
                && Double.compare(width, that.width) == 0 && Double.compare(height, that.height) == 0;
    }
    @Override public int hashCode() { return Objects.hash(x, y, width, height); }
    @Override public String toString() {
        return "RectangleView[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }
}
