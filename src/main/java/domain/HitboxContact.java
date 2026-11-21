package domain;

import java.util.Objects;

/**
 * Identifica una entidad de dominio con la que se detectó contacto y la
 * hitbox asociada que participó en él.
 *
 * @param name nombre simple de la entidad, o {@code ""} cuando no hay contacto
 * @param hitbox hitbox asociada a la entidad; es {@code null} si no hay contacto
 */
public final class HitboxContact {

    private final String name;
    private final Hitbox hitbox;

    public HitboxContact(String name, Hitbox hitbox) {
        this.name = name;
        this.hitbox = hitbox;
    }

    public String name() { return name; }

    public Hitbox hitbox() { return hitbox; }

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

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HitboxContact that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(hitbox, that.hitbox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hitbox);
    }

    @Override
    public String toString() {
        return "HitboxContact[name=" + name + ", hitbox=" + hitbox + "]";
    }
}
