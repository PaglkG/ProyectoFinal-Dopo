package edu.eci.DOPOSoccer.domain;

/**
 * Estadísticas modificables del jugador y puntos disponibles.
 * La velocidad, el salto y la supervivencia usan el multiplicador
 * m = 1 + 0,10 × (estadística - 1).
 * La vida máxima es vidaMáxima = 100 × (1 + 0,10 × (supervivencia - 1)).
 * El multiplicador de patada es multiplicadorKick = 1 + 0,10 × (kick - 1).
 * El daño normal de patada es 10 × multiplicadorKick y la vida nueva es
 * max(0, vida actual - daño). La estadística Kick no cambia la fuerza con que
 * sale el balón. El daño de poder es dañoBasePoder × multiplicadorKick.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Stats {

    private int speed;
    private int side;
    private int goal;
    private double kick;
    private int survival;
    private int points;
}
