package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.rules.ByGoals;

/** Pruebas para las rutas de error implementadas en el constructor de sesión. */
class GameSessionTest {

    @Test
    void constructorReportsAnUnknownMatchTypeClass() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new GameSession(false, "missing.match.Type", List.of()));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    @Test
    void constructorReportsWhenTheMatchTypeHasNoDefaultConstructor() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new GameSession(false, ByGoals.class.getName(), List.of("5")));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    @Test
    void constructorCurrentlyFailsWhenParametersArePassedToANoArgumentConstructor() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new GameSession(false, String.class.getName(), List.of()));

        assertNotNull(exception.getMessage());
    }
}
