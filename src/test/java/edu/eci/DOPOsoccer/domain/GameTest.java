package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.rules.ByGoals;
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

    @Test
    void startSessionReportsTheCustomConstructorExceptionWhenTheSignatureIsMissing() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), String.class.getName(), ByGoals.class.getName()));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
        assertEquals(0, game.sessions.size());
    }

    @Test
    void startSessionPropagatesIllegalArgumentWhenTheReflectiveArgumentsDoNotMatch() {
        assertThrows(
                IllegalArgumentException.class,
                () -> game.startSession(List.of(), SessionClassWithThreeArgumentConstructor.class.getName(), "rules"));
    }

    @Test
    void startSessionMapsAnInaccessibleConstructorToTheCustomConstructorException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), SessionClassWithPrivateConstructor.class.getName(), "rules"));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    /** Clase auxiliar para detectar el número incorrecto de argumentos en la invocación reflectiva. */
    public static class SessionClassWithThreeArgumentConstructor {

        public SessionClassWithThreeArgumentConstructor(String matchType, List<String> parameters, String extra) {
        }
    }

    /** Clase auxiliar con constructor privado para cubrir el manejo de IllegalAccessException. */
    public static class SessionClassWithPrivateConstructor {

        private SessionClassWithPrivateConstructor(String matchType, List<String> parameters, String extra) {
        }
    }
}
