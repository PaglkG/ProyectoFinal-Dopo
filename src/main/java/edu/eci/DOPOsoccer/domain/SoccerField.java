package edu.eci.DOPOSoccer.domain;

import java.util.List;

import edu.eci.DOPOSoccer.domain.enums.Side;
import edu.eci.DOPOSoccer.domain.powers.Power;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class SoccerField {

    private double width;
    private double height;
    private Ball ball;
    private List<Power> specialPower;

    /**
     * Updates the field state using the elapsed time.
     */
    public void update(double dt) {
    }

    /**
     * Resets the ball position.
     */
    public void resetBall() {
    }

    /**
     * Checks whether the ball has entered a goal.
     */
    public Side checkGoal() {
        return null;
    }

    /**
     * Finds a power touching the specified participant.
     */
    public Power findPowerTouching(SessionParticipant participant) {
        return null;
    }

    /**
     * Creates or selects a power to spawn on the field.
     */
    public Power spawnPower() {
        return null;
    }
}
