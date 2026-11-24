package domain;

import java.io.Serializable;
/**
 * Representa una hitbox rectangular mediante la esquina superior izquierda
 * {@code (x, y)} y sus dimensiones {@code width × height}.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class RectangularHitbox implements Hitbox, Serializable {

    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private double width;
    private double height;

    /**
     * Crea una hitbox rectangular.
     *
     * @param x coordenada horizontal de la esquina superior izquierda
     * @param y coordenada vertical de la esquina superior izquierda
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
     * Comprueba la intersección con otra entidad o forma.
     *
     * @param target entidad o forma con la que se comprobará la intersección
     * @return entidad y hitbox asociada que hacen contacto, o nombre vacío si
     *         no hay una entidad de dominio identificable en contacto
     */
    @Override
    public HitboxContact intersects(Object target) {
        return HitboxIntersections.intersects(this, target);
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

    /**
     * Devuelve la coordenada horizontal de la esquina superior izquierda.
     *
     * @return coordenada horizontal de la esquina superior izquierda
     */
    public double getX() {
        return x;
    }

    /**
     * Devuelve la coordenada vertical de la esquina superior izquierda.
     *
     * @return coordenada vertical de la esquina superior izquierda
     */
    public double getY() {
        return y;
    }

    /**
     * Devuelve el ancho del rectángulo.
     *
     * @return ancho del rectángulo
     */
    public double getWidth() {
        return width;
    }

    /**
     * Devuelve el alto del rectángulo.
     *
     * @return alto del rectángulo
     */
    public double getHeight() {
        return height;
    }

    /**
     * Cambia las dimensiones del rectángulo sin mover su esquina superior izquierda.
     *
     * @param width nuevo ancho
     * @param height nuevo alto
     */
    public void resize(double width, double height) {
        this.width = width;
        this.height = height;
    }
}
