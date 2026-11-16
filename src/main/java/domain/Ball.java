package domain;

import domain.hitbox.CircularHitbox;
import domain.hitbox.Hitbox;
import domain.hitbox.HitboxContact;
import domain.hitbox.HitboxIntersections;

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

    /** @return coordenada horizontal del centro del balón */
    public double getX() {
        return BallHitbox.getX();
    }

    /** @return coordenada vertical del centro del balón */
    public double getY() {
        return BallHitbox.getY();
    }

    /** @return radio del balón */
    public double getRadius() {
        return BallHitbox.getRadius();
    }

    /** @return velocidad horizontal actual */
    public double getVelocityX() {
        return velocityX;
    }

    /** @return velocidad vertical actual */
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
     * Garantiza que la velocidad horizontal salga en la dirección indicada.
     * Si el impulso recibido aún apunta hacia atrás, la redirige con la magnitud mínima.
     *
     * @param direction dirección horizontal: -1 izquierda, 1 derecha
     * @param minimumSpeed magnitud mínima cuando se redirige
     */
    void ensureHorizontalDirection(int direction, double minimumSpeed) {
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
        velocityY += gravity * deltaSeconds;
        BallHitbox.reposition(
                getX() + velocityX * deltaSeconds,
                getY() + velocityY * deltaSeconds);
    }

    /**
     * Coloca el balón sobre el suelo y aplica la pérdida de velocidad del rebote.
     * Si la velocidad resultante está por debajo del mínimo, deja el balón quieto.
     *
     * @param centerY coordenada vertical del centro cuando toca el suelo
     * @param retention fracción de velocidad conservada tras el impacto
     * @param minimumSpeed velocidad mínima para continuar rebotando
     */
    void bounceAt(double centerY, double retention, double minimumSpeed) {
        BallHitbox.reposition(getX(), centerY);
        if (velocityY >= 0) {
            velocityY = -velocityY * retention;
            if (Math.abs(velocityY) < minimumSpeed) {
                velocityY = 0;
            }
        }
    }

    void bounceHorizontallyAt(double centerX, double retention) {
        BallHitbox.reposition(centerX, getY());
        velocityX = -velocityX * retention;
    }

    void bounceVerticallyAt(double centerY, double retention) {
        BallHitbox.reposition(getX(), centerY);
        velocityY = -velocityY * retention;
    }

    /**
     * Comprueba la intersección con otra hitbox.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return nombre de la entidad contactada y su hitbox; si no hay contacto,
     *         un resultado con nombre vacío
     */
    @Override
    public HitboxContact intersects(Hitbox hitbox) {
        return HitboxIntersections.intersects(this, hitbox);
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
