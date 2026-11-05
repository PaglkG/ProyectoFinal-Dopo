package edu.eci.DOPOSoccer.domain.rules;

import java.util.List;

import edu.eci.DOPOSoccer.domain.SessionParticipant;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public abstract class TypeOfMatche {

    private List<Rule> rules;

    /**
     * Creates a match type.
     */
    public TypeOfMatche() {
    }

    /**
     * Adds a rule by its identifier.
     */
    public void addRule(String rule) {
    }

    /**
     * Adds the specified rule.
     */
    public void addRule(Rule rule) {
    }

    /**
     * Applies a rule to a participant.
     */
    public void apply(SessionParticipant player, Rule rule) {
    }

    /**
     * Removes a rule effect from a participant.
     */
    public void delete(SessionParticipant player, Rule rule) {
    }

    /**
     * Applies the rules associated with this match type.
     */
    public void applyTypeGame() {
    }
}
