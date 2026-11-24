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
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import domain.GameException;
import domain.ByGoals;
import domain.ByTime;
class GameTest {

    private Game game;
    @TempDir
    private Path tempDir;

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
                () -> game.startSession(List.of("150", "450", "5"), List.of("domain.Clumsy", "domain.Lightning"),
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
                () -> game.startSession(List.of("150", "450", "5"), List.of("domain.BlueAura"),
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
            super(false, ByGoals.class.getName(), List.of("150", "450", "5"));
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
        game.startSession(List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning", "Clumsy"),
                PlayerVsBot.class.getName(), ByGoals.class.getName(), true);

        assertNotNull(managedSession(game));
        assertInstanceOf(PlayerVsBot.class, managedSession(game));
    }

    @Test
    void startSessionCreatesAndStoresAPlayerVsPlayerSession() throws GameException, ReflectiveOperationException {
        game.startSession(List.of("150", "450", "10"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByTime.class.getName(), false);

        assertNotNull(managedSession(game));
        assertInstanceOf(PlayerVsPlayer.class, managedSession(game));
    }

    @Test
    void allSessionSubclassesShareTheFirstCreatedInstance() throws GameException, ReflectiveOperationException {
        GameSession first = game.startSession(List.of("150", "450", "10"),
                List.of("domain.BlueAura", "domain.Lightning"), PlayerVsPlayer.class.getName(),
                ByGoals.class.getName(), false);

        GameSession second = game.startSession(List.of("150", "450", "5"),
                List.of("domain.BlueAura", "domain.Lightning", "Clumsy"), PlayerVsBot.class.getName(),
                ByGoals.class.getName(), true);

        assertSame(first, second);
        assertInstanceOf(PlayerVsPlayer.class, second);
        assertNotNull(managedSession(game));
        assertEquals(PlayerVsPlayer.class.getSimpleName(), game.getSessionType());
    }

    @Test
    void closeSessionRemovesTheSingleManagedSession() throws GameException, ReflectiveOperationException {
        GameSession first = game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        GameSession second = game.startSession(
                List.of("150", "450", "10"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), false);

        game.closeSession();

        assertSame(first, second);
        assertNull(managedSession(game));

        assertSame(first, game.startSession(List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));
        assertNotNull(managedSession(game));
    }

    @Test
    void startSessionReusesTheSameSessionAndItsStatistics() throws GameException {
        GameSession first = game.startSession(
                List.of("150", "450", "1"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        first.goal("right");
        Stats playerOne = first.getParticipantsRight().get(0).getStats();
        for (int win = 0; win < 4; win++) playerOne.awardWin();
        playerOne.improveStatistics("speed");
        game.closeSession();

        GameSession second = game.startSession(
                List.of("150", "450", "1"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        assertSame(playerOne, second.getParticipantsRight().get(0).getStats());
        assertSame(first, second);
        assertEquals(2, second.getParticipantsRight().get(0).getStats().getStatistic("speed"));
        assertEquals(0, second.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(Stats.POINTS_PER_LOSS, second.getParticipantsLeft().get(0).getStats().getPoints());

        assertSame(second, game.startSession(
                List.of("150", "450", "1"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true));
        assertEquals(0, playerOne.getPoints());
    }

    @Test
    void resetSessionRestartsOnlyTheRoundOfTheManagedSingleton() throws Exception {
        game.startSession(List.of("150", "450", "5"),
                List.of("domain.BlueAura", "domain.Lightning"), PlayerVsPlayer.class.getName(),
                ByGoals.class.getName(), true);
        GameSession session = managedSession(game);
        session.goal("right");
        game.setBallPosition(0, 80, 50);
        game.applyBallImpulse(0, 6, -2);
        game.setPlayerPosition(true, 100, 20);

        game.resetSession();

        BallView ball = game.getBallViews().get(0);
        assertEquals(1, game.getGoalsRight());
        assertEquals(225, ball.x());
        assertEquals(75, ball.y());
        assertEquals(0, ball.velocityX());
        assertEquals(0, ball.velocityY());
        assertEquals(326.25, game.getParticipantViews().get(1).x());
    }

    @Test
    void movePlayerUsesTheSingletonAndParticipantIdentifier() throws Exception {
        GameSession session = game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        SessionParticipant participant = session.getParticipantsRight().get(0);
        participant.resetPosition(100, 20);

        game.movePlayer(true, true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(20, participant.getHitbox().getY(), 1e-9);
    }

    @Test
    void improveStatisticsDelegatesToTheSelectedParticipant() throws Exception {
        GameSession session = game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        Stats rightStats = session.getParticipantsRight().get(0).getStats();
        for (int win = 0; win < 5; win++) rightStats.awardWin();

        game.improveStatistics(true, "kick");

        assertEquals(2, rightStats.getStatistic("kick"));
        assertEquals(0, rightStats.getPoints());
        assertEquals(1, session.getParticipantsLeft().get(0).getStats().getStatistic("kick"));
    }

    @Test
    void gameGettersExposeImmutableRenderingStateWithoutSessionObjects() throws Exception {
        game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        assertEquals(450, game.getCourtView().width());
        assertEquals(150, game.getCourtView().height());
        assertEquals(4, game.getBoundaryViews().size());
        assertEquals(2, game.getGoalViews().size());
        assertEquals(List.of("left", "right"),
                game.getParticipantViews().stream().map(ParticipantView::side).toList());
        assertEquals(1, game.getBallViews().size());
        assertThrows(UnsupportedOperationException.class, () -> game.getBallViews().clear());
        assertThrows(UnsupportedOperationException.class, () -> game.getBoundaryViews().clear());
    }

    @Test
    void facadeMovementUsesParticipantIdentifierForSingleton() throws Exception {
        game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        game.setPlayerPosition(true, 100, 20);

        game.movePlayer(true, true);

        ParticipantView participant = game.getParticipantViews().get(1);
        assertEquals(102.5, participant.x(), 1e-9);
        assertEquals(20, participant.y(), 1e-9);
    }

    @Test
    void facadeKickReturnsWhetherTheBallWasReachedAndUpdatesItsSnapshot() throws Exception {
        game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        ParticipantView right = game.getParticipantViews().get(1);
        game.setBallPosition(0, right.x() + right.width() / 2, right.y() + right.height() / 2);

        assertTrue(game.kickPlayer(true));

        BallView ball = game.getBallViews().get(0);
        assertEquals(-150, ball.velocityX());
        assertEquals(-50, ball.velocityY());
    }

    @Test
    void facadeBallSetupUpdatesPositionAndVelocityInTheSnapshot() throws Exception {
        game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        game.setBallPosition(0, 80, 50);

        game.applyBallImpulse(0, 6, -2);

        BallView ball = game.getBallViews().get(0);
        assertEquals(80, ball.x());
        assertEquals(50, ball.y());
        assertEquals(6, ball.velocityX());
        assertEquals(-2, ball.velocityY());
    }

    @Test
    void facadeUpdateAdvancesTheSingleton() throws Exception {
        game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);

        game.updateGame();

        assertEquals(1.0 / 60, game.getElapsedSeconds(), 1e-9);
    }

    @Test
    void saveWritesTheManagedSessionAndOpenRestoresItsObservableState() throws Exception {
        game.startSession(
                List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"),
                PlayerVsPlayer.class.getName(), ByGoals.class.getName(), true);
        managedSession(game).goal("right");
        game.setBallPosition(0, 180, 60);
        game.applyBallImpulse(0, 7, -3);

        Path saveFile = tempDir.resolve("partida.ser");
        game.save(saveFile.toFile());

        game.closeSession();
        Game restored = new Game();
        restored.open(saveFile.toFile());

        assertEquals(1, restored.getGoalsRight());
        assertEquals(180, restored.getBallX(0), 1e-9);
        assertEquals(60, restored.getBallY(0), 1e-9);
        assertEquals(7, restored.getBallVelocityX(0), 1e-9);
        assertEquals(-3, restored.getBallVelocityY(0), 1e-9);
    }

    @Test
    void openRejectsAFileThatDoesNotContainASession() throws Exception {
        Path invalidFile = tempDir.resolve("texto.ser");
        java.nio.file.Files.writeString(invalidFile, "contenido inválido");

        GameException exception = assertThrows(
                GameException.class, () -> game.open(invalidFile.toFile()));

        assertEquals(GameException.OPEN_FAILED, exception.getMessage());
        java.nio.file.Files.deleteIfExists(invalidFile);
    }

    private static GameSession managedSession(Game game) throws ReflectiveOperationException {
        Field session = Game.class.getDeclaredField("session");
        session.setAccessible(true);
        return (GameSession) session.get(game);
    }

}
