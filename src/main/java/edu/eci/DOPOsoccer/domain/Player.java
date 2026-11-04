package edu.eci.DOPOsoccer.domain;

import java.util.Set;
import edu.eci.DOPOsoccer.domain.configuration.KeyBindings;
import edu.eci.DOPOsoccer.domain.enums.Action;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Player extends SessionParticipant {

    private User user;

    /**
     * Creates a player associated with a user.
     */
    // Inicializa un jugador asociado al usuario recibido.
    public Player(User user) {
    }

    /**
     * Registers a pressed keyboard key.
     */
    // Registra la tecla que ha sido presionada.
    public void keyPressed(int key) {
    }

    /**
     * Registers a released keyboard key.
     */
    // Registra la tecla que ha sido liberada.
    public void keyReleased(int key) {
    }

    /**
     * Returns the actions derived from the player input.
     */
    // Devuelve las acciones determinadas por los controles del jugador.
    public Set<Action> decideAction(SoccerField field) {
        return null;
    }

    /**
     * Checks whether two key configurations conflict.
     */
    // Comprueba si dos configuraciones de teclas entran en conflicto.
    private boolean conflictsWith(KeyBindings other) {
        return false;
    }
}
