package edu.eci.DOPOSoccer.domain;

import java.util.List;

import edu.eci.DOPOSoccer.domain.rules.TypeOfMatche;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Game {

    private List<User> users;
    private List<GameSession> session;

    /**
     * Creates the game coordinator.
     */
    public Game() {
    }

    /**
     * Creates a user with the specified name and type.
     */
    public boolean createUser(String name, String type) {
        return false;
    }

    /**
     * Creates a session with the selected users and match settings.
     */
    public GameSession startSession(
            List<User> users,
            int bots,
            String typeGame,
            String typeMatche) {
        return null;
    }

    /**
     * Adds the selected rules to a game session.
     */
    public void addRules(GameSession session, TypeOfMatche rules) {
    }

    /**
     * Closes the specified game session.
     */
    public void closeSession(GameSession closeSession) {
    }

    /**
     * Changes a user configuration or key bindings.
     */
    public void changeConfiguration(
            User user,
            boolean configurationOrKeyBindings,
            String[] change) {
    }

    /**
     * Resets the specified game session.
     */
    public void resetSession(GameSession session) {
    }

    /**
     * Restores the default configuration of a user.
     */
    public void resetConfiguration(User user) {
    }

    /**
     * Registers a goal for the specified side.
     */
    public boolean gool(GameSession session, String side) {
        return false;
    }

    /**
     * Requests movement for the selected participant.
     */
    public void movePlayer(
            GameSession session,
            SessionParticipant player,
            char direction) {
    }

    /**
     * Requests a jump for the selected participant.
     */
    public void jumpPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Requests power activation for the selected participant.
     */
    public void powerPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Returns the session reference to be processed.
     */
    private GameSession whatSession(GameSession session) {
        return null;
    }

    /**
     * Selects a user type from the provided identifier.
     */
    private User whatTypeUser(String type) {
        return null;
    }

    /**
     * Checks the received user information.
     */
    private void verificationUser(String[] users) {
    }
}
