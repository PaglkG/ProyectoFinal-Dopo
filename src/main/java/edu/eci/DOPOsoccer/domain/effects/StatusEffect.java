package edu.eci.DOPOSoccer.domain.effects;

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
    public void update() {
    }

    /**
     * Checks whether the effect has expired.
     */
    public boolean isExpired() {
        return false;
    }

    /**
     * Refreshes the effect duration.
     */
    public void refresh() {
    }

    /**
     * Returns the speed factor applied by the effect.
     */
    public double getSpeedFactor() {
        return 0.0;
    }

    /**
     * Returns the jump factor applied by the effect.
     */
    public double getJumpFactor() {
        return 0.0;
    }

    /**
     * Indicates whether the effect blocks movement.
     */
    public boolean blocksMovement() {
        return false;
    }
}
