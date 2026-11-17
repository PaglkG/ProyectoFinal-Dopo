package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
                () -> game.startSession(List.of(), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                        "missing.session.Class", ByGoals.class.getName(), false));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionReportsTheCustomConstructorExceptionWhenTheSignatureIsMissing()
            throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                        SessionClassWithoutExpectedConstructor.class.getName(), ByGoals.class.getName(), false));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionReportsTheCustomExceptionWhenTheClassIsNotAGameSession()
            throws ReflectiveOperationException {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                        SessionClassWithThreeArgumentConstructor.class.getName(), "rules", false));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionMapsAnInaccessibleConstructorToTheCustomConstructorException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> game.startSession(List.of(), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                        SessionClassWithPrivateConstructor.class.getName(), "rules", false));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    @Test
    void startSessionWrapsAnInvalidPowerClassLikeOtherReflectiveConstructorFailures() throws ReflectiveOperationException {
        GameException exception = assertThrows(GameException.class,
                () -> game.startSession(List.of("5"), List.of("domain.Clumsy", "domain.powers.Rayo"),
                        PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        InvocationTargetException invocation = (InvocationTargetException) exception.getCause();
        GameException cause = (GameException) invocation.getCause();
        assertEquals(GameException.NO_FOUND_SESSION, cause.getMessage());
        assertEquals(0, sessions(game).size());
    }

    @Test
    void startSessionRejectsAnIncompletePowerSelection() throws ReflectiveOperationException {
        GameException exception = assertThrows(GameException.class,
                () -> game.startSession(List.of("5"), List.of("domain.powers.AuraAzul"),
                        PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        assertEquals(0, sessions(game).size());
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
        game.startSession(List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo", "Clumsy"),
                PlayerVsBot.class.getName(), ByGoals.class.getName(), true);

        assertEquals(1, sessions(game).size());
        assertInstanceOf(PlayerVsBot.class, sessions(game).get(0));
    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsPlayerSession() throws GameException, ReflectiveOperationException {
        game.startSession(List.of("10"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByTime.class.getName(), false);

        assertEquals(1, sessions(game).size());
        assertInstanceOf(PlayerVsPlayer.class, sessions(game).get(0));
    }

    @Test
    void closeSessionRemovesTheSpecifiedSessionAndKeepsTheOthers() throws GameException, ReflectiveOperationException {
        GameSession first = game.startSession(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        GameSession second = game.startSession(
                List.of("10"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), false);

        game.closeSession(second);

        assertEquals(1, sessions(game).size());
        org.junit.jupiter.api.Assertions.assertSame(first, sessions(game).get(0));
    }

    @Test
    void startSessionRestoresEachUsersPointsAndSkillLevelsAcrossSessions() throws GameException {
        GameSession first = game.startSession(
                List.of("1"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        first.goal("right");
        Stats playerOne = first.getParticipantsRight().get(0).getStats();
        playerOne.ImproveStatistics("speed");

        GameSession second = game.startSession(
                List.of("1"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        assertSame(playerOne, second.getParticipantsRight().get(0).getStats());
        assertEquals(2, second.getParticipantsRight().get(0).getStats().getStatistic("speed"));
        assertEquals(0, second.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(0, second.getParticipantsLeft().get(0).getStats().getPoints());

        second.goal("right");
        GameSession third = game.startSession(
                List.of("1"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        assertEquals(2, third.getParticipantsRight().get(0).getStats().getStatistic("speed"));
        assertEquals(5, third.getParticipantsRight().get(0).getStats().getPoints());
    }

    @Test
    void movePlayerDelegatesToTheSpecifiedSessionAndParticipant() throws Exception {
        GameSession session = game.startSession(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        SessionParticipant participant = session.getParticipantsRight().get(0);
        participant.resetPosition(100, 20);

        game.movePlayer(session, participant, true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(20, participant.getHitbox().getY(), 1e-9);
    }

    @Test
    void improveStatisticsDelegatesToTheSelectedParticipant() throws Exception {
        GameSession session = game.startSession(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        Stats rightStats = session.getParticipantsRight().get(0).getStats();
        rightStats.awardWin();

        game.ImproveStatistics(session, true, "kick");

        assertEquals(2, rightStats.getStatistic("kick"));
        assertEquals(0, rightStats.getPoints());
        assertEquals(1, session.getParticipantsLeft().get(0).getStats().getStatistic("kick"));
    }

    @Test
    void sessionViewExposesImmutableRenderingStateWithoutSessionObjects() throws Exception {
        int sessionIndex = game.startSessionForView(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        Game.SessionView view = game.inspectSession(sessionIndex);

        assertEquals(450, view.court().width());
        assertEquals(150, view.court().height());
        assertEquals(List.of("left:0", "right:0"),
                view.participants().stream().map(Game.ParticipantView::id).toList());
        assertEquals(1, view.balls().size());
        assertThrows(UnsupportedOperationException.class, () -> view.balls().clear());
    }

    @Test
    void facadeMovementUsesSessionAndParticipantIdentifiers() throws Exception {
        int sessionIndex = game.startSessionForView(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        game.setParticipantPosition(sessionIndex, "right:0", 100, 20);

        game.movePlayer(sessionIndex, "right:0", true);

        Game.ParticipantView participant = game.inspectSession(sessionIndex).participants().get(1);
        assertEquals(102.5, participant.x(), 1e-9);
        assertEquals(20, participant.y(), 1e-9);
    }

    @Test
    void facadeKickReturnsWhetherTheBallWasReachedAndUpdatesItsSnapshot() throws Exception {
        int sessionIndex = game.startSessionForView(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        Game.ParticipantView right = game.inspectSession(sessionIndex).participants().get(1);
        game.setBallPosition(sessionIndex, 0, right.x() + right.width() / 2, right.y() + right.height() / 2);

        assertTrue(game.kickPlayer(sessionIndex, "right:0"));

        Game.BallView ball = game.inspectSession(sessionIndex).balls().get(0);
        assertEquals(-150, ball.velocityX());
        assertEquals(-50, ball.velocityY());
    }

    @Test
    void facadeBallSetupUpdatesPositionAndVelocityInTheSnapshot() throws Exception {
        int sessionIndex = game.startSessionForView(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        game.setBallPosition(sessionIndex, 0, 80, 50);

        game.applyBallImpulse(sessionIndex, 0, 6, -2);

        Game.BallView ball = game.inspectSession(sessionIndex).balls().get(0);
        assertEquals(80, ball.x());
        assertEquals(50, ball.y());
        assertEquals(6, ball.velocityX());
        assertEquals(-2, ball.velocityY());
    }

    @Test
    void facadeUpdateAdvancesTheIndexedSession() throws Exception {
        int sessionIndex = game.startSessionForView(
                List.of("5"), List.of("domain.powers.AuraAzul", "domain.powers.Rayo"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        game.updateGame(sessionIndex);

        assertEquals(1.0 / 60, game.inspectSession(sessionIndex).elapsedSeconds(), 1e-9);
    }

    @SuppressWarnings("unchecked")
    private static List<GameSession> sessions(Game game) throws ReflectiveOperationException {
        Field sessions = Game.class.getDeclaredField("sessions");
        sessions.setAccessible(true);
        return (List<GameSession>) sessions.get(game);
    }

}
