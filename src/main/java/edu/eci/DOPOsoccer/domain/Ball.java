package edu.eci.DOPOSoccer.domain;

/**
 * Representa el balón, su estado físico, posición y velocidad.
 * La guía establece gravedad y rebotes contra suelo, arcos y límites de la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Ball {

    private boolean physical;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double radius;

    /** Crea el balón. */
    public Ball() {
    }

    /**
     * Actualiza la posición física según el tiempo transcurrido.
     * @param dt intervalo de actualización
     */
    public void update(double dt) {
    }

    /** Invierte la dirección horizontal del movimiento. */
    public void bounceX() {
    }

    /** Invierte la dirección vertical del movimiento. */
    public void bounceY() {
    }

    /**
     * Asigna la velocidad producida por un golpe.
     * @param vx componente horizontal
     * @param vy componente vertical
     */
    public void kick(double vx, double vy) {
    }

    /**
     * Restablece la posición del balón.
     * @param x coordenada horizontal
     * @param y coordenada vertical
     */
    public void resetTo(double x, double y) {
    }
}
