package domain;

import java.io.Serializable;
/**
 * Representa una hitbox circular definida por el centro {@code (x, y)} y un
 * radio. Se utiliza para aproximar objetos redondos, como el balón.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class CircularHitbox implements Hitbox, Serializable {

    private static final long serialVersionUID = 1L;

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
     * @return entidad y hitbox asociada que hacen contacto, o nombre vacío si
     *         no hay una entidad de dominio identificable en contacto
     */
    @Override
    public HitboxContact intersects(Hitbox hitbox) {
        return HitboxIntersections.intersects(this, hitbox);
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

    /**
     * Devuelve la coordenada horizontal del centro.
     *
     * @return coordenada horizontal del centro
     */
    public double getX() {
        return x;
    }

    /**
     * Devuelve la coordenada vertical del centro.
     *
     * @return coordenada vertical del centro
     */
    public double getY() {
        return y;
    }

    /**
     * Devuelve el radio.
     *
     * @return radio almacenado
     */
    public double getRadius() {
        return radius;
    }
}
