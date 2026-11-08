package edu.eci.DOPOSoccer.domain;

import edu.eci.DOPOSoccer.domain.powers.Power;

/**
 * Agrupa el país, la cabeza, el calzado y el poder especial del personaje.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Outfit {

    private String name;
    private String country;
    private Head head;
    private Shoe shoe;
    private Power specialPower;

    /**
     * Devuelve la cabeza del atuendo.
     * @return cabeza asociada
     */
    public Head getHead() {
        return null;
    }

    /**
     * Devuelve el calzado del atuendo.
     * @return calzado asociado
     */
    public Shoe getShoe() {
        return null;
    }
}
