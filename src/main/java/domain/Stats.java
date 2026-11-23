package domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa las estadísticas de un jugador y los puntos disponibles para mejorarlas.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Stats implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Crea las estadísticas con habilidades en nivel inicial y sin puntos. */
    public Stats() { }

    /** Puntos de recompensa por una victoria. */
    public static final int POINTS_PER_WIN = 100;
    /** Puntos de recompensa por una derrota. */
    public static final int POINTS_PER_LOSS = 25;
    /** Puntos de recompensa por un empate. */
    public static final int POINTS_PER_DRAW = 25;
    /** Precio de la primera mejora de estadísticas. */
    public static final int INITIAL_UPGRADE_COST = 500;
    /** Nivel máximo que puede alcanzar una estadística. */
    public static final int MAX_STAT_LEVEL = 10;
    /** Vida base a partir de la que se calcula la supervivencia. */
    public static final int BASE_LIFE = 100;
    /** Daño base de una patada antes del multiplicador de nivel. */
    public static final int KICK_BASE_DAMAGE = 10;
    /** Incremento del multiplicador de patada por cada nivel de habilidad. */
    public static final double KICK_MULTIPLIER_PER_LEVEL = 0.10;

    private int points;
    private int nextUpgradeCost = INITIAL_UPGRADE_COST;
    private final Map<String, Integer> action = new HashMap<>(Map.of(
            "speed", 1,
            "jump", 1,
            "kick", 1,
            "survival", 1));

    /**
     * Devuelve el nivel actual de velocidad.
     *
     * @return nivel de velocidad
     */
    public int getSpeed() {
        return getStatistic("speed");
    }

    /**
     * Devuelve el multiplicador de daño de patada correspondiente al nivel de {@code kick}.
     *
     * @return multiplicador entre 1.0 y 1.9
     */
    public double getKickMultiplier() {
        return 1.0 + KICK_MULTIPLIER_PER_LEVEL * (getStatistic("kick") - 1);
    }

    /**
     * Devuelve el daño entero de una patada en función de {@code kick}.
     *
     * @return daño entre 10 y 19
     */
    public int getKickDamage() {
        return (int) Math.round(KICK_BASE_DAMAGE * getKickMultiplier());
    }

    /**
     * Devuelve la vida máxima determinada por el nivel de supervivencia.
     *
     * @return vida máxima entre 100 y 190
     */
    public int getMaxLife() {
        return BASE_LIFE + 10 * (getStatistic("survival") - 1);
    }

    /**
     * Devuelve los puntos disponibles.
     *
     * @return puntos del jugador
     */
    public int getPoints() {
        return points;
    }

    /**
     * Devuelve el precio de la siguiente mejora del jugador.
     *
     * @return precio de la siguiente compra
     */
    public int getNextUpgradeCost() {
        return nextUpgradeCost;
    }

    /**
     * Devuelve el nivel de una estadística.
     *
     * @param statistic nombre de la estadística
     * @return nivel actual
     * @throws GameException si la estadística no existe
     */
    public int getStatistic(String statistic) {
        Integer value = action.get(statistic);
        if (value == null) {
            throw new GameException(GameException.UNKNOWN_STAT);
        }
        return value;
    }

    /**
     * Añade los puntos de una victoria.
     */
    public void awardWin() {
        points += POINTS_PER_WIN;
    }

    /**
     * Añade los puntos de una derrota.
     */
    public void awardLoss() {
        points += POINTS_PER_LOSS;
    }

    /**
     * Añade los puntos que corresponden a un empate.
     */
    public void awardDraw() {
        points += POINTS_PER_DRAW;
    }

    /**
     * Mejora un nivel la estadística indicada y descuenta el precio vigente. Si
     * la estadística ya está en nivel 10, la solicitud no cambia el estado.
     * El precio se duplica tras cada compra, sin importar qué estadística se mejore.
     *
     * @param stats nombre de la estadística que se mejorará
     * @throws GameException si la habilidad no existe o faltan puntos
     */
    public void improveStatistics(String stats) {
        Integer level = action.get(stats);
        if (level == null) {
            throw new GameException(GameException.UNKNOWN_STAT);
        }
        if (level >= MAX_STAT_LEVEL) {
            return;
        }
        if (points < nextUpgradeCost) {
            throw new GameException(GameException.INSUFFICIENT_STAT_POINTS);
        }

        action.put(stats, level + 1);
        points -= nextUpgradeCost;
        nextUpgradeCost = nextUpgradeCost > Integer.MAX_VALUE / 2
                ? Integer.MAX_VALUE : nextUpgradeCost * 2;
    }
}
