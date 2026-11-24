package domain;

/**
 * Estrategia para dos círculos. Detecta contacto cuando la distancia entre sus
 * centros, calculada con {@link Math#hypot(double, double)}, no supera la suma
 * de los valores absolutos de sus radios; la igualdad incluye el contacto
 * tangencial.
 */
public final class CircleCircleStrategy implements CollisionDetectionStrategy {

    /** Crea la estrategia círculo-círculo. */
    public CircleCircleStrategy() { }

    /** {@inheritDoc} */
    @Override
    public boolean supports(CollisionShape first, CollisionShape second) {
        return first instanceof CircularHitbox && second instanceof CircularHitbox;
    }

    /** {@inheritDoc} */
    @Override
    public boolean detect(CollisionShape first, CollisionShape second) {
        CircularHitbox firstCircle = (CircularHitbox) first;
        CircularHitbox secondCircle = (CircularHitbox) second;
        double distance = Math.hypot(firstCircle.getX() - secondCircle.getX(),
                firstCircle.getY() - secondCircle.getY());
        return distance <= Math.abs(firstCircle.getRadius()) + Math.abs(secondCircle.getRadius());
    }
}
