package domain;

import domain.hitbox.CircularHitbox;
import domain.hitbox.Hitbox;

/**
 * Representa el balón y su hitbox circular.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Ball implements Hitbox {

    private boolean physical;
    private double velocityX;
    private double velocityY;
    private CircularHitbox BallHitbox;

    /**
     * Crea el balón en el centro geométrico de las dimensiones indicadas.
     *
     * @param width ancho de la cancha
     * @param height alto de la cancha
     * @param radius radio del balón
     */
    public Ball(double width, double height, double radius) {
        BallHitbox = new CircularHitbox(width / 2, height / 2, radius);
    }

    /**
     * Centra el balón según las dimensiones de la cancha y detiene su movimiento.
     *
     * @param width ancho de la cancha
     * @param height alto de la cancha
     */
    public void resetBall(double width, double height) {
        BallHitbox.reposition(width / 2, height / 2);
        velocityX = 0;
        velocityY = 0;
    }

    /**
     * Devuelve la hitbox circular del balón.
     *
     * @return hitbox circular asociada
     */
    public CircularHitbox getHitbox() {
        return BallHitbox;
    }

    /**
     * Comprueba la intersección con otra hitbox.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return {@code false} hasta que se definan las reglas de colisión
     */
    @Override
    public boolean intersects(Hitbox hitbox) {
        return false;
    }

    /**
     * Reposiciona el centro de la hitbox circular del balón.
     *
     * @param x nueva coordenada horizontal del centro
     * @param y nueva coordenada vertical del centro
     */
    @Override
    public void reposition(double x, double y) {
        BallHitbox.reposition(x, y);
    }
}
