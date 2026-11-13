package domain.hitbox;

/**
 * Define la operación común para determinar si dos hitboxes se intersectan.
 * El efecto de cada intersección depende de los objetos que representa y está
 * pendiente de las reglas acordadas para cada pareja.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public interface Hitbox {

    /**
     * Comprueba si esta hitbox se intersecta con la indicada.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return {@code true} si ambas hitboxes se intersectan
     */
    boolean intersects(Hitbox hitbox);

    /**
     * Reposiciona esta hitbox.
     *
     * @param x nueva coordenada horizontal de su punto de referencia
     * @param y nueva coordenada vertical de su punto de referencia
     */
    void reposition(double x, double y);
}
