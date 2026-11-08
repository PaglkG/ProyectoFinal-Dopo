package edu.eci.DOPOSoccer.domain;

import java.util.List;

/**
 * Representa las dimensiones y los balones contenidos en la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class SoccerField {

    private double width;
    private double height;
    private List<Ball> ball;

    /** Crea una cancha. */
    public SoccerField() {
    }

    /** Restablece la posición de los balones al centro de la cancha. */
    public void resetBall() {
    }
}
