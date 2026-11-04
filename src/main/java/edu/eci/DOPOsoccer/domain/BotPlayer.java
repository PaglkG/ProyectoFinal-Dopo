package edu.eci.DOPOsoccer.domain;

import java.util.Set;
import edu.eci.DOPOsoccer.domain.enums.Action;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class BotPlayer extends SessionParticipant {

    /**
     * Creates a bot participant.
     */
    // Inicializa un participante controlado por la máquina.
    public BotPlayer() {
    }

    /**
     * Returns the actions selected by the bot for the field.
     */
    // Devuelve las acciones que decide realizar el bot en la cancha.
    public Set<Action> decideAction(SoccerField field) {
        return null;
    }
}
