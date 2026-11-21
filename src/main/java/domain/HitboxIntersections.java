package domain;

import domain.Ball;
import domain.SessionParticipant;
import domain.SoccerField;

/**
 * Centraliza las comprobaciones geométricas entre hitboxes.
 *
 * <p>Reconoce círculos y rectángulos, y también objetos del dominio que
 * delegan en una forma ({@code Ball} y {@code SessionParticipant}) o en varias
 * formas ({@code SoccerField}). Las comparaciones admiten contacto entre
 * bordes. Esta clase solo detecta contacto: no mueve objetos ni resuelve sus
 * consecuencias físicas.</p>
 */
public final class HitboxIntersections {

    /** Evita crear instancias de esta clase, que solo ofrece operaciones estáticas. */
    private HitboxIntersections() {
    }

    /**
     * Comprueba la intersección entre dos formas o entre objetos que exponen
     * formas mediante sus hitboxes.
     *
     * <p>Si un argumento es {@code null}, o si el par de tipos no está
     * soportado, devuelve un resultado sin contacto. Los participantes y balones se
     * comparan usando su hitbox; una cancha comprueba sus límites, arcos y
     * balones. El contacto entre bordes cuenta como intersección.</p>
     *
     * @param first primera hitbox
     * @param second segunda hitbox
     * @return información de la entidad contactada y su hitbox, o un resultado
     *         sin contacto si las formas no se tocan o no hay una entidad
     *         de dominio identificable
     */
    public static HitboxContact intersects(Hitbox first, Hitbox second) {
        if (first == null || second == null) {
            return HitboxContact.none();
        }

        if (first instanceof SoccerField field) {
            return field.intersects(second);
        }
        if (second instanceof SoccerField field) {
            return field.contactingPart(first);
        }

        Hitbox firstShape = shapeOf(first);
        Hitbox secondShape = shapeOf(second);
        if (firstShape == null || secondShape == null || !shapesIntersect(firstShape, secondShape)) {
            return HitboxContact.none();
        }

        return contactFor(second);
    }

    /**
     * Extrae la forma geométrica de una entidad que la contiene.
     * Las formas geométricas puras se devuelven directamente.
     *
     * @param hitbox entidad o forma recibida
     * @return forma geométrica conocida, o {@code null} para tipos no soportados
     */
    private static Hitbox shapeOf(Hitbox hitbox) {
        if (hitbox instanceof Ball ball) {
            return ball.getHitbox();
        }
        if (hitbox instanceof SessionParticipant participant) {
            return participant.getHitbox();
        }
        if (hitbox instanceof CircularHitbox || hitbox instanceof RectangularHitbox) {
            return hitbox;
        }
        return null;
    }

    /**
     * Comprueba la geometría cuando ambas referencias ya son formas básicas.
     *
     * @param first primera forma
     * @param second segunda forma
     * @return {@code true} si se tocan o se solapan
     */
    private static boolean shapesIntersect(Hitbox first, Hitbox second) {
        if (first instanceof CircularHitbox firstCircle
                && second instanceof CircularHitbox secondCircle) {
            return circlesIntersect(firstCircle, secondCircle);
        }
        if (first instanceof RectangularHitbox firstRectangle
                && second instanceof RectangularHitbox secondRectangle) {
            return rectanglesIntersect(firstRectangle, secondRectangle);
        }
        if (first instanceof CircularHitbox circle
                && second instanceof RectangularHitbox rectangle) {
            return circleIntersectsRectangle(circle, rectangle);
        }
        if (first instanceof RectangularHitbox rectangle
                && second instanceof CircularHitbox circle) {
            return circleIntersectsRectangle(circle, rectangle);
        }
        return false;
    }

    /**
     * Construye la información de contacto correspondiente al segundo objeto.
     *
     * @param target objeto del lado contactado
     * @return entidad y hitbox asociada, o resultado vacío si no tiene nombre
     *         de dominio contemplado
     */
    private static HitboxContact contactFor(Hitbox target) {
        if (target instanceof Ball ball) {
            return new HitboxContact("Ball", ball.getHitbox());
        }
        if (target instanceof SessionParticipant participant) {
            return new HitboxContact("SessionParticipant", participant.getHitbox());
        }
        return HitboxContact.none();
    }

    /**
     * Compara dos círculos mediante la distancia entre centros.
     *
     * @param first primer círculo
     * @param second segundo círculo
     * @return {@code true} cuando la distancia entre centros no supera la suma
     *         de los radios absolutos
     */
    private static boolean circlesIntersect(CircularHitbox first, CircularHitbox second) {
        double distance = Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
        return distance <= Math.abs(first.getRadius()) + Math.abs(second.getRadius());
    }

    /**
     * Compara dos rectángulos usando sus intervalos horizontal y vertical.
     * Normaliza los extremos para que dimensiones negativas sigan describiendo
     * el mismo rectángulo entre sus dos coordenadas.
     *
     * @param first primer rectángulo
     * @param second segundo rectángulo
     * @return {@code true} si ambos intervalos se solapan o se tocan en los dos ejes
     */
    private static boolean rectanglesIntersect(RectangularHitbox first, RectangularHitbox second) {
        double firstLeft = Math.min(first.getX(), first.getX() + first.getWidth());
        double firstRight = Math.max(first.getX(), first.getX() + first.getWidth());
        double firstTop = Math.min(first.getY(), first.getY() + first.getHeight());
        double firstBottom = Math.max(first.getY(), first.getY() + first.getHeight());
        double secondLeft = Math.min(second.getX(), second.getX() + second.getWidth());
        double secondRight = Math.max(second.getX(), second.getX() + second.getWidth());
        double secondTop = Math.min(second.getY(), second.getY() + second.getHeight());
        double secondBottom = Math.max(second.getY(), second.getY() + second.getHeight());

        return firstLeft <= secondRight
                && firstRight >= secondLeft
                && firstTop <= secondBottom
                && firstBottom >= secondTop;
    }

    /**
     * Compara un círculo y un rectángulo mediante el punto del rectángulo más
     * cercano al centro del círculo. La distancia a ese punto se compara con
     * el radio absoluto; por ello se detectan también contactos en esquinas.
     *
     * @param circle círculo que se compara
     * @param rectangle rectángulo que se compara
     * @return {@code true} si el círculo toca o solapa el rectángulo
     */
    private static boolean circleIntersectsRectangle(
            CircularHitbox circle,
            RectangularHitbox rectangle) {
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

