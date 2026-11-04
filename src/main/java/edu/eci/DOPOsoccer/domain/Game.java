package edu.eci.DOPOsoccer.domain;

import edu.eci.DOPOsoccer.domain.rules.TypeOfMatche;

import java.util.List;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Game {

    private List<User> users;
    private List<GameSession> session;

    /**
     * Creates the game coordinator.
     */
    // Inicializa el administrador del juego.
    public Game() {
    }

    /**
     * Creates a user with the specified name and type.
     */
    // Crea un usuario con el nombre y tipo indicados.
    public boolean createUser(String name, String type) {
        return false;
    }

    /**
     * Creates a session with the selected users and match settings.
     */
    // Inicia una sesión con los usuarios y las modalidades indicadas.
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
    // Agrega las reglas seleccionadas a una sesión.
    public void addRules(GameSession session, TypeOfMatche rules) {
    }

    /**
     * Closes the specified game session.
     */
    // Cierra la sesión de juego indicada.
    public void closeSession(GameSession closeSession) {
    }

    /**
     * Changes a user configuration or key bindings.
     */
    // Cambia la configuración o los controles del usuario.
    public void changeConfiguration(
            User user,
            boolean configurationOrKeyBindings,
            String[] change) {
    }

    /**
     * Resets the specified game session.
     */
    // Reinicia la sesión indicada.
    public void resetSession(GameSession session) {
    }

    /**
     * Restores the default configuration of a user.
     */
    // Restablece la configuración predeterminada del usuario.
    public void resetConfiguration(User user) {
    }

    /**
     * Registers a goal for the specified side.
     */
    // Registra un gol para el lado indicado.
    public boolean gool(GameSession session, String side) {
        return false;
    }

    /**
     * Requests movement for the selected participant.
     */
    // Solicita el movimiento del participante indicado.
    public void movePlayer(
            GameSession session,
            SessionParticipant player,
            char direction) {
    }

    /**
     * Requests a jump for the selected participant.
     */
    // Solicita que el participante indicado salte.
    public void jumpPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Requests power activation for the selected participant.
     */
    // Solicita la activación del poder del participante indicado.
    public void powerPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Returns the session reference to be processed.
     */
    // Obtiene la referencia de la sesión que se va a procesar.
    private GameSession whatSession(GameSession session) {
        return null;
    }

    /**
     * Selects a user type from the provided identifier.
     */
    // Determina el tipo de usuario a partir del identificador recibido.
    private User whatTypeUser(String type) {
        return null;
    }

    /**
     * Checks the received user information.
     */
    // Verifica la información de los usuarios recibidos.
    private void verificationUser(String[] users) {
    }
}
