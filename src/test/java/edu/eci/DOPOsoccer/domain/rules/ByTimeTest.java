package edu.eci.DOPOSoccer.domain.rules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.exception.GameException;

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
}
