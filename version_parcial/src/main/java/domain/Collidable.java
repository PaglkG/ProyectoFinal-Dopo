package domain;

/**
 * Entidad que expone al detector la forma geométrica con la que participa en
 * una consulta de contacto. La interfaz no define cómo se resuelve el contacto.
 */
public interface Collidable {

    /**
     * Devuelve la forma asociada a esta entidad.
     *
     * @return forma geométrica de la entidad
     */
    CollisionShape getCollisionShape();
}
