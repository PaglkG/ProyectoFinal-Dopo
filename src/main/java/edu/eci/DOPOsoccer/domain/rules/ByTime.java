package edu.eci.DOPOSoccer.domain.rules;

/**
 * Tipo de partido con duración limitada y opción de muerte súbita.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByTime extends TypeOfMatche {

    private int minutes;
    private boolean suddenDeath;

    /** Crea una modalidad por tiempo. */
    public ByTime() {
    }
}
