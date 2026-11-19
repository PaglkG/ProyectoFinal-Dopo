package domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import domain.GameException;

/** Pruebas de las validaciones implementadas actualmente en {@link ByTime}. */
class ByTimeTest {

    @Test
    void constructorAcceptsNonNegativeMinutes() {
        assertDoesNotThrow(() -> new ByTime(List.of("5")));
    }

    @Test
    void constructorRejectsNegativeMinutes() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new ByTime(List.of("-1")));

        assertEquals(GameException.MINUTES_NOT_NEGATIVE, exception.getMessage());
    }

    @Test
    void constructorRejectsNonNumericMinutes() {
        assertEquals(GameException.INVALID_RULE_PARAMETERS,
                assertThrows(GameException.class, () -> new ByTime(List.of("five"))).getMessage());
    }

    @Test
    void constructorRejectsMissingMinutes() {
        assertEquals(GameException.INVALID_RULE_PARAMETERS,
                assertThrows(GameException.class, () -> new ByTime(List.of())).getMessage());
    }

    @Test
    void checkRejectsANonNumericElapsedTime() {
        ByTime match = new ByTime(List.of("5"));

        assertEquals(GameException.INVALID_SCORE_PARAMETERS,
                assertThrows(GameException.class, () -> match.check(List.of("2", "1", "not-a-time")))
                        .getMessage());
    }

    @Test
    void checkRejectsMissingElapsedTime() {
        ByTime match = new ByTime(List.of("5"));

        assertEquals(GameException.INVALID_SCORE_PARAMETERS,
                assertThrows(GameException.class, () -> match.check(List.of("2", "1"))).getMessage());
    }

    @Test
    void checkContinuesBeforeTheTimeLimit() {
        ByTime match = new ByTime(List.of("5"));

        assertTrue(match.check(List.of("2", "1", "4")));
    }

    @Test
    void checkContinuesInSuddenDeathWhenTheScoreIsTiedAtTheTimeLimit() {
        ByTime match = new ByTime(List.of("5"));

        assertTrue(match.check(List.of("2", "2", "5")));
    }

    @Test
    void checkActivatesTheSuddenDeathStateForATieAtTheTimeLimit() throws ReflectiveOperationException {
        ByTime match = new ByTime(List.of("5"));

        match.check(List.of("2", "2", "5"));

        Field suddenDeath = ByTime.class.getDeclaredField("suddenDeath");
        suddenDeath.setAccessible(true);
        assertTrue(suddenDeath.getBoolean(match));
    }

    @Test
    void suddenDeathStateClearsWhenAWinningScoreEndsTheMatch()
            throws ReflectiveOperationException {
        ByTime match = new ByTime(List.of("5"));
        match.check(List.of("2", "2", "5"));

        assertFalse(match.check(List.of("3", "2", "6")));

        Field suddenDeath = ByTime.class.getDeclaredField("suddenDeath");
        suddenDeath.setAccessible(true);
        assertFalse(suddenDeath.getBoolean(match));
    }

    @Test
    void checkEndsTheMatchAtTheTimeLimitWhenThereIsAWinner() {
        ByTime match = new ByTime(List.of("5"));

        assertFalse(match.check(List.of("3", "2", "5")));
    }

    @Test
    void checkContinuesInSuddenDeathAfterTheTimeLimitWhenTheScoreIsTied() {
        ByTime match = new ByTime(List.of("5"));

        assertTrue(match.check(List.of("2", "2", "7")));
    }

    @Test
    void checkEndsTheMatchAfterTheTimeLimitWhenThereIsAWinner() {
        ByTime match = new ByTime(List.of("5"));

        assertFalse(match.check(List.of("3", "2", "7")));
    }

    @Test
    void checkRejectsNonNumericScores() {
        ByTime match = new ByTime(List.of("5"));

        assertEquals(GameException.INVALID_SCORE_PARAMETERS,
                assertThrows(GameException.class, () -> match.check(List.of("x", "2", "5"))).getMessage());
    }
}
