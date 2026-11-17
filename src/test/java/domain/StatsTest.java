package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import domain.exception.GameException;

/** Pruebas de niveles de habilidad y puntos de mejora. */
class StatsTest {

    @Test
    void startsAllUpgradeableStatisticsAtOneAndPointsAtZero() throws ReflectiveOperationException {
        Stats stats = new Stats();

        @SuppressWarnings("unchecked")
        Map<String, Integer> action = (Map<String, Integer>) readField(stats, "action");

        assertEquals(Map.of("speed", 1, "goal", 1, "kick", 1, "jump", 1), action);
        assertEquals(0, readField(stats, "points"));
    }

    @Test
    void eachWinAddsFivePoints() {
        Stats stats = new Stats();

        stats.awardWin();

        assertEquals(5, stats.getPoints());
    }

    @Test
    void eachValidUpgradeCostsFivePointsAndRaisesOneLevel() {
        for (String statistic : List.of("speed", "goal", "kick", "jump")) {
            Stats stats = new Stats();
            stats.awardWin();

            stats.ImproveStatistics(statistic);

            assertEquals(2, stats.getStatistic(statistic));
            assertEquals(0, stats.getPoints());
        }
    }

    @Test
    void upgradeWithoutFivePointsThrowsGameExceptionAndLeavesStateUnchanged() {
        Stats stats = new Stats();

        GameException exception = assertThrows(GameException.class, () -> stats.ImproveStatistics("speed"));

        assertEquals(GameException.INSUFFICIENT_STAT_POINTS, exception.getMessage());
        assertEquals(1, stats.getStatistic("speed"));
        assertEquals(0, stats.getPoints());
    }

    @Test
    void upgradeOfUnknownStatisticThrowsGameExceptionWithoutSpendingPoints() {
        Stats stats = new Stats();
        stats.awardWin();

        GameException exception = assertThrows(GameException.class, () -> stats.ImproveStatistics("stamina"));

        assertEquals(GameException.UNKNOWN_STAT, exception.getMessage());
        assertEquals(5, stats.getPoints());
    }

    @Test
    void kickDamageStartsAtTenAndGrowsFiveAfterStrengthUpgrade() {
        Stats stats = new Stats();

        assertEquals(10, stats.getKickDamage());
        stats.awardWin();
        stats.ImproveStatistics("kick");

        assertEquals(15, stats.getKickDamage());
    }

    private static Object readField(Stats stats, String fieldName) throws ReflectiveOperationException {
        Field field = Stats.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(stats);
    }
}
