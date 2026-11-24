package domain;

/**
 * Estrategia AABB para dos rectángulos alineados con los ejes.
 * Normaliza los extremos de cada intervalo horizontal y vertical y considera
 * contacto cuando los cuatro intervalos se solapan o tocan, incluidos los
 * bordes. La normalización también conserva el tratamiento actual de anchos o
 * altos negativos.
 */
public final class RectangleRectangleStrategy implements CollisionDetectionStrategy {

    /** Crea la estrategia rectángulo-rectángulo. */
    public RectangleRectangleStrategy() { }

    /** {@inheritDoc} */
    @Override
    public boolean supports(CollisionShape first, CollisionShape second) {
        return first instanceof RectangularHitbox && second instanceof RectangularHitbox;
    }

    /** {@inheritDoc} */
    @Override
    public boolean detect(CollisionShape first, CollisionShape second) {
        RectangularHitbox firstRectangle = (RectangularHitbox) first;
        RectangularHitbox secondRectangle = (RectangularHitbox) second;
        double firstLeft = Math.min(firstRectangle.getX(), firstRectangle.getX() + firstRectangle.getWidth());
        double firstRight = Math.max(firstRectangle.getX(), firstRectangle.getX() + firstRectangle.getWidth());
        double firstTop = Math.min(firstRectangle.getY(), firstRectangle.getY() + firstRectangle.getHeight());
        double firstBottom = Math.max(firstRectangle.getY(), firstRectangle.getY() + firstRectangle.getHeight());
        double secondLeft = Math.min(secondRectangle.getX(), secondRectangle.getX() + secondRectangle.getWidth());
        double secondRight = Math.max(secondRectangle.getX(), secondRectangle.getX() + secondRectangle.getWidth());
        double secondTop = Math.min(secondRectangle.getY(), secondRectangle.getY() + secondRectangle.getHeight());
        double secondBottom = Math.max(secondRectangle.getY(), secondRectangle.getY() + secondRectangle.getHeight());

        return firstLeft <= secondRight
                && firstRight >= secondLeft
                && firstTop <= secondBottom
                && firstBottom >= secondTop;
    }
}
