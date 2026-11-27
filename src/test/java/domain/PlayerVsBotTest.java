package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import domain.ByGoals;
import domain.BlueAura;
import domain.Lightning;
import domain.Clumsy;
import domain.Expert;
import domain.Defensive;
import domain.Offensive;
import domain.GameException;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de la composición de participantes de una sesión jugador contra bot. */
class PlayerVsBotTest {

    @BeforeEach
    void resetSingleton() throws ReflectiveOperationException {
        GameSessionTestSupport.resetSingleton();
    }

    @Test
    void openingALegacyMatchReflectsTheSidesAndPreservesGoalsByRole(@TempDir Path directory) throws Exception {
        Game oldGame = new Game();
        GameSession session = oldGame.startSession(List.of("150", "450", "5"),
                List.of("domain.BlueAura", "domain.Lightning", "Expert"),
                PlayerVsBot.class.getName(), ByGoals.class.getName(), true);
        SessionParticipant human = session.participantsLeft.get(0);
        SessionParticipant bot = session.participantsRight.get(0);
        session.participantsLeft.set(0, bot);
        session.participantsRight.set(0, human);
        session.goal("right");
        Ball ball = session.getField().balls()[0];
        ball.reposition(190, 70);
        ball.applyImpulse(70, -5);
        File file = directory.resolve("partida.dat").toFile();
        oldGame.save(file);

        Game restored = new Game();
        restored.open(file);

        assertTrue(restored.getPlayerX(true) < restored.getPlayerX(false));
        assertEquals(1, restored.getGoalsLeft());
        assertEquals(0, restored.getGoalsRight());
        assertEquals(260, restored.getBallX(0), 1e-9);
        assertEquals(-70, restored.getBallVelocityX(0), 1e-9);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void offensiveBotMovesOnTheRightRegardlessOfHumanIdentity(boolean humanIsPlayerOne) {
        Game game = new Game();
        game.startSession(List.of("150", "450", "5"),
                List.of("domain.BlueAura", "domain.Lightning", "Offensive"),
                PlayerVsBot.class.getName(), ByGoals.class.getName(), humanIsPlayerOne);
        double botX = game.getPlayerX(!humanIsPlayerOne);
        double humanX = game.getPlayerX(humanIsPlayerOne);
        assertTrue(humanX < botX);

        game.updateGame();

        assertTrue(game.getPlayerX(!humanIsPlayerOne) < botX);
        assertTrue(game.getPlayerX(!humanIsPlayerOne) > humanX);
        assertEquals(humanX, game.getPlayerX(humanIsPlayerOne));
    }

    @Test
    void offensiveBotKicksAnInRangeBallDuringTheUpdate() {
        PlayerVsBot session = createSession("Offensive");
        RectangularHitbox box = session.participantsRight.get(0).getHitbox();
        Ball ball = session.getField().balls()[0];
        ball.reposition(box.getX() - ball.getRadius() - 4,
                box.getY() + box.getHeight() / 2);

        session.updateGame();

        assertTrue(ball.getVelocityX() < 0);
    }

    @Test
    void offensiveBotDoesNotStackKicksWhileTheSameContactPersists() {
        PlayerVsBot session = createSession("Offensive");
        RectangularHitbox box = session.participantsRight.get(0).getHitbox();
        Ball ball = session.getField().balls()[0];
        ball.reposition(box.getX() - ball.getRadius() - 1,
                box.getY() + box.getHeight() / 2);
        session.updateGame();
        double firstKick = ball.getVelocityX();
        assertEquals(-150, firstKick, 1e-9);

        session.updateGame();

        assertEquals(firstKick, ball.getVelocityX(), 1e-9);
    }

    @Test
    void offensiveBotCanKickAgainAfterLosingAndRegainingReach() {
        PlayerVsBot session = createSession("Offensive");
        SessionParticipant bot = session.participantsRight.get(0);
        Ball ball = session.getField().balls()[0];
        RectangularHitbox box = bot.getHitbox();
        ball.resetBall(2 * (box.getX() - ball.getRadius() - 1),
                2 * (box.getY() + box.getHeight() / 2));
        session.updateGame();
        ball.resetBall(450, 80);
        session.updateGame();
        ball.resetBall(2 * (box.getX() - ball.getRadius() - 1),
                2 * (box.getY() + box.getHeight() / 2));

        session.updateGame();

        assertEquals(-150, ball.getVelocityX(), 1e-9);
    }

    @Test
    void defensiveBotMovesTowardTheSpaceBetweenBallAndOwnGoal() {
        PlayerVsBot session = createSession("Defensive");
        SessionParticipant bot = session.participantsRight.get(0);
        double botX = bot.getHitbox().getX();
        session.getField().balls()[0].reposition(290, 75);

        session.updateGame();

        assertTrue(bot.getHitbox().getX() > botX);
    }

    @Test
    void expertChangesFromAttackToDefenseAfterTakingTheLead() {
        PlayerVsBot session = createSession("Expert");
        SessionParticipant bot = session.participantsRight.get(0);
        Ball ball = session.getField().balls()[0];
        double initialX = bot.getHitbox().getX();
        ball.reposition(290, 75);

        session.updateGame();
        assertTrue(bot.getHitbox().getX() < initialX);

        session.goal("right");
        ball.reposition(290, 75);
        session.updateGame();

        assertTrue(bot.getHitbox().getX() > initialX);
    }

    @Test
    void updateExecutesTheBotMovementWithoutMovingTheHuman() {
        PlayerVsBot session = createSession("Offensive");
        SessionParticipant bot = installMovingBot(session);
        SessionParticipant human = session.participantsLeft.get(0);
        double botX = bot.getHitbox().getX();
        double humanX = human.getHitbox().getX();

        session.updateGame();

        assertTrue(bot.getHitbox().getX() > botX);
        assertEquals(humanX, human.getHitbox().getX());
    }

    @Test
    void botDoesNotTakeAStepThatWouldOverlapTheHuman() {
        PlayerVsBot session = createSession("Offensive");
        SessionParticipant bot = installMovingBot(session);
        SessionParticipant human = session.participantsLeft.get(0);
        double botX = bot.getHitbox().getX();
        human.resetPosition(botX + bot.getHitbox().getWidth() + 1, human.getHitbox().getY());

        assertDoesNotThrow(session::updateGame);
        assertEquals(botX, bot.getHitbox().getX());
    }

    @Test
    void offensiveBotJumpsOverTheHumanWhenItsGroundStepIsBlocked() {
        PlayerVsBot session = createSession("Offensive");
        RectangularHitbox bot = session.getParticipantsRight().get(0).getHitbox();
        SessionParticipant human = session.getParticipantsLeft().get(0);
        double groundY = bot.getY();
        human.resetPosition(bot.getX() - human.getHitbox().getWidth() - 1, groundY);
        session.getField().balls()[0].reposition(150, 72);

        double botStartX = bot.getX();
        session.updateGame();

        assertEquals(botStartX, bot.getX(), 1e-9);
        assertTrue(bot.getY() < groundY);
        boolean crossedWhileAbove = false;
        for (int step = 0; step < 100; step++) {
            session.updateGame();
            crossedWhileAbove |= bot.getX() + bot.getWidth() < human.getHitbox().getX()
                    && bot.getY() + bot.getHeight() <= human.getHitbox().getY();
        }
        assertTrue(crossedWhileAbove);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clumsy", "Offensive", "Defensive", "Expert"})
    void allBotProfilesJumpTowardAReachableHighBall(String profile) {
        PlayerVsBot session = createSession(profile);
        RectangularHitbox bot = session.getParticipantsRight().get(0).getHitbox();
        double groundY = bot.getY();
        session.getField().balls()[0].reposition(bot.getX() + bot.getWidth() / 2, groundY - 35);

        session.updateGame();

        assertTrue(bot.getY() < groundY);
    }

    @Test
    void botCanJumpTowardAHighBallWhileSupportedByTheHuman() {
        PlayerVsBot session = createSession("Offensive");
        SessionParticipant bot = session.getParticipantsRight().get(0);
        SessionParticipant human = session.getParticipantsLeft().get(0);
        RectangularHitbox box = bot.getHitbox();
        RectangularHitbox support = human.getHitbox();
        double headTop = support.getY() - box.getHeight();
        bot.resetPosition(support.getX(), headTop);
        session.getField().balls()[0].reposition(box.getX() + box.getWidth() / 2, headTop - 35);

        session.updateGame();

        assertTrue(box.getY() < headTop);
    }

    @Test
    void offensiveBotKeepsItsFeetOnTheGroundForABallAboveJumpReach() {
        PlayerVsBot session = createSession("Offensive");
        RectangularHitbox bot = session.getParticipantsRight().get(0).getHitbox();
        double groundY = bot.getY();
        session.getField().balls()[0].reposition(bot.getX() + bot.getWidth() / 2, groundY - 70);

        session.updateGame();

        assertEquals(groundY, bot.getY(), 1e-9);
    }

    @Test
    void botDoesNotTakeAStepThatWouldTouchTheLateralBoundary() {
        PlayerVsBot session = createSession("Offensive");
        SessionParticipant bot = installMovingBot(session);
        SoccerField field = session.getField();
        double botX = field.courtX() + field.courtWidth()
                - field.boundaryThickness() - bot.getHitbox().getWidth() - 1;
        double aboveGoal = field.getGoalHitboxes()[1].getY() - bot.getHitbox().getHeight() - 1;
        bot.resetPosition(botX, aboveGoal);

        assertDoesNotThrow(session::updateGame);
        assertEquals(botX, bot.getHitbox().getX());
    }

    private static BotPlayer installMovingBot(PlayerVsBot session) {
        BotPlayer bot = new BotPlayer() {
            @Override
            public BotDecisionStrategy createDecisionStrategy() {
                return context -> "moveRight";
            }
        };
        session.participantsRight.set(0, bot);
        session.positionParticipants();
        return bot;
    }

    @Test
    void constructorCreatesOneHumanAndOneBot() throws Exception {
        PlayerVsBot session = createSession(true, "Clumsy", "1");


        assertEquals(1, session.participantsRight.size());
        assertEquals(1, session.participantsLeft.size());
        assertInstanceOf(Player.class, session.participantsLeft.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsRight.get(0));
        assertInstanceOf(BlueAura.class, session.participantsLeft.get(0).getSpecialPower());
        assertInstanceOf(Lightning.class, session.participantsRight.get(0).getSpecialPower());
        assertInstanceOf(Clumsy.class, session.participantsRight.get(0));
    }

    @Test
    void constructorCreatesSameParticipantKindsForEitherPlayerOption() throws Exception {
        PlayerVsBot session = createSession(false, "Expert");

        assertInstanceOf(Player.class, session.participantsLeft.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsRight.get(0));
        assertInstanceOf(Expert.class, session.participantsRight.get(0));
    }

    @Test
    void constructorCreatesOffensiveBotFromTheSelectedDifficulty() throws Exception {
        PlayerVsBot session = createSession("Offensive");

        assertInstanceOf(Offensive.class, session.participantsRight.get(0));
    }

    @Test
    void humanReceivesWinPointsAgainstBotAndBotReceivesNone() throws Exception {
        PlayerVsBot session = createSession(true, "Clumsy", "1");

        session.goal("left");

        assertEquals(Stats.POINTS_PER_WIN, session.participantsLeft.get(0).getStats().getPoints());
        assertEquals(0, session.participantsRight.get(0).getStats().getPoints());
    }

    @Test
    void humanReceivesLossPointsWhenBotWinsAndBotReceivesNone() throws Exception {
        PlayerVsBot session = createSession(true, "Clumsy", "1");

        session.goal("right");

        assertEquals(Stats.POINTS_PER_LOSS, session.participantsLeft.get(0).getStats().getPoints());
        assertEquals(0, session.participantsRight.get(0).getStats().getPoints());
    }

    @Test
    void constructorCreatesDefensiveBotFromTheSelectedDifficulty() throws Exception {
        PlayerVsBot session = createSession("Defensive");

        assertInstanceOf(Defensive.class, session.participantsRight.get(0));
    }

    @Test
    void constructorRejectsAnUnknownBotDifficulty() {
        GameException exception = assertThrows(GameException.class, () -> createSession("Unknown"));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
    }

    private static PlayerVsBot createSession(String difficulty) throws GameException {
        return createSession(true, difficulty);
    }

    private static PlayerVsBot createSession(boolean player, String difficulty) throws GameException {
        return createSession(player, difficulty, "5");
    }

    private static PlayerVsBot createSession(boolean player, String difficulty, String limit) throws GameException {
        return GameSessionTestSupport.create(PlayerVsBot.class, player, ByGoals.class.getName(),
                List.of("150", "450", limit), List.of("domain.BlueAura", "domain.Lightning", difficulty));
    }
}
