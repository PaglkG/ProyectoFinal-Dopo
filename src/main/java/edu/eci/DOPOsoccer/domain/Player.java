package edu.eci.DOPOSoccer.domain;

import java.util.Set;

import edu.eci.DOPOSoccer.domain.configuration.KeyBindings;
import edu.eci.DOPOSoccer.domain.enums.Action;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Player extends SessionParticipant {

    private User user;

    /**
     * Creates a player associated with a user.
     */
    public Player(User user) {
    }

    /**
     * Registers a pressed keyboard key.
     */
    public void keyPressed(int key) {
    }

    /**
     * Registers a released keyboard key.
     */
    public void keyReleased(int key) {
    }

    /**
     * Returns the actions derived from the player input.
     */
    public Set<Action> decideAction(SoccerField field) {
        return null;
    }

    /**
     * Checks whether two key configurations conflict.
     */
    private boolean conflictsWith(KeyBindings other) {
        return false;
    }
}
