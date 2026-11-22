package domain;

/** Instantánea inmutable de un participante expuesta por la inspección de sesión. */
public final class ParticipantView {
    private final String side;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final int speed;

    /** Crea una instantánea con el lado, la geometría y la velocidad del participante.
     * @param side lado ocupado, {@code left} o {@code right}
     * @param x coordenada horizontal de la hitbox
     * @param y coordenada vertical de la hitbox
     * @param width ancho de la hitbox
     * @param height alto de la hitbox
     * @param speed nivel de velocidad
     */
    public ParticipantView(String side, double x, double y, double width, double height, int speed) {
        this.side = side; this.x = x; this.y = y;
        this.width = width; this.height = height; this.speed = speed;
    }
    /** Devuelve el lado ocupado por el participante.
     * @return {@code left} o {@code right}
     */
    public String side() { return side; }
    /** Devuelve la coordenada horizontal de la hitbox.
     * @return coordenada horizontal
     */
    public double x() { return x; }
    /** Devuelve la coordenada vertical de la hitbox.
     * @return coordenada vertical
     */
    public double y() { return y; }
    /** Devuelve el ancho de la hitbox.
     * @return ancho
     */
    public double width() { return width; }
    /** Devuelve el alto de la hitbox.
     * @return alto
     */
    public double height() { return height; }
    /** Devuelve el nivel de velocidad del participante.
     * @return nivel de velocidad
     */
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
