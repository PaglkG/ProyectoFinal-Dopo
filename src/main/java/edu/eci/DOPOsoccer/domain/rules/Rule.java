package edu.eci.DOPOSoccer.domain.rules;

import edu.eci.DOPOSoccer.domain.SessionParticipant;

/**
 * Regla que aplica un efecto a un participante.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Rule {

    private String name;

    /** Crea una regla sin parámetros. */
    public Rule() {
    }

    /**
     * Crea una regla con nombre y efecto.
     * @param name nombre de la regla
     * @param effect descripción del efecto
     */
    public Rule(String name, String effect) {
    }

    /**
     * Aplica el efecto de esta regla al participante.
     * @param player participante afectado
     */
    public void effect(SessionParticipant player) {
    }
}
