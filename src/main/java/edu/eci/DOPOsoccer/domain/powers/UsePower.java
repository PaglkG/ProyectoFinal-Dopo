package edu.eci.DOPOsoccer.domain.powers;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class UsePower extends Power {

    /**
     * Indicates whether the power activates when picked up.
     */
    @Override
    // Indica si el poder se activa al recogerlo.
    public boolean activatesOnPickup() {
        return false;
    }
}
