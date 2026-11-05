package edu.eci.DOPOSoccer.domain;

import edu.eci.DOPOSoccer.domain.enums.Side;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Scoreboard {

    private int leftScore;
    private int rightScore;
    private int leftPowerBar;
    private int rightPowerBar;

    /**
     * Adds a goal to the score of the specified side.
     */
    public void addGoal(Side side) {
    }

    /**
     * Returns the score for the specified side.
     */
    public int getScore(Side side) {
        return 0;
    }

    /**
     * Checks whether both sides have the same score.
     */
    public boolean isTied() {
        return false;
    }

    /**
     * Returns the side currently leading the match.
     */
    public Side getLeader() {
        return null;
    }

    /**
     * Resets the scoreboard values.
     */
    public void reset() {
    }
}
