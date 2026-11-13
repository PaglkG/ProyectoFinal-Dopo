package domain.rules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import domain.exception.GameException;

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
        assertThrows(NumberFormatException.class, () -> new ByTime(List.of("five")));
    }

    @Test
    void constructorRejectsMissingMinutes() {
        assertThrows(IndexOutOfBoundsException.class, () -> new ByTime(List.of()));
    }

    @Test
    void checkRejectsANonNumericElapsedTime() {
        ByTime match = new ByTime(List.of("5"));

        assertThrows(NumberFormatException.class, () -> match.check(List.of("2", "1", "not-a-time")));
    }

    @Test
    void checkRejectsMissingElapsedTime() {
        ByTime match = new ByTime(List.of("5"));

        assertThrows(IndexOutOfBoundsException.class, () -> match.check(List.of("2", "1")));
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

        assertThrows(NumberFormatException.class, () -> match.check(List.of("x", "2", "5")));
    }
}
