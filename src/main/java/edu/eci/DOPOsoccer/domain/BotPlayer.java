package edu.eci.DOPOSoccer.domain;

import java.util.Set;

import edu.eci.DOPOSoccer.domain.enums.Action;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class BotPlayer extends SessionParticipant {

    /**
     * Creates a bot participant.
     */
    public BotPlayer() {
    }

    /**
     * Returns the actions selected by the bot for the field.
     */
    public Set<Action> decideAction(SoccerField field) {
        return null;
    }
}
