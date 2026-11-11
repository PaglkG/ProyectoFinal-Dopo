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
    private double velocityX = 0;
    private double velocityY = 0;
    private double radius;

    /** Crea el balón. */
    public Ball(double width, double height,double radius) {
        this.x =  width/2;
        this.y = height/2;
        this.radius = radius;
    }

    /**
     * Centra el balón según las dimensiones de la cancha y detiene su movimiento.
     *
     * @param width ancho de la cancha
     * @param height alto de la cancha
     */
    public void resetBall(double width, double height) {
        this.x =  width/2;
        this.y = height/2;
        this.velocityX = 0;
        this.velocityY = 0;
    }
}
