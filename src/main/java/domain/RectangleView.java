package domain;

import java.util.Objects;

/** Instantánea inmutable de un rectángulo expuesta por la inspección de sesión. */
public final class RectangleView {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    /** Crea una instantánea rectangular.
     * @param x coordenada horizontal de la esquina superior izquierda
     * @param y coordenada vertical de la esquina superior izquierda
     * @param width ancho del rectángulo
     * @param height alto del rectángulo
     */
    public RectangleView(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    /** Devuelve la coordenada horizontal de la esquina superior izquierda.
     * @return coordenada horizontal
     */
    public double x() { return x; }
    /** Devuelve la coordenada vertical de la esquina superior izquierda.
     * @return coordenada vertical
     */
    public double y() { return y; }
    /** Devuelve el ancho del rectángulo.
     * @return ancho
     */
    public double width() { return width; }
    /** Devuelve el alto del rectángulo.
     * @return alto
     */
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
