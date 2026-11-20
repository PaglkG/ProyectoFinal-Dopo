package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.GameException;
import domain.ByGoals;
import domain.ByTime;
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        GameSessionTestSupport.resetSingleton();
        game = new Game();
    }

    @Test
    void startSessionReportsAnUnknownSessionClassAndDoesNotStoreASession() throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.BlueAura", "domain.Lightning"),
                        "missing.session.Class", ByGoals.class.getName(), false));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertNull(managedSession(game));
    }

    @Test
    void startSessionReportsTheCustomConstructorExceptionWhenTheSignatureIsMissing()
            throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.BlueAura", "domain.Lightning"),
                        SessionClassWithoutExpectedConstructor.class.getName(), ByGoals.class.getName(), false));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
        assertNull(managedSession(game));
    }

    @Test
    void startSessionReportsTheCustomExceptionWhenTheClassIsNotAGameSession()
            throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.BlueAura", "domain.Lightning"),
                        SessionClassWithThreeArgumentConstructor.class.getName(), "rules", false));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertNull(managedSession(game));
    }

    @Test
    void startSessionMapsAnInaccessibleConstructorToTheCustomConstructorException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.BlueAura", "domain.Lightning"),
                        SessionClassWithPrivateConstructor.class.getName(), "rules", false));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    @Test
    void startSessionWrapsAnInvalidPowerClassLikeOtherReflectiveConstructorFailures() throws ReflectiveOperationException {
        GameException exception = assertThrows(GameException.class,
                () -> game.startSession(List.of("5"), List.of("domain.Clumsy", "domain.Lightning"),
                        PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        InvocationTargetException invocation = (InvocationTargetException) exception.getCause();
        GameException cause = (GameException) invocation.getCause();
        assertEquals(GameException.NO_FOUND_SESSION, cause.getMessage());
        assertNull(managedSession(game));
    }

    @Test
    void startSessionRejectsAnIncompletePowerSelection() throws ReflectiveOperationException {
        GameException exception = assertThrows(GameException.class,
                () -> game.startSession(List.of("5"), List.of("domain.BlueAura"),
                        PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        assertNull(managedSession(game));
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

        private SessionClassWithPrivateConstructor(boolean player, String matchType, List<String> parameters,
                List<String> powers) throws GameException {
            super(player, matchType, parameters);
        }

    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsBotSession() throws GameException, ReflectiveOperationException {
        game.startSession(List.of("5"), List.of("domain.BlueAura", "domain.Lightning", "Clumsy"),
                PlayerVsBot.class.getName(), ByGoals.class.getName(), true);

        assertNotNull(managedSession(game));
        assertInstanceOf(PlayerVsBot.class, managedSession(game));
    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsPlayerSession() throws GameException, ReflectiveOperationException {
        game.startSession(List.of("10"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByTime.class.getName(), false);

        assertNotNull(managedSession(game));
        assertInstanceOf(PlayerVsPlayer.class, managedSession(game));
    }

    @Test
    void allSessionSubclassesShareTheFirstCreatedInstance() throws GameException, ReflectiveOperationException {
        GameSession first = game.startSession(List.of("10"),
                List.of("domain.BlueAura", "domain.Lightning"), PlayerVsPlayer.class.getName(),
                ByGoals.class.getName(), false);

        GameSession second = game.startSession(List.of("5"),
                List.of("domain.BlueAura", "domain.Lightning", "Clumsy"), PlayerVsBot.class.getName(),
                ByGoals.class.getName(), true);

        assertSame(first, second);
        assertInstanceOf(PlayerVsPlayer.class, second);
        assertNotNull(managedSession(game));
        assertEquals(PlayerVsPlayer.class.getName(), game.getSessionType());
    }

    @Test
    void closeSessionRemovesTheSingleManagedSession() throws GameException, ReflectiveOperationException {
        GameSession first = game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        GameSession second = game.startSession(
                List.of("10"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), false);

        game.closeSession();

        assertSame(first, second);
        assertNull(managedSession(game));

        assertSame(first, game.startSession(List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));
        assertNotNull(managedSession(game));
    }

    @Test
    void startSessionReusesTheSameSessionAndItsStatistics() throws GameException {
        GameSession first = game.startSession(
                List.of("1"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        first.goal("right");
        Stats playerOne = first.getParticipantsRight().get(0).getStats();
        playerOne.improveStatistics("speed");
        game.closeSession();

        GameSession second = game.startSession(
                List.of("1"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        assertSame(playerOne, second.getParticipantsRight().get(0).getStats());
        assertSame(first, second);
        assertEquals(2, second.getParticipantsRight().get(0).getStats().getStatistic("speed"));
        assertEquals(0, second.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(0, second.getParticipantsLeft().get(0).getStats().getPoints());

        assertSame(second, game.startSession(
                List.of("1"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));
        assertEquals(0, playerOne.getPoints());
    }

    @Test
    void resetSessionRestartsOnlyTheRoundOfTheManagedSingleton() throws Exception {
        game.startSession(List.of("5"),
                List.of("domain.BlueAura", "domain.Lightning"), PlayerVsPlayer.class.getName(),
                ByGoals.class.getName(), true);
        GameSession session = managedSession(game);
        session.goal("right");
        game.setBallPosition(0, 80, 50);
        game.applyBallImpulse(0, 6, -2);
        game.setParticipantPosition("right:0", 100, 20);

        game.resetSession();

        Game.SessionView view = game.inspectSession();
        assertEquals(1, view.goalsRight());
        assertEquals(225, view.balls().get(0).x());
        assertEquals(75, view.balls().get(0).y());
        assertEquals(0, view.balls().get(0).velocityX());
        assertEquals(0, view.balls().get(0).velocityY());
        assertEquals(326.25, view.participants().get(1).x());
    }

    @Test
    void movePlayerUsesTheSingletonAndParticipantIdentifier() throws Exception {
        GameSession session = game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        SessionParticipant participant = session.getParticipantsRight().get(0);
        participant.resetPosition(100, 20);

        game.movePlayer("right:0", true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(20, participant.getHitbox().getY(), 1e-9);
    }

    @Test
    void improveStatisticsDelegatesToTheSelectedParticipant() throws Exception {
        GameSession session = game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        Stats rightStats = session.getParticipantsRight().get(0).getStats();
        rightStats.awardWin();

        game.improveStatistics(true, "kick");

        assertEquals(2, rightStats.getStatistic("kick"));
        assertEquals(0, rightStats.getPoints());
        assertEquals(1, session.getParticipantsLeft().get(0).getStats().getStatistic("kick"));
    }

    @Test
    void sessionViewExposesImmutableRenderingStateWithoutSessionObjects() throws Exception {
        game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        Game.SessionView view = game.inspectSession();

        assertEquals(450, view.court().width());
        assertEquals(150, view.court().height());
        assertEquals(List.of("left:0", "right:0"),
                view.participants().stream().map(Game.ParticipantView::id).toList());
        assertEquals(1, view.balls().size());
        assertThrows(UnsupportedOperationException.class, () -> view.balls().clear());
    }

    @Test
    void facadeMovementUsesParticipantIdentifierForSingleton() throws Exception {
        game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        game.setParticipantPosition("right:0", 100, 20);

        game.movePlayer("right:0", true);

        Game.ParticipantView participant = game.inspectSession().participants().get(1);
        assertEquals(102.5, participant.x(), 1e-9);
        assertEquals(20, participant.y(), 1e-9);
    }

    @Test
    void facadeKickReturnsWhetherTheBallWasReachedAndUpdatesItsSnapshot() throws Exception {
        game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        Game.ParticipantView right = game.inspectSession().participants().get(1);
        game.setBallPosition(0, right.x() + right.width() / 2, right.y() + right.height() / 2);

        assertTrue(game.kickPlayer("right:0"));

        Game.BallView ball = game.inspectSession().balls().get(0);
        assertEquals(-150, ball.velocityX());
        assertEquals(-50, ball.velocityY());
    }

    @Test
    void facadeBallSetupUpdatesPositionAndVelocityInTheSnapshot() throws Exception {
        game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        game.setBallPosition(0, 80, 50);

        game.applyBallImpulse(0, 6, -2);

        Game.BallView ball = game.inspectSession().balls().get(0);
        assertEquals(80, ball.x());
        assertEquals(50, ball.y());
        assertEquals(6, ball.velocityX());
        assertEquals(-2, ball.velocityY());
    }

    @Test
    void facadeUpdateAdvancesTheSingleton() throws Exception {
        game.startSession(
                List.of("5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        game.updateGame();

        assertEquals(1.0 / 60, game.inspectSession().elapsedSeconds(), 1e-9);
    }

    private static GameSession managedSession(Game game) throws ReflectiveOperationException {
        Field session = Game.class.getDeclaredField("session");
        session.setAccessible(true);
        return (GameSession) session.get(game);
    }

}
