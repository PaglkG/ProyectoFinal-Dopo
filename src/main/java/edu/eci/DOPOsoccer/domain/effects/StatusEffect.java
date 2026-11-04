package edu.eci.DOPOsoccer.domain.effects;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public abstract class StatusEffect {

    private String type;
    private double duration;
    private double remaining;

    /**
     * Updates the remaining effect duration.
     */
    // Actualiza el tiempo restante del efecto.
    public void update() {
    }

    /**
     * Checks whether the effect has expired.
     */
    // Comprueba si el efecto ha terminado.
    public boolean isExpired() {
        return false;
    }

    /**
     * Refreshes the effect duration.
     */
    // Renueva la duración del efecto.
    public void refresh() {
    }

    /**
     * Returns the speed factor applied by the effect.
     */
    // Devuelve el factor de velocidad aplicado por el efecto.
    public double getSpeedFactor() {
        return 0.0;
    }

    /**
     * Returns the jump factor applied by the effect.
     */
    // Devuelve el factor de salto aplicado por el efecto.
    public double getJumpFactor() {
        return 0.0;
    }

    /**
     * Indicates whether the effect blocks movement.
     */
    // Indica si el efecto bloquea el movimiento.
    public boolean blocksMovement() {
        return false;
    }
}
