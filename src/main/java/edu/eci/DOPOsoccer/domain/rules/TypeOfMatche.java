package edu.eci.DOPOsoccer.domain.rules;

import java.util.List;
import edu.eci.DOPOsoccer.domain.SessionParticipant;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public abstract class TypeOfMatche {

    private List<Rule> rules;

    /**
     * Creates a match type.
     */
    // Inicializa un tipo de partido.
    public TypeOfMatche() {
    }

    /**
     * Adds a rule by its identifier.
     */
    // Agrega una regla a partir de su identificador.
    public void addRule(String rule) {
    }

    /**
     * Adds the specified rule.
     */
    // Agrega la regla indicada.
    public void addRule(Rule rule) {
    }

    /**
     * Applies a rule to a participant.
     */
    // Aplica una regla a un participante.
    public void apply(SessionParticipant player, Rule rule) {
    }

    /**
     * Removes a rule effect from a participant.
     */
    // Elimina el efecto de una regla de un participante.
    public void delete(SessionParticipant player, Rule rule) {
    }

    /**
     * Applies the rules associated with this match type.
     */
    // Aplica las reglas asociadas a este tipo de partido.
    public void applyTypeGame() {
    }
}
