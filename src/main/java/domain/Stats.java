package domain;

import java.util.Map;
import java.util.HashMap;
import domain.GameException;

/**
 * Representa las estadísticas del jugador y los puntos disponibles.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Stats {

    public static final int POINTS_PER_WIN = 5;
    public static final int POINTS_PER_UPGRADE = 5;
    public static final int KICK_BASE_DAMAGE = 10;
    public static final int KICK_DAMAGE_PER_LEVEL = 5;

    private int points = 0;
    private Map<String, Integer> action = new HashMap<>(Map.of(
            "speed", 1,
            "goal", 1,
            "kick", 1,
            "jump", 1));

    /** @return nivel actual de velocidad del participante */
    public int getSpeed() {
        return action.get("speed");
    }

    /** @return daño de patada: 10 en nivel 1 y 5 adicionales por nivel */
    public int getKickDamage() {
        return KICK_BASE_DAMAGE + (action.get("kick") - 1) * KICK_DAMAGE_PER_LEVEL;
    }

    /** @return puntos disponibles para mejorar habilidades */
    public int getPoints() {
        return points;
    }

    /** @return nivel actual de la habilidad indicada */
    public int getStatistic(String statistic) {
        Integer value = action.get(statistic);
        if (value == null) {
            throw new GameException(GameException.UNKNOWN_STAT);
        }
        return value;
    }

    /** Otorga los cinco puntos que corresponden a una victoria. */
    public void awardWin() {
        points += POINTS_PER_WIN;
    }

    /**
     * Mejora un nivel la estadística indicada y descuenta su costo.
     *
     * @param stats nombre de la estadística que se mejorará
     * @throws GameException si la habilidad no existe o faltan puntos
     */
    public void improveStatistics(String stats) {
        if (!action.containsKey(stats)) {
            throw new GameException(GameException.UNKNOWN_STAT);
        }
        if (points < POINTS_PER_UPGRADE) {
            throw new GameException(GameException.INSUFFICIENT_STAT_POINTS);
        }
        switch (stats) {
            case "speed" -> increaseSpeed();
            case "goal" -> increaseGoals();
            case "kick" -> increaseKick();
            case "jump" -> increaseJump();
            default -> throw new GameException(GameException.UNKNOWN_STAT);
        }
        points -= POINTS_PER_UPGRADE;
    }

    /** Incrementa la clave {@code speed}. */
    private void increaseSpeed() {
        action.put("speed", action.get("speed") + 1);
    }

    /** Incrementa la clave {@code goal}. */
    private void increaseGoals() {
        action.put("goal", action.get("goal") + 1);
    }

    /** Incrementa la clave {@code kick}. */
    private void increaseKick() {
        action.put("kick", action.get("kick") + 1);
    }

    /** Incrementa la clave {@code jump}. */
    private void increaseJump() {
        action.put("jump", action.get("jump") + 1);
    }
}
