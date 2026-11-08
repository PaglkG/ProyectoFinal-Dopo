package edu.eci.DOPOSoccer.domain.powers;

/**
 * Poder que puede reaparecer en una posición de la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class Power {

    /** Crea un poder. */
    public Power() {
    }

    /**
     * Reubica el poder.
     * @param x coordenada horizontal
     * @param y coordenada vertical
     */
    public void respawn(double x, double y) {
    }
}
