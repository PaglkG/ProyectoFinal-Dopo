package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.exception.GameException;
import domain.rules.ByGoals;
import domain.rules.ByTime;
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void startSessionReportsAnUnknownSessionClassAndDoesNotStoreASession() throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), "missing.session.Class", ByGoals.class.getName(), false));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionReportsTheCustomConstructorExceptionWhenTheSignatureIsMissing()
            throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), SessionClassWithoutExpectedConstructor.class.getName(), ByGoals.class.getName(), false));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionReportsTheCustomExceptionWhenTheClassIsNotAGameSession()
            throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), SessionClassWithThreeArgumentConstructor.class.getName(), "rules", false));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionMapsAnInaccessibleConstructorToTheCustomConstructorException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), SessionClassWithPrivateConstructor.class.getName(), "rules", false));

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
    void startSessionCreatesAndStoresAPlayerVsBotSession() throws GameException, ReflectiveOperationException {
        game.startSession(List.of("5"), PlayerVsBot.class.getName(), ByGoals.class.getName(), true);

        assertEquals(1, sessions(game).size());
        assertInstanceOf(PlayerVsBot.class, sessions(game).get(0));
    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsPlayerSession() throws GameException, ReflectiveOperationException {
        game.startSession(List.of("10"), PlayerVsPlayer.class.getName(), ByTime.class.getName(), false);

        assertEquals(1, sessions(game).size());
        assertInstanceOf(PlayerVsPlayer.class, sessions(game).get(0));
    }

    @SuppressWarnings("unchecked")
    private static List<GameSession> sessions(Game game) throws ReflectiveOperationException {
        Field sessions = Game.class.getDeclaredField("sessions");
        sessions.setAccessible(true);
        return (List<GameSession>) sessions.get(game);
    }

}
