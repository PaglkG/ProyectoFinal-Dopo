package edu.eci.DOPOSoccer.domain.rules;

import java.util.List;
import edu.eci.DOPOSoccer.domain.SessionParticipant;

/**
 * Define un tipo de partido y las reglas que lo componen.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class TypeOfMatche {

    private List<Rule> rules;

    /** Crea un tipo de partido. */
    public TypeOfMatche() {
    }

    /**
     * Agrega la regla indicada.
     * @param rule regla que se agregará
     */
    public void addRule(Rule rule) {
    }

    /**
     * Elimina el efecto de una regla en un participante.
     * @param player participante
     * @param rule regla que se retirará
     */
    public void delete(SessionParticipant player, Rule rule) {
    }

    /** Aplica las reglas de este tipo de partido. */
    public void applyTypeGame() {
    }

    /** Comprueba las condiciones asociadas a este tipo de partido. */
    public void check() {
    }
}
