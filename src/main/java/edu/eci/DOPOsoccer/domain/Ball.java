package edu.eci.DOPOSoccer.domain;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Ball {

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double radius;

    /**
     * Updates the ball state using the elapsed time.
     */
    public void update(double dt) {
    }

    /**
     * Changes the horizontal direction of the ball.
     */
    public void bounceX() {
    }

    /**
     * Changes the vertical direction of the ball.
     */
    public void bounceY() {
    }

    /**
     * Applies the velocity produced by a kick.
     */
    public void kick(double vx, double vy) {
    }

    /**
     * Places the ball at the specified coordinates.
     */
    public void resetTo(double x, double y) {
    }
}
