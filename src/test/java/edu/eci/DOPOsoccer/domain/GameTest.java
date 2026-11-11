package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.configuration.Configuration;
import edu.eci.DOPOSoccer.domain.configuration.KeyBindings;
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

        @Override
        public void resetConfiguration(boolean player) {
        }
    }

    /** Clase auxiliar con constructor privado para cubrir el manejo de IllegalAccessException. */
    public static class SessionClassWithPrivateConstructor extends GameSession {

        private SessionClassWithPrivateConstructor(boolean player, String matchType, List<String> parameters) throws GameException {
            super(player, matchType, parameters);
        }

        @Override
        public void resetConfiguration(boolean player) {
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

    @Test
    void resetConfigurationForwardsTheSelectedPlayerToTheSession() throws GameException {
        ConfigurationTrackingSession session = new ConfigurationTrackingSession();
        game.sessions.add(session);

        game.resetConfiguration(false);

        assertFalse(session.selectedPlayer);
    }

    @Test
    void resetConfigurationRestoresOnlyPlayerOneThroughTheWholeSequence() throws Exception {
        PlayerVsPlayer session = createPlayerVsPlayerSession();
        Player player1 = (Player) session.participantsRight.get(0);
        Player player2 = (Player) session.participantsLeft.get(0);
        customizeConfiguration(player1);
        customizeConfiguration(player2);

        game.resetConfiguration(true);

        assertMatchesDefaults(player1, 1);
        assertRemainsCustomized(player2);
    }

    @Test
    void resetConfigurationRestoresOnlyPlayerTwoThroughTheWholeSequence() throws Exception {
        PlayerVsPlayer session = createPlayerVsPlayerSession();
        Player player1 = (Player) session.participantsRight.get(0);
        Player player2 = (Player) session.participantsLeft.get(0);
        customizeConfiguration(player1);
        customizeConfiguration(player2);

        game.resetConfiguration(false);

        assertRemainsCustomized(player1);
        assertMatchesDefaults(player2, 2);
    }

    private PlayerVsPlayer createPlayerVsPlayerSession() throws GameException {
        game.startSession(true, List.of("5"), PlayerVsPlayer.class.getName(), ByGoals.class.getName());
        return (PlayerVsPlayer) game.sessions.get(0);
    }

    private static void customizeConfiguration(Player player) throws ReflectiveOperationException {
        Object configuration = getField(player, "configuration");
        setField(configuration, "sound", false);
        setField(configuration, "music", false);
        KeyBindings bindings = (KeyBindings) getField(configuration, "keyBindings");
        setField(bindings, "right", "custom-right");
        setField(bindings, "left", "custom-left");
        setField(bindings, "jump", "custom-jump");
        setField(bindings, "power", "custom-power");
    }

    private static void assertMatchesDefaults(Player player, int user) throws ReflectiveOperationException {
        Object actualConfiguration = getField(player, "configuration");
        Configuration expectedConfiguration = new Configuration(user);
        assertEquals(getField(expectedConfiguration, "sound"), getField(actualConfiguration, "sound"));
        assertEquals(getField(expectedConfiguration, "music"), getField(actualConfiguration, "music"));
        KeyBindings actualBindings = (KeyBindings) getField(actualConfiguration, "keyBindings");
        KeyBindings expectedBindings = (KeyBindings) getField(expectedConfiguration, "keyBindings");
        assertBindingsEqual(expectedBindings, actualBindings);
    }

    private static void assertRemainsCustomized(Player player) throws ReflectiveOperationException {
        Object configuration = getField(player, "configuration");
        assertEquals(false, getField(configuration, "sound"));
        assertEquals(false, getField(configuration, "music"));
        KeyBindings bindings = (KeyBindings) getField(configuration, "keyBindings");
        assertEquals("custom-right", getField(bindings, "right"));
        assertEquals("custom-left", getField(bindings, "left"));
        assertEquals("custom-jump", getField(bindings, "jump"));
        assertEquals("custom-power", getField(bindings, "power"));
    }

    private static void assertBindingsEqual(KeyBindings expected, KeyBindings actual)
            throws ReflectiveOperationException {
        assertEquals(getField(expected, "right"), getField(actual, "right"));
        assertEquals(getField(expected, "left"), getField(actual, "left"));
        assertEquals(getField(expected, "jump"), getField(actual, "jump"));
        assertEquals(getField(expected, "power"), getField(actual, "power"));
    }

    private static Object getField(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class ConfigurationTrackingSession extends GameSession {
        private boolean selectedPlayer = true;

        ConfigurationTrackingSession() throws GameException {
            super(false, ByGoals.class.getName(), List.of("5"));
        }

        @Override
        public void resetConfiguration(boolean player) {
            selectedPlayer = player;
        }
    }
}
