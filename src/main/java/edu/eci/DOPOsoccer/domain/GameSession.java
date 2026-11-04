package edu.eci.DOPOsoccer.domain;

import edu.eci.DOPOsoccer.domain.rules.TypeOfMatche;

import java.util.List;
import edu.eci.DOPOsoccer.domain.powers.Power;
import edu.eci.DOPOsoccer.domain.rules.Rule;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class GameSession {

    private double timeLeft;
    private int yBaseRight;
    private int xBaseRight;
    private int yBaseLeft;
    private int xBaseLeft;

    private SoccerField field;
    private Scoreboard scoreboard;
    private List<SessionParticipant> participantsRight;
    private List<SessionParticipant> participantsLeft;
    private TypeOfMatche typeOfMatche;

    /**
     * Creates a game session.
     */
    // Inicializa una sesión de juego.
    public GameSession() {
    }

    /**
     * Starts the game session.
     */
    // Inicia la sesión de juego.
    public void start() {
    }

    /**
     * Updates the game session using the elapsed time.
     */
    // Actualiza la sesión según el tiempo transcurrido.
    public void update(double dt) {
    }

    /**
     * Aborts the current game session.
     */
    // Interrumpe la sesión de juego actual.
    public void abort() {
    }

    /**
     * Removes a participant from the session.
     */
    // Retira un participante de la sesión.
    public void removeParticipant(SessionParticipant participant) {
    }

    /**
     * Requests movement for a participant.
     */
    // Solicita el movimiento de un participante.
    public void movePlayer(SessionParticipant user, char direction) {
    }

    /**
     * Restarts the current round.
     */
    // Reinicia la ronda actual.
    public void restartRound() {
    }

    /**
     * Applies a power selected by a participant.
     */
    // Aplica el poder seleccionado por un participante.
    public void applyPower(Power power, SessionParticipant picker) {
    }

    /**
     * Indicates whether the session has a winner.
     */
    // Indica si la sesión ya tiene un ganador.
    public boolean getWinner() {
        return false;
    }

    /**
     * Checks the completion state of the session.
     */
    // Comprueba el estado de finalización de la sesión.
    public void isFinished() {
    }

    /**
     * Handles the minimum participant condition.
     */
    // Gestiona la condición de participantes mínimos.
    public GameSession minParticipants() {
        return null;
    }

    /**
     * Assigns the rules for the selected match type.
     */
    // Asigna las reglas correspondientes al tipo de partido.
    public void assignRules(String typeMatche) {
    }

    /**
     * Adds a rule to the session.
     */
    // Agrega una regla a la sesión.
    public void addRule(Rule rule) {
    }

    /**
     * Registers a goal for the specified side.
     */
    // Registra un gol para el lado indicado.
    public void gool(String side) {
    }

    /**
     * Requests a jump for the selected participant.
     */
    // Solicita que el participante indicado salte.
    public void jumpPlayer(SessionParticipant player) {
    }

    /**
     * Requests power activation for the selected participant.
     */
    // Solicita la activación del poder del participante indicado.
    public void powerPlayer(SessionParticipant player) {
    }

    /**
     * Finds the rules associated with a game type.
     */
    // Obtiene las reglas asociadas al tipo de juego.
    private TypeOfMatche whatsRules(String typeGame) {
        return null;
    }
}
