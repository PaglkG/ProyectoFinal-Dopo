package domain;

/**
 * Adapta las consultas de hitboxes al cálculo geométrico de {@link CollisionDetector}.
 *
 * <p>Reconoce círculos y rectángulos asociados a objetos del dominio
 * ({@code Ball} y {@code SessionParticipant}) o contenidos por el campo
 * ({@code SoccerField}). Las comparaciones admiten contacto entre
 * bordes. Esta clase solo detecta contacto: no mueve objetos ni resuelve sus
 * consecuencias físicas.</p>
 */
public final class HitboxIntersections {

    /** Evita crear instancias de esta clase, que solo ofrece operaciones estáticas. */
    private HitboxIntersections() {
    }

    /**
     * Comprueba la intersección entre formas o entidades con hitboxes asociadas.
     *
     * <p>Si un argumento es {@code null}, o si el par de tipos no está
     * soportado, devuelve un resultado sin contacto. Los participantes y balones se
     * comparan usando su hitbox; una cancha comprueba sus límites, arcos y
     * balones. El contacto entre bordes cuenta como intersección.</p>
     *
     * @param first primera forma o entidad
     * @param second segunda forma o entidad
     * @return información de la entidad contactada y su hitbox, o un resultado
     *         sin contacto si las formas no se tocan o no hay una entidad
     *         de dominio identificable
     */
    public static HitboxContact intersects(Object first, Object second) {
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
        if (!CollisionDetector.intersects(firstShape, secondShape)) {
            return HitboxContact.none();
        }

        return contactFor(second);
    }

    /**
     * Extrae la forma geométrica asociada a una entidad.
     * Las formas geométricas puras se devuelven directamente.
     *
     * @param subject entidad o forma recibida
     * @return forma geométrica conocida, o {@code null} para tipos no soportados
     */
    private static Hitbox shapeOf(Object subject) {
        if (subject instanceof Ball ball) {
            return ball.getHitbox();
        }
        if (subject instanceof SessionParticipant participant) {
            return participant.getHitbox();
        }
        if (subject instanceof Hitbox hitbox) {
            return hitbox;
        }
        return null;
    }

    /**
     * Construye la información de contacto correspondiente al segundo objeto.
     *
     * @param target objeto del lado contactado
     * @return entidad y hitbox asociada, o resultado vacío si no tiene nombre
     *         de dominio contemplado
     */
    private static HitboxContact contactFor(Object target) {
        if (target instanceof Ball ball) {
            return new HitboxContact("Ball", ball.getHitbox());
        }
        if (target instanceof SessionParticipant participant) {
            return new HitboxContact("SessionParticipant", participant.getHitbox());
        }
        return HitboxContact.none();
    }

}

