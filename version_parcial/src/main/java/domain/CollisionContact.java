package domain;

/**
 * Resultado inmutable de una consulta del {@link CollisionDetector}.
 * Conserva las dos entidades en el orden recibido y señala si sus formas
 * geométricas tienen contacto. No modifica ni resuelve el estado físico.
 */
public final class CollisionContact {

    /** Primera entidad entregada al detector. */
    private final Collidable first;
    /** Segunda entidad entregada al detector. */
    private final Collidable second;
    /** Indica si la estrategia geométrica encontró contacto. */
    private final boolean contact;

    /**
     * Crea un resultado de detección.
     *
     * @param first primera entidad evaluada
     * @param second segunda entidad evaluada
     * @param contact {@code true} si sus formas están en contacto
     */
    public CollisionContact(Collidable first, Collidable second, boolean contact) {
        this.first = first;
        this.second = second;
        this.contact = contact;
    }

    /**
     * Devuelve la primera entidad que recibió el detector.
     *
     * @return primera entidad evaluada, o {@code null} si se recibió nula
     */
    public Collidable getFirst() {
        return first;
    }

    /**
     * Devuelve la segunda entidad que recibió el detector.
     *
     * @return segunda entidad evaluada, o {@code null} si se recibió nula
     */
    public Collidable getSecond() {
        return second;
    }

    /**
     * Indica si la estrategia aplicable detectó contacto entre las formas.
     *
     * @return {@code true} si las formas se tocan o solapan
     */
    public boolean isContact() {
        return contact;
    }
}
