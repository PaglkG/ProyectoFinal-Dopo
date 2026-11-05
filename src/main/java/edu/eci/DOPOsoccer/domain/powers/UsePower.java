package edu.eci.DOPOSoccer.domain.powers;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class UsePower extends Power {

    /**
     * Indicates whether the power activates when picked up.
     */
    @Override
    public boolean activatesOnPickup() {
        return false;
    }
}
