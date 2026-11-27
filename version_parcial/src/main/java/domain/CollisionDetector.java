package domain;

/** Detecta contactos geométricos entre formas sin modificar las entidades. */
public final class CollisionDetector {

    /** Crea un detector con soporte directo para círculos y rectángulos. */
    public CollisionDetector() {
    }

    /**
     * Detecta contacto entre las formas expuestas por las entidades.
     * Si falta una entidad o su forma, o el par no está admitido, devuelve
     * un resultado sin contacto y conserva las entidades recibidas.
     *
     * @param first primera entidad
     * @param second segunda entidad
     * @return entidades evaluadas y resultado de contacto
     */
    public CollisionContact detect(Collidable first, Collidable second) {
        if (first == null || second == null) {
            return new CollisionContact(first, second, false);
        }
        boolean contact = intersects(first.getCollisionShape(), second.getCollisionShape());
        return new CollisionContact(first, second, contact);
    }

    /** Comprueba el contacto entre dos formas del dominio, incluidos sus bordes. */
    static boolean intersects(CollisionShape first, CollisionShape second) {
        if (first instanceof CircularHitbox firstCircle) {
            if (second instanceof CircularHitbox secondCircle) {
                return circlesIntersect(firstCircle, secondCircle);
            }
            if (second instanceof RectangularHitbox rectangle) {
                return circleIntersectsRectangle(firstCircle, rectangle);
            }
        }
        if (first instanceof RectangularHitbox firstRectangle) {
            if (second instanceof CircularHitbox circle) {
                return circleIntersectsRectangle(circle, firstRectangle);
            }
            if (second instanceof RectangularHitbox secondRectangle) {
                return rectanglesIntersect(firstRectangle, secondRectangle);
            }
        }
        return false;
    }

    /** Compara la distancia entre centros con la suma de los radios. */
    private static boolean circlesIntersect(CircularHitbox firstCircle, CircularHitbox secondCircle) {
        double distance = Math.hypot(firstCircle.getX() - secondCircle.getX(),
                firstCircle.getY() - secondCircle.getY());
        return distance <= Math.abs(firstCircle.getRadius()) + Math.abs(secondCircle.getRadius());
    }

    /** Compara los intervalos de los rectángulos tras normalizar sus extremos. */
    private static boolean rectanglesIntersect(RectangularHitbox firstRectangle, RectangularHitbox secondRectangle) {
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

    /** Compara el radio con la distancia del centro al punto del rectángulo más cercano. */
    private static boolean circleIntersectsRectangle(CircularHitbox circle, RectangularHitbox rectangle) {
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
