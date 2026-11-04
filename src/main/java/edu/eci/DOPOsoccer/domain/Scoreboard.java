package edu.eci.DOPOsoccer.domain;

import edu.eci.DOPOsoccer.domain.enums.Side;

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
    // Suma un gol al marcador del lado indicado.
    public void addGoal(Side side) {
    }

    /**
     * Returns the score for the specified side.
     */
    // Devuelve el marcador del lado indicado.
    public int getScore(Side side) {
        return 0;
    }

    /**
     * Checks whether both sides have the same score.
     */
    // Comprueba si ambos lados tienen el mismo marcador.
    public boolean isTied() {
        return false;
    }

    /**
     * Returns the side currently leading the match.
     */
    // Devuelve el lado que va ganando el partido.
    public Side getLeader() {
        return null;
    }

    /**
     * Resets the scoreboard values.
     */
    // Restablece los valores del marcador.
    public void reset() {
    }
}
