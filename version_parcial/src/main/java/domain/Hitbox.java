package domain;

/**
 * Define las operaciones geométricas comunes a las hitboxes del dominio.
 * Detectar una intersección informa que dos formas se tocan o se solapan; no
 * determina qué efecto físico o regla de juego debe ocurrir después.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public interface Hitbox extends CollisionShape {

    /**
     * Comprueba si esta hitbox se toca o se solapa con la entidad o forma
     * indicada. El contacto entre bordes cuenta como intersección; si el tipo
     * no está soportado, devuelve un resultado sin contacto.
     *
     * @param target entidad o hitbox con la que se comprobará la intersección
     * @return nombre de la entidad contactada y su hitbox; si no hay contacto,
     *         el nombre es vacío y la hitbox es {@code null}
     */
    HitboxContact intersects(Object target);

    /**
     * Reposiciona esta hitbox conservando sus dimensiones o radio.
     *
     * @param x nueva coordenada horizontal del punto de referencia; es el
     *          centro para círculos y la esquina superior izquierda para rectángulos
     * @param y nueva coordenada vertical del punto de referencia; es el centro
     *          para círculos y la esquina superior izquierda para rectángulos
     */
    @Override
    void reposition(double x, double y);
}
