package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import domain.GameException;

/** Pruebas del límite de goles y la validación implementada en {@link ByGoals}. */
class ByGoalsTest {

    @Test
    void constructorAcceptsNonNegativeGoalLimits() {
        assertFalse(new ByGoals(List.of("5")).check(List.of("4", "3")));
        assertTrue(new ByGoals(List.of("0")).check(List.of("0", "0")));
    }

    @Test
    void constructorRejectsNegativeGoalLimitWithTheDomainException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new ByGoals(List.of("-1")));

        assertEquals(GameException.GOALS_NOT_NEGATIVE, exception.getMessage());
    }

    @Test
    void checkEndsTheMatchWhenTheLeftSideReachesTheLimit() {
        assertTrue(new ByGoals(List.of("5")).check(List.of("5", "2")));
    }

    @Test
    void checkEndsTheMatchWhenTheRightSideReachesTheLimit() {
        assertTrue(new ByGoals(List.of("5")).check(List.of("2", "5")));
    }

    @Test
    void checkContinuesWhenNeitherSideReachesTheLimit() {
        assertFalse(new ByGoals(List.of("5")).check(List.of("4", "3")));
    }

    @Test
    void checkDoesNotTreatAScoreAboveTheLimitAsEqualToIt() {
        assertFalse(new ByGoals(List.of("5")).check(List.of("6", "2")));
    }

    @Test
    void constructorRejectsNonNumericGoalLimit() {
        assertEquals(GameException.INVALID_RULE_PARAMETERS,
                assertThrows(GameException.class, () -> new ByGoals(List.of("five"))).getMessage());
    }

    @Test
    void constructorRejectsMissingGoalLimit() {
        assertEquals(GameException.INVALID_RULE_PARAMETERS,
                assertThrows(GameException.class, () -> new ByGoals(List.of())).getMessage());
    }

    @Test
    void checkRejectsNonNumericScores() {
        assertEquals(GameException.INVALID_SCORE_PARAMETERS,
                assertThrows(GameException.class, () -> new ByGoals(List.of("5")).check(List.of("x", "2")))
                        .getMessage());
    }

    @Test
    void checkRejectsMissingScores() {
        assertEquals(GameException.INVALID_SCORE_PARAMETERS,
                assertThrows(GameException.class, () -> new ByGoals(List.of("5")).check(List.of("2")))
                        .getMessage());
    }
}
