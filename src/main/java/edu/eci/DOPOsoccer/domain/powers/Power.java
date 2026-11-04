package edu.eci.DOPOsoccer.domain.powers;

import edu.eci.DOPOsoccer.domain.effects.StatusEffect;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public abstract class Power {

    private double x;
    private double t;
    private String effectType;
    private double effectDuration;

    /**
     * Checks whether the power is available.
     */
    // Comprueba si el poder está disponible.
    public boolean isAvailable() {
        return false;
    }

    /**
     * Marks the power as stored.
     */
    // Marca el poder como almacenado.
    public void markStored() {
    }

    /**
     * Marks the power as used.
     */
    // Marca el poder como utilizado.
    public void markUsed() {
    }

    /**
     * Sets the power position to the specified coordinates.
     */
    // Establece la posición del poder en las coordenadas indicadas.
    public void respawn(double x, double y) {
    }

    /**
     * Creates the status effect associated with the power.
     */
    // Crea el efecto de estado asociado al poder.
    public StatusEffect createEffect() {
        return null;
    }

    /**
     * Indicates whether the power activates when picked up.
     */
    // Indica si el poder se activa al recogerlo.
    public abstract boolean activatesOnPickup();
}
