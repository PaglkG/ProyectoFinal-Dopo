package domain;

import java.io.Serializable;
import domain.CircularHitbox;
import domain.HitboxContact;
import domain.HitboxIntersections;

/**
 * Representa el balón y su hitbox circular.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Ball implements Serializable, Collidable {

    private static final long serialVersionUID = 1L;

    private double velocityX;
    private double velocityY;
    private double previousX;
    private double previousY;
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
        previousX = getX();
        previousY = getY();
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

    /** @return hitbox circular usada para detectar contactos */
    @Override
    public CollisionShape getCollisionShape() {
        return BallHitbox;
    }

    /** Devuelve la coordenada horizontal actual del centro del balón.
     * @return coordenada horizontal del centro almacenada en la hitbox
     */
    public double getX() {
        return BallHitbox.getX();
    }

    /** Devuelve la coordenada vertical actual del centro del balón.
     * @return coordenada vertical del centro almacenada en la hitbox
     */
    public double getY() {
        return BallHitbox.getY();
    }

    double getPreviousX() { return previousX; }

    double getPreviousY() { return previousY; }

    /** Devuelve el radio del balón.
     * @return radio almacenado en la hitbox circular
     */
    public double getRadius() {
        return BallHitbox.getRadius();
    }

    /** Devuelve la componente horizontal actual de la velocidad.
     * @return componente horizontal de la velocidad actual
     */
    public double getVelocityX() {
        return velocityX;
    }

    /** Devuelve la componente vertical actual de la velocidad.
     * @return componente vertical de la velocidad actual
     */
    public double getVelocityY() {
        return velocityY;
    }

    /**
     * Suma un impulso a las velocidades actuales.
     *
     * @param deltaX cambio de velocidad horizontal
     * @param deltaY cambio de velocidad vertical
     */
    public void applyImpulse(double deltaX, double deltaY) {
        velocityX += deltaX;
        velocityY += deltaY;
    }

    /**
     * Asegura que la velocidad horizontal apunte hacia la dirección indicada.
     * Solo reemplaza la componente cuando es cero o apunta en sentido contrario;
     * en ese caso fija su magnitud mínima requerida.
     *
     * @param direction sentido horizontal esperado, expresado como -1 o 1
     * @param minimumSpeed magnitud mínima de velocidad que debe conservarse
     */
    public void ensureHorizontalDirection(int direction, double minimumSpeed) {
        if (direction != 0 && velocityX * direction <= 0) {
            velocityX = direction * Math.max(Math.abs(velocityX), minimumSpeed);
        }
    }

    /**
     * Integra un paso físico fijo: aplica gravedad y luego desplaza el balón
     * usando las velocidades resultantes.
     *
     * @param deltaSeconds duración del paso en segundos
     * @param gravity aceleración vertical en unidades por segundo cuadrado
     */
    public void advance(double deltaSeconds, double gravity) {
        previousX = getX();
        previousY = getY();
        velocityY += gravity * deltaSeconds;
        BallHitbox.reposition(
                getX() + velocityX * deltaSeconds,
                getY() + velocityY * deltaSeconds);
    }

    /**
     * Coloca el centro del balón en la altura de contacto y refleja su velocidad
     * vertical si se mueve hacia abajo. Reduce la magnitud por el factor de
     * retención y detiene el rebote si queda por debajo del mínimo.
     *
     * @param centerY coordenada vertical del centro después del contacto
     * @param retention fracción de velocidad que se conserva tras el rebote
     * @param minimumSpeed magnitud mínima para mantener el rebote
     */
    public void bounceAt(double centerY, double retention, double minimumSpeed) {
        BallHitbox.reposition(getX(), centerY);
        if (velocityY >= 0) {
            velocityY = -velocityY * retention;
            if (Math.abs(velocityY) < minimumSpeed) {
                velocityY = 0;
            }
        }
    }

    /**
     * Coloca el balón en la coordenada horizontal de contacto e invierte la
     * velocidad horizontal, conservando la fracción indicada.
     *
     * @param centerX nueva coordenada horizontal del centro
     * @param retention fracción de velocidad que se conserva
     */
    public void bounceHorizontallyAt(double centerX, double retention) {
        BallHitbox.reposition(centerX, getY());
        velocityX = -velocityX * retention;
    }

    /**
     * Coloca el balón en la coordenada vertical de contacto e invierte la
     * velocidad vertical, conservando la fracción indicada.
     *
     * @param centerY nueva coordenada vertical del centro
     * @param retention fracción de velocidad que se conserva
     */
    public void bounceVerticallyAt(double centerY, double retention) {
        BallHitbox.reposition(getX(), centerY);
        velocityY = -velocityY * retention;
    }

    /**
     * Comprueba el contacto con otra entidad o hitbox mediante la geometría centralizada.
     *
     * @param target entidad o hitbox que se comparará
     * @return nombre de la entidad contactada y su hitbox; si no hay contacto,
     *         un resultado con nombre vacío
     */
    public HitboxContact intersects(Object target) {
        return HitboxIntersections.intersects(this, target);
    }

    /**
     * Reposiciona el balón y el centro de su hitbox circular.
     *
     * @param x nueva coordenada horizontal del centro
     * @param y nueva coordenada vertical del centro
     */
    public void reposition(double x, double y) {
        BallHitbox.reposition(x, y);
    }
}
