package domain.hitbox;

/**
 * Representa una hitbox circular definida por centro y radio.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class CircularHitbox implements Hitbox {

    private double x;
    private double y;
    private double radius;

    /**
     * Crea una hitbox circular.
     *
     * @param x coordenada horizontal del centro
     * @param y coordenada vertical del centro
     * @param radius radio del círculo
     */
    public CircularHitbox(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
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
     * Reposiciona el centro del círculo y conserva su radio.
     *
     * @param x nueva coordenada horizontal del centro
     * @param y nueva coordenada vertical del centro
     */
    @Override
    public void reposition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
