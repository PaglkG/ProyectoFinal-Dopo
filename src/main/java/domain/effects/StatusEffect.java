package domain.effects;

/**
 * Efecto temporal que modifica el estado de un participante.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class StatusEffect {

    private String type;
    private double duration;
    private double remaining;

    /** Punto de extensión para actualizar la duración; actualmente no cambia {@code remaining}. */
    public void update() {
    }

    /**
     * Comprueba si el efecto terminó. Actualmente devuelve siempre {@code false}.
     * @return {@code false} en la implementación actual
     */
    public boolean isExpired() {
        return false;
    }

    /** Punto de extensión para renovar el efecto; actualmente no cambia su duración restante. */
    public void refresh() {
    }

    /**
     * Devuelve el multiplicador de velocidad aplicado por el efecto.
     * @return factor de velocidad
     */
    public double getSpeedFactor() {
        return 0.0;
    }

    /**
     * Devuelve el multiplicador de salto aplicado por el efecto.
     * @return factor de salto
     */
    public double getJumpFactor() {
        return 0.0;
    }

    /**
     * Indica si el efecto impide moverse.
     * @return indica si bloquea el movimiento
     */
    public boolean blocksMovement() {
        return false;
    }
}
