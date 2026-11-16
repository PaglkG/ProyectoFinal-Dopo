package domain;

import java.util.Map;
import java.util.HashMap;

/**
 * Representa las estadísticas del jugador y los puntos disponibles.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Stats {

    private int points = 0;
    private Map<String, Integer> action = new HashMap<>(Map.of(
            "speed", 1,
            "goal", 1,
            "kick", 1,
            "jump", 1));

    /**
     * Punto de extensión para mejorar una estadística. Actualmente no modifica
     * el mapa de estadísticas ni los puntos.
     *
     * @param stats nombre de la estadística que se mejorará
     */
    public void ImproveStatistics(String stats) {
    }

    /** Punto de extensión para incrementar la clave {@code speed}; sin implementación actual. */
    private void aumentSpeed() {
    }

    /** Punto de extensión para incrementar la clave {@code goal}; sin implementación actual. */
    private void aumentGoal() {
    }

    /** Punto de extensión para incrementar la clave {@code kick}; sin implementación actual. */
    private void aumentKick() {
    }

    /** Punto de extensión para incrementar {@code points}; sin implementación actual. */
    private void aumentPoints() {
    }

    /** Punto de extensión para incrementar la clave {@code jump}; sin implementación actual. */
    private void aumentJump() {
    }
}
