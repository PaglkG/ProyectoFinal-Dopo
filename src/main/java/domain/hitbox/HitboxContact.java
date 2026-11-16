package domain.hitbox;

/**
 * Identifica una entidad de dominio con la que se detectó contacto y la
 * hitbox asociada que participó en él.
 *
 * @param name nombre simple de la entidad, o {@code ""} cuando no hay contacto
 * @param hitbox hitbox asociada a la entidad; es {@code null} si no hay contacto
 */
public record HitboxContact(String name, Hitbox hitbox) {

    private static final HitboxContact NO_CONTACT = new HitboxContact("", null);

    /**
     * Devuelve el resultado que representa la ausencia de contacto.
     *
     * @return resultado con nombre vacío y sin hitbox asociada
     */
    public static HitboxContact none() {
        return NO_CONTACT;
    }

    /**
     * Indica si este resultado contiene un contacto detectado.
     *
     * @return {@code true} si el nombre no está vacío y existe una hitbox
     */
    public boolean isContact() {
        return !name.isEmpty() && hitbox != null;
    }
}
