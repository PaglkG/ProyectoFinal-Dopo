package domain;

/**
 * Estrategia para un círculo y un rectángulo, recibidos en cualquier orden.
 * Acota el centro del círculo al rectángulo para obtener el punto más cercano y
 * compara esa distancia con el valor absoluto del radio. Incluye contactos en
 * los bordes y las esquinas.
 */
public final class CircleRectangleStrategy implements CollisionDetectionStrategy {

    /** Crea la estrategia círculo-rectángulo. */
    public CircleRectangleStrategy() { }

    /** {@inheritDoc} */
    @Override
    public boolean supports(CollisionShape first, CollisionShape second) {
        return first instanceof CircularHitbox && second instanceof RectangularHitbox
                || first instanceof RectangularHitbox && second instanceof CircularHitbox;
    }

    /** {@inheritDoc} */
    @Override
    public boolean detect(CollisionShape first, CollisionShape second) {
        CircularHitbox circle = first instanceof CircularHitbox firstCircle
                ? firstCircle : (CircularHitbox) second;
        RectangularHitbox rectangle = first instanceof RectangularHitbox firstRectangle
                ? firstRectangle : (RectangularHitbox) second;
        double left = Math.min(rectangle.getX(), rectangle.getX() + rectangle.getWidth());
        double right = Math.max(rectangle.getX(), rectangle.getX() + rectangle.getWidth());
        double top = Math.min(rectangle.getY(), rectangle.getY() + rectangle.getHeight());
        double bottom = Math.max(rectangle.getY(), rectangle.getY() + rectangle.getHeight());
        double closestX = Math.max(left, Math.min(circle.getX(), right));
        double closestY = Math.max(top, Math.min(circle.getY(), bottom));
        double distance = Math.hypot(circle.getX() - closestX, circle.getY() - closestY);
        return distance <= Math.abs(circle.getRadius());
    }
}
