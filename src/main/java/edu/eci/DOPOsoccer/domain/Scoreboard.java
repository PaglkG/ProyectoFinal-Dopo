package edu.eci.DOPOSoccer.domain;

import edu.eci.DOPOSoccer.domain.enums.Side;

/**
 * Mantiene los marcadores y las barras de poder de ambos lados.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Scoreboard {

    private int leftScore;
    private int rightScore;
    private int leftPowerBar;
    private int rightPowerBar;

    /** Crea un marcador. */
    public Scoreboard() {
    }

    /**
     * Incrementa el marcador del lado indicado. Un gol agrega un cuarto (25 %) a la barra de poder del lado anotador; la barra se llena en 10 segundos.
     * @param side lado que anotó
     */
    public void addGoal(Side side) {
    }

    /**
     * Devuelve el lado que lidera el partido.
     * @return lado ganador o empate
     */
    public Side getLeader() {
        return null;
    }
}
