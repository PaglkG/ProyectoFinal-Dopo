package domain;

/** Immutable participant state exposed by session inspection. */
public final class ParticipantView {
    private final String side;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final int speed;

    public ParticipantView(String side, double x, double y, double width, double height, int speed) {
        this.side = side; this.x = x; this.y = y;
        this.width = width; this.height = height; this.speed = speed;
    }
    public String side() { return side; }
    public double x() { return x; }
    public double y() { return y; }
    public double width() { return width; }
    public double height() { return height; }
    public int speed() { return speed; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ParticipantView that)) return false;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0
                && Double.compare(width, that.width) == 0 && Double.compare(height, that.height) == 0
                && speed == that.speed && java.util.Objects.equals(side, that.side);
    }
    @Override public int hashCode() { return java.util.Objects.hash(side, x, y, width, height, speed); }
    @Override public String toString() {
        return "ParticipantView[side=" + side + ", x=" + x + ", y=" + y
                + ", width=" + width + ", height=" + height + ", speed=" + speed + "]";
    }
}
