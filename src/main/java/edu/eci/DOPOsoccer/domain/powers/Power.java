package edu.eci.DOPOSoccer.domain.powers;

import edu.eci.DOPOSoccer.domain.effects.StatusEffect;

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
    public boolean isAvailable() {
        return false;
    }

    /**
     * Marks the power as stored.
     */
    public void markStored() {
    }

    /**
     * Marks the power as used.
     */
    public void markUsed() {
    }

    /**
     * Sets the power position to the specified coordinates.
     */
    public void respawn(double x, double y) {
    }

    /**
     * Creates the status effect associated with the power.
     */
    public StatusEffect createEffect() {
        return null;
    }

    /**
     * Indicates whether the power activates when picked up.
     */
    public abstract boolean activatesOnPickup();
}
