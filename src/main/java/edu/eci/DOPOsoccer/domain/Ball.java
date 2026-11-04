package edu.eci.DOPOsoccer.domain;

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
    // Actualiza el estado del balón según el tiempo transcurrido.
    public void update(double dt) {
    }

    /**
     * Changes the horizontal direction of the ball.
     */
    // Invierte la dirección horizontal del balón.
    public void bounceX() {
    }

    /**
     * Changes the vertical direction of the ball.
     */
    // Invierte la dirección vertical del balón.
    public void bounceY() {
    }

    /**
     * Applies the velocity produced by a kick.
     */
    // Asigna al balón la velocidad producida por una patada.
    public void kick(double vx, double vy) {
    }

    /**
     * Places the ball at the specified coordinates.
     */
    // Coloca el balón en las coordenadas indicadas.
    public void resetTo(double x, double y) {
    }
}
