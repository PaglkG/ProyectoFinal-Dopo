package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.rules.ByGoals;

/** Pruebas del comportamiento implementado actualmente en {@link Game}. */
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void startSessionReportsAnUnknownSessionClassAndDoesNotStoreASession() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), "missing.session.Class", ByGoals.class.getName()));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, game.sessions.size());
    }
}
