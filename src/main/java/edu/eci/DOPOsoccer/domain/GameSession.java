package edu.eci.DOPOSoccer.domain;

import java.util.List;

import edu.eci.DOPOSoccer.domain.powers.Power;
import edu.eci.DOPOSoccer.domain.rules.Rule;
import edu.eci.DOPOSoccer.domain.rules.TypeOfMatche;

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
    public GameSession() {
    }

    /**
     * Starts the game session.
     */
    public void start() {
    }

    /**
     * Updates the game session using the elapsed time.
     */
    public void update(double dt) {
    }

    /**
     * Aborts the current game session.
     */
    public void abort() {
    }

    /**
     * Removes a participant from the session.
     */
    public void removeParticipant(SessionParticipant participant) {
    }

    /**
     * Requests movement for a participant.
     */
    public void movePlayer(SessionParticipant user, char direction) {
    }

    /**
     * Restarts the current round.
     */
    public void restartRound() {
    }

    /**
     * Applies a power selected by a participant.
     */
    public void applyPower(Power power, SessionParticipant picker) {
    }

    /**
     * Indicates whether the session has a winner.
     */
    public boolean getWinner() {
        return false;
    }

    /**
     * Checks the completion state of the session.
     */
    public void isFinished() {
    }

    /**
     * Handles the minimum participant condition.
     */
    public GameSession minParticipants() {
        return null;
    }

    /**
     * Assigns the rules for the selected match type.
     */
    public void assignRules(String typeMatche) {
    }

    /**
     * Adds a rule to the session.
     */
    public void addRule(Rule rule) {
    }

    /**
     * Registers a goal for the specified side.
     */
    public void gool(String side) {
    }

    /**
     * Requests a jump for the selected participant.
     */
    public void jumpPlayer(SessionParticipant player) {
    }

    /**
     * Requests power activation for the selected participant.
     */
    public void powerPlayer(SessionParticipant player) {
    }

    /**
     * Finds the rules associated with a game type.
     */
    private TypeOfMatche whatsRules(String typeGame) {
        return null;
    }
}
