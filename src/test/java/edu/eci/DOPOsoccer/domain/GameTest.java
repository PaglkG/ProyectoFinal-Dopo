package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.rules.ByGoals;
import edu.eci.DOPOSoccer.domain.rules.ByTime;
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
                () -> game.startSession(false, List.of(), "missing.session.Class", ByGoals.class.getName()));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, game.sessions.size());
    }

    @Test
    void startSessionReportsTheCustomConstructorExceptionWhenTheSignatureIsMissing() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(false, List.of(), SessionClassWithoutExpectedConstructor.class.getName(), ByGoals.class.getName()));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
        assertEquals(0, game.sessions.size());
    }

    @Test
    void startSessionReportsTheCustomExceptionWhenTheClassIsNotAGameSession() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(false, List.of(), SessionClassWithThreeArgumentConstructor.class.getName(), "rules"));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, game.sessions.size());
    }

    @Test
    void startSessionMapsAnInaccessibleConstructorToTheCustomConstructorException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(false, List.of(), SessionClassWithPrivateConstructor.class.getName(), "rules"));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    /** Clase auxiliar con la firma correcta, pero que no representa una sesión. */
    public static class SessionClassWithThreeArgumentConstructor {

        public SessionClassWithThreeArgumentConstructor(boolean player, String matchType, List<String> parameters) {
        }
    }

    /** Sesión válida sin el constructor requerido por la fábrica reflectiva. */
    public static class SessionClassWithoutExpectedConstructor extends GameSession {

        public SessionClassWithoutExpectedConstructor() throws GameException {
            super(false, ByGoals.class.getName(), List.of("5"));
        }

    }

    /** Clase auxiliar con constructor privado para cubrir el manejo de IllegalAccessException. */
    public static class SessionClassWithPrivateConstructor extends GameSession {

        private SessionClassWithPrivateConstructor(boolean player, String matchType, List<String> parameters) throws GameException {
            super(player, matchType, parameters);
        }

    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsBotSession() throws GameException {
        game.startSession(true, List.of("5"), PlayerVsBot.class.getName(), ByGoals.class.getName());

        assertEquals(1, game.sessions.size());
        assertInstanceOf(PlayerVsBot.class, game.sessions.get(0));
    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsPlayerSession() throws GameException {
        game.startSession(false, List.of("10"), PlayerVsPlayer.class.getName(), ByTime.class.getName());

        assertEquals(1, game.sessions.size());
        assertInstanceOf(PlayerVsPlayer.class, game.sessions.get(0));
    }

}
