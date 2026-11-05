package edu.eci.DOPOSoccer.domain;

import java.util.List;
import java.util.Set;

import edu.eci.DOPOSoccer.domain.effects.StatusEffect;
import edu.eci.DOPOSoccer.domain.enums.Action;
import edu.eci.DOPOSoccer.domain.powers.Power;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public abstract class SessionParticipant {

    private int side;
    private double x;
    private double y;
    private int speed;
    private int jump;
    private int power;

    private Outfit outfit;
    private List<StatusEffect> status;

    /**
     * Creates a session participant.
     */
    public SessionParticipant() {
    }

    /**
     * Performs the actions received from the participant.
     */
    public void perform(Set<Action> actions) {
    }

    /**
     * Moves the participant in the selected direction.
     */
    public void move(Action dir) {
    }

    /**
     * Requests a participant jump.
     */
    public void jump() {
    }

    /**
     * Requests the use of the stored power.
     */
    public void power() {
    }

    /**
     * Kicks the specified ball.
     */
    public void kick(Ball ball) {
    }

    /**
     * Attempts to pick up the specified power.
     */
    public boolean pickUp(Power power) {
        return false;
    }

    /**
     * Indicates whether the participant can store a power.
     */
    public boolean canStorePower() {
        return false;
    }

    /**
     * Uses the power stored by the participant.
     */
    public void takeStoredPower() {
    }

    /**
     * Adds a status effect to the participant.
     */
    public void addEffect(StatusEffect effect) {
    }

    /**
     * Updates the participant effects using elapsed time.
     */
    public void updateEffects(double dt) {
    }

    /**
     * Removes the active effects from the participant.
     */
    public void clearEffects() {
    }

    /**
     * Sets the participant position to the specified coordinates.
     */
    public void resetPosition(double x, double y) {
    }

    /**
     * Returns the participant speed.
     */
    public int getSpeed() {
        return 0;
    }

    /**
     * Returns the participant jump power.
     */
    public int getJumpPower() {
        return 0;
    }

    /**
     * Indicates whether the participant can move.
     */
    public boolean canMove() {
        return false;
    }
}
