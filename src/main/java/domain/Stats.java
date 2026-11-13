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
     * Mejora la estadística indicada.
     *
     * @param stats nombre de la estadística que se mejorará
     */
    public void ImproveStatistics(String stats) {
    }

    /** Incrementa la estadística de velocidad. */
    private void aumentSpeed() {
    }

    /** Incrementa la estadística de goles. */
    private void aumentGoal() {
    }

    /** Incrementa la estadística de patada. */
    private void aumentKick() {
    }

    /** Incrementa los puntos disponibles. */
    private void aumentPoints() {
    }

    /** Incrementa la estadística de salto. */
    private void aumentJump() {
    }
}
