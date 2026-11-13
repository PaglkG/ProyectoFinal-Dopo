package domain.hitbox;

/**
 * Representa una hitbox rectangular definida por posición y dimensiones.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class RectangularHitbox implements Hitbox {

    private double x;
    private double y;
    private double width;
    private double height;

    /**
     * Crea una hitbox rectangular.
     *
     * @param x coordenada horizontal
     * @param y coordenada vertical
     * @param width ancho del rectángulo
     * @param height alto del rectángulo
     */
    public RectangularHitbox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Comprueba la intersección con otra hitbox.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return {@code false} hasta que se definan las reglas geométricas
     */
    @Override
    public boolean intersects(Hitbox hitbox) {
        return false;
    }

    /**
     * Reposiciona la esquina superior izquierda del rectángulo.
     *
     * @param x nueva coordenada horizontal
     * @param y nueva coordenada vertical
     */
    @Override
    public void reposition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
