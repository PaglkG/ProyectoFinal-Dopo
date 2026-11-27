package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Pruebas de niveles de habilidad y puntos de mejora. */
class StatsTest {

    @Test
    void startsAllUpgradeableStatisticsAtLevelOneWithNoPointsAndInitialPrice() {
        Stats stats = new Stats();

        for (String statistic : List.of("speed", "jump", "kick", "survival")) {
            assertEquals(1, stats.getStatistic(statistic));
        }
        assertEquals(0, stats.getPoints());
        assertEquals(Stats.INITIAL_UPGRADE_COST, stats.getNextUpgradeCost());
    }

    @Test
    void awardsPointsAccordingToWinLossAndDrawResults() {
        Stats stats = new Stats();

        stats.awardWin();
        assertEquals(Stats.POINTS_PER_WIN, stats.getPoints());

        stats.awardLoss();
        assertEquals(Stats.POINTS_PER_WIN + Stats.POINTS_PER_LOSS, stats.getPoints());

        stats.awardDraw();
        assertEquals(Stats.POINTS_PER_WIN + Stats.POINTS_PER_LOSS + Stats.POINTS_PER_DRAW,
                stats.getPoints());
    }

    @Test
    void firstUpgradeCostsFiveHundredPointsAndRaisesOneLevel() {
        Stats stats = new Stats();
        awardWins(stats, 5);

        stats.improveStatistics("speed");

        assertEquals(2, stats.getStatistic("speed"));
        assertEquals(0, stats.getPoints());
        assertEquals(1_000, stats.getNextUpgradeCost());
    }

    @Test
    void upgradePriceDoublesAcrossDifferentStatistics() {
        Stats stats = new Stats();
        awardWins(stats, 15);

        stats.improveStatistics("speed");
        stats.improveStatistics("jump");

        assertEquals(2, stats.getStatistic("speed"));
        assertEquals(2, stats.getStatistic("jump"));
        assertEquals(0, stats.getPoints());
        assertEquals(2_000, stats.getNextUpgradeCost());
    }

    @Test
    void upgradeWithoutEnoughPointsThrowsAndLeavesStateUnchanged() {
        Stats stats = new Stats();
        stats.awardWin();

        GameException exception = assertThrows(GameException.class, () -> stats.improveStatistics("speed"));

        assertEquals(GameException.INSUFFICIENT_STAT_POINTS, exception.getMessage());
        assertEquals(1, stats.getStatistic("speed"));
        assertEquals(Stats.POINTS_PER_WIN, stats.getPoints());
        assertEquals(Stats.INITIAL_UPGRADE_COST, stats.getNextUpgradeCost());
    }

    @Test
    void upgradeOfUnknownStatisticThrowsWithoutSpendingPoints() {
        Stats stats = new Stats();
        awardWins(stats, 5);

        GameException exception = assertThrows(GameException.class, () -> stats.improveStatistics("stamina"));

        assertEquals(GameException.UNKNOWN_STAT, exception.getMessage());
        assertEquals(500, stats.getPoints());
        assertEquals(Stats.INITIAL_UPGRADE_COST, stats.getNextUpgradeCost());
    }

    @Test
    void kickDamageIncreasesByTenPercentForEachKickLevel() {
        Stats stats = new Stats();

        assertEquals(10, stats.getKickDamage());
        assertEquals(1.0, stats.getKickMultiplier());

        awardWins(stats, 5);
        stats.improveStatistics("kick");

        assertEquals(1.1, stats.getKickMultiplier(), 1e-9);
        assertEquals(11, stats.getKickDamage());
    }

    @Test
    void survivalUpgradeIncreasesMaximumLifeByTen() {
        Stats stats = new Stats();
        awardWins(stats, 5);

        stats.improveStatistics("survival");

        assertEquals(110, stats.getMaxLife());
    }

    @Test
    void maximumLevelDoesNotChangePointsOrNextUpgradePrice() {
        Stats stats = new Stats();
        awardWins(stats, 2_556);

        for (int level = 1; level < Stats.MAX_STAT_LEVEL; level++) {
            stats.improveStatistics("kick");
        }

        int remainingPoints = stats.getPoints();
        int nextUpgradeCost = stats.getNextUpgradeCost();
        stats.improveStatistics("kick");

        assertEquals(Stats.MAX_STAT_LEVEL, stats.getStatistic("kick"));
        assertEquals(19, stats.getKickDamage());
        assertEquals(remainingPoints, stats.getPoints());
        assertEquals(nextUpgradeCost, stats.getNextUpgradeCost());
    }

    private static void awardWins(Stats stats, int count) {
        for (int win = 0; win < count; win++) {
            stats.awardWin();
        }
    }
}
