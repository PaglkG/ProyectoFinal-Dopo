package edu.eci.DOPOSoccer.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa las dimensiones y los balones contenidos en la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class SoccerField {

    private double width;
    private double height = 150;
    private List<Ball> balls;

    /** Crea una cancha. */
    public SoccerField() {
        balls = new ArrayList<>();
        Ball  starBall = new Ball(width,height,2);
        balls.add(starBall);
        width = height * 3;

    }

    /**
     * Restablece los balones usando las coordenadas indicadas para ambos lados.
     *
     * @param xLeft coordenada horizontal del lado izquierdo
     * @param yLeft coordenada vertical del lado izquierdo
     * @param yRight coordenada vertical del lado derecho
     * @param xRight coordenada horizontal del lado derecho
     */
    public void resetBall(double width, double height) {
        for(Ball ball : balls) {
            ball.resetBall(width, height);
        }
    }
}
