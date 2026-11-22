package domain;

import java.util.Objects;

/** Instantánea inmutable del estado de un balón expuesta por la inspección de sesión. */
public final class BallView {
    private final int index;
    private final double x;
    private final double y;
    private final double radius;
    private final double velocityX;
    private final double velocityY;

    /** Crea una instantánea con la geometría y velocidad actuales del balón.
     * @param index índice del balón en la sesión
     * @param x coordenada horizontal de su centro
     * @param y coordenada vertical de su centro
     * @param radius radio del balón
     * @param velocityX componente horizontal de su velocidad
     * @param velocityY componente vertical de su velocidad
     */
    public BallView(int index, double x, double y, double radius, double velocityX, double velocityY) {
        this.index = index; this.x = x; this.y = y; this.radius = radius;
        this.velocityX = velocityX; this.velocityY = velocityY;
    }
    /** Devuelve el índice del balón en la sesión.
     * @return índice del balón en la sesión
     */
    public int index() { return index; }
    /** Devuelve la coordenada horizontal del centro.
     * @return coordenada horizontal del centro
     */
    public double x() { return x; }
    /** Devuelve la coordenada vertical del centro.
     * @return coordenada vertical del centro
     */
    public double y() { return y; }
    /** Devuelve el radio del balón.
     * @return radio del balón
     */
    public double radius() { return radius; }
    /** Devuelve la componente horizontal de la velocidad.
     * @return componente horizontal de la velocidad
     */
    public double velocityX() { return velocityX; }
    /** Devuelve la componente vertical de la velocidad.
     * @return componente vertical de la velocidad
     */
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
