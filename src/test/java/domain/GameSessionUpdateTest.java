package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import domain.GameException;
import domain.ByGoals;
import domain.ByTime;

class GameSessionUpdateTest {

    @BeforeEach
    void resetSingleton() throws ReflectiveOperationException {
        GameSessionTestSupport.resetSingleton();
    }

    @Test
    void scoresForSideOppositeTheGoalAndResetsRound() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(20, 125);

        session.updateGame();

        assertEquals(0, session.getGoalsLeft());
        assertEquals(1, session.getGoalsRight());
        assertEquals(225, ball.getX());
        assertEquals(75, ball.getY());
        assertEquals(326.25, session.participantsRight.get(0).getHitbox().getX());
        assertFalse(session.isFinished());
    }

    @Test
    void ballEnteringGoalFromTheFieldScoresInsteadOfBouncingAtItsOpening() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(46, 120);
        ball.applyImpulse(-150, 0);

        session.updateGame();

        assertEquals(1, session.getGoalsRight());
        assertEquals(225, ball.getX());
        assertEquals(75, ball.getY());
    }

    @Test
    void reachingGoalLimitFinishesAndStopsClock() throws Exception {
        PlayerVsPlayer session = session(1);
        ball(session).reposition(20, 125);
        session.updateGame();
        double elapsed = session.getElapsedSeconds();

        assertTrue(session.isFinished());
        assertEquals(5, session.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(0, session.getParticipantsLeft().get(0).getStats().getPoints());
        session.updateGame();
        assertEquals(elapsed, session.getElapsedSeconds());
    }

    @Test
    void finishedMatchAwardsWinnerOnlyOnce() {
        PlayerVsPlayer session = session(1);

        session.goal("right");
        session.checkTypeOfMatch();
        session.checkTypeOfMatch();

        assertEquals(5, session.getParticipantsRight().get(0).getStats().getPoints());
    }

    @Test
    void improveStatisticsSelectsRightForTrueAndLeftForFalse() {
        PlayerVsPlayer session = session(5);
        Stats rightStats = session.getParticipantsRight().get(0).getStats();
        Stats leftStats = session.getParticipantsLeft().get(0).getStats();
        rightStats.awardWin();
        leftStats.awardWin();

        session.improveStatistics(true, "speed");
        session.improveStatistics(false, "jump");

        assertEquals(2, rightStats.getStatistic("speed"));
        assertEquals(2, leftStats.getStatistic("jump"));
        assertEquals(0, rightStats.getPoints());
        assertEquals(0, leftStats.getPoints());
    }

    @Test
    void goalIsScoredBeforeBallHitboxOutsideCheck() throws Exception {
        PlayerVsPlayer session = session(5);
        ball(session).reposition(1, 125); // centro dentro del arco, hitbox parcialmente fuera

        session.updateGame();

        assertEquals(1, session.getGoalsRight());
    }

    @Test
    void fixedStepAppliesGravityAndAdvancesElapsedTime() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(225, 70);
        ball.applyImpulse(6, 0);

        session.updateGame();

        assertEquals(6.0 / 60, ball.getX() - 225, 1e-9);
        assertEquals(40.0 / 60, ball.getVelocityY(), 1e-9);
        assertEquals(1.0 / 60, session.getElapsedSeconds(), 1e-9);
    }

    @Test
    void movePlayerMovesOnlyParticipantsRegisteredInThisActiveSession() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.getParticipantsRight().get(0);
        participant.resetPosition(100, 20);
        Player outsider = new Player(99);
        outsider.resetPosition(200, 30);

        session.movePlayer(participant, true);
        session.movePlayer(outsider, true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(200, outsider.getHitbox().getX(), 1e-9);
    }

    @Test
    void movePlayerDoesNotMoveParticipantAfterSessionFinishes() throws Exception {
        PlayerVsPlayer session = session(1);
        SessionParticipant participant = session.getParticipantsRight().get(0);
        session.goal("left");
        participant.resetPosition(100, 20);

        session.movePlayer(participant, true);

        assertTrue(session.isFinished());
        assertEquals(100, participant.getHitbox().getX(), 1e-9);
    }

    @Test
    void ballReboundsWithRetentionAndStopsBelowMinimum() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(225, 138.5);
        ball.applyImpulse(0, 10);

        session.updateGame();

        assertEquals(138.5, ball.getY(), 1e-9);
        assertEquals(-((10 + 40.0 / 60) * 0.70), ball.getVelocityY(), 1e-9);

        ball.reposition(225, 138.5);
        setVelocityY(ball, 0.01);
        session.updateGame();
        assertEquals(0, ball.getVelocityY());
    }

    @Test
    void ballBouncesOffSideWallAndKeepsSeventyPercentHorizontalSpeed() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(3.1, 30);
        ball.applyImpulse(-20, 0);

        session.updateGame();

        assertEquals(11.5, ball.getX(), 1e-9);
        assertEquals(14, ball.getVelocityX(), 1e-9);
    }

    @Test
    void ballBouncesOffCeilingAndKeepsSeventyPercentVerticalSpeed() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(225, 3.1);
        ball.applyImpulse(0, -20);

        session.updateGame();

        assertEquals(11.5, ball.getY(), 1e-9);
        assertEquals(20 * 0.7 - 40.0 / 60 * 0.7, ball.getVelocityY(), 1e-9);
    }

    @Test
    void ballBouncesOffGoalFrameWhenItsCenterHasNotEnteredGoal() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(46.2, 97.5);
        ball.applyImpulse(-10, 0);

        session.updateGame();

        assertEquals(0, session.getGoalsRight());
        var goal = session.getField().getGoalHitboxes()[0];
        double outsideDistanceY = goal.getY() - ball.getY();
        double expectedX = goal.getX() + goal.getWidth()
                + Math.sqrt(ball.getRadius() * ball.getRadius() - outsideDistanceY * outsideDistanceY);
        assertEquals(expectedX, ball.getX(), 1e-9);
        assertEquals(7, ball.getVelocityX(), 1e-9);
        double cornerDistance = Math.hypot(ball.getX() - (goal.getX() + goal.getWidth()),
                ball.getY() - goal.getY());
        assertEquals(ball.getRadius(), cornerDistance, 1e-9);
    }

    @Test
    void registeredLeftParticipantKicksBallWithinReach() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant left = session.getParticipantsLeft().get(0);
        var box = left.getHitbox();
        Ball ball = ball(session);
        ball.reposition(box.getX() + box.getWidth() + ball.getRadius() + 8, box.getY() + box.getHeight() / 2);

        assertTrue(session.kickPlayer(left));
        assertEquals(150, ball.getVelocityX());
        assertEquals(-50, ball.getVelocityY());
    }

    @Test
    void kickHitsAnOpponentInRangeAndPrioritizesThemOverTheBall() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant attacker = session.participantsLeft.get(0);
        SessionParticipant target = session.participantsRight.get(0);
        attacker.resetPosition(100, 50);
        target.resetPosition(130, 50);
        Ball ball = ball(session);
        ball.reposition(140.5, 75);
        ball.applyImpulse(-20, 0);

        assertTrue(session.kickPlayer(attacker));

        assertEquals(90, target.getLife());
        assertEquals(100, attacker.getLife());
        assertEquals(-20, ball.getVelocityX());
    }

    @Test
    void kickDamageIncreasesByFiveForEachKickSkillLevel() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant attacker = session.participantsLeft.get(0);
        SessionParticipant target = session.participantsRight.get(0);
        attacker.resetPosition(100, 50);
        target.resetPosition(130, 50);
        attacker.getStats().awardWin();
        attacker.getStats().improveStatistics("kick");

        assertTrue(session.kickPlayer(attacker));

        assertEquals(85, target.getLife());
    }

    @Test
    void atZeroLifeTargetCannotMoveOrKickAndRecoversAfterStun() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant attacker = session.participantsLeft.get(0);
        SessionParticipant target = session.participantsRight.get(0);
        attacker.resetPosition(100, 50);
        target.resetPosition(130, 50);
        for (int hit = 0; hit < 10; hit++) assertTrue(session.kickPlayer(attacker));
        double targetX = target.getHitbox().getX();

        assertEquals(0, target.getLife());
        assertTrue(target.blocksMovement());
        assertFalse(session.kickPlayer(target));
        session.movePlayer(target, true);
        assertEquals(targetX, target.getHitbox().getX());

        ball(session).reposition(225, 75);
        for (int tick = 0; tick < 180; tick++) session.updateGame();

        assertEquals(100, target.getLife());
        assertFalse(target.blocksMovement());
    }

    @Test
    void opponentOutsideKickReachIsNotDamaged() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant attacker = session.participantsLeft.get(0);
        SessionParticipant target = session.participantsRight.get(0);
        attacker.resetPosition(100, 50);
        target.resetPosition(131, 50);
        ball(session).reposition(225, 75);

        assertFalse(session.kickPlayer(attacker));

        assertEquals(100, target.getLife());
    }

    @Test
    void kickIsRejectedOutsideReachForUnregisteredOrFinishedSession() throws Exception {
        PlayerVsPlayer session = session(1);
        SessionParticipant left = session.getParticipantsLeft().get(0);
        Ball ball = ball(session);
        ball.reposition(300, 75);
        assertFalse(session.kickPlayer(left));

        PlayerVsPlayer outsiderSession = session(5);
        assertFalse(outsiderSession.kickPlayer(new Player(1)));

        ball(session).reposition(20, 125);
        session.updateGame();
        assertTrue(session.isFinished());
        assertEquals(5, session.getParticipantsRight().get(0).getStats().getPoints());
        assertFalse(session.kickPlayer(left));
    }

    @Test
    void movingBallBouncesFromParticipantWithoutTeleportingOrAddingRepeatedImpulse() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(100, 50);
        Ball ball = ball(session);
        double contactX = participant.getHitbox().getX() + participant.getHitbox().getWidth() + ball.getRadius() - 0.1;
        ball.reposition(contactX, 75);
        ball.applyImpulse(-20, 0);

        session.updateGame();
        assertTrue(ball.getVelocityX() > 0);
        assertTrue(ball.getX() >= participant.getHitbox().getX()
                + participant.getHitbox().getWidth() + ball.getRadius());
        double afterBounce = ball.getVelocityX();
        session.updateGame();
        assertEquals(afterBounce, ball.getVelocityX(), 1e-9);
        assertTrue(ball.getX() > contactX);
    }

    @Test
    void walkingIntoStationaryBallPushesItWithoutChangingItsVelocity() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(100, 50);
        Ball ball = ball(session);
        ball.reposition(100 + participant.getHitbox().getWidth() + ball.getRadius() + 1, 75);
        double originalBallX = ball.getX();

        session.movePlayer(participant, true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(originalBallX + 1.5, ball.getX(), 1e-6);
        assertEquals(0, ball.getVelocityX(), 1e-9);
        assertEquals(0, ball.getVelocityY(), 1e-9);
        assertFalse(ball.intersects(participant).isContact());
    }

    @Test
    void walkingIntoMovingBallDisplacesItAndPreservesItsVelocity() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(100, 50);
        Ball ball = ball(session);
        ball.reposition(100 + participant.getHitbox().getWidth() + ball.getRadius() + 1, 75);
        ball.applyImpulse(12, -3);
        double originalBallX = ball.getX();

        session.movePlayer(participant, true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(originalBallX + 1.5, ball.getX(), 1e-6);
        assertEquals(12, ball.getVelocityX(), 1e-9);
        assertEquals(-3, ball.getVelocityY(), 1e-9);
        assertFalse(ball.intersects(participant).isContact());
    }

    @Test
    void walkingAwayFromBallUnderParticipantMovesPlayerWithoutMovingBall() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(100, 50);
        Ball ball = ball(session);
        ball.reposition(111.25, 110);
        double originalBallX = ball.getX();

        session.movePlayer(participant, true);

        assertEquals(102.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(originalBallX, ball.getX(), 1e-9);
    }

    @Test
    void walkingPushStopsAtCourtBoundaryAndKeepsPlayerFromPassingThrough() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(405, 50);
        Ball ball = ball(session);
        ball.reposition(438, 75);

        session.movePlayer(participant, true);

        assertEquals(405, participant.getHitbox().getX(), 1e-9);
        assertTrue(ball.getX() >= 438.4 && ball.getX() <= 438.5);
        assertFalse(ball.intersects(participant).isContact());
    }

    @Test
    void landingOnTopKeepsParticipantAboveAndSendsBallDownward() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(100, 70);
        Ball ball = ball(session);
        ball.reposition(111.25, 59.83);
        ball.applyImpulse(0, 10);

        session.updateGame();

        assertEquals(60, ball.getY(), 2e-6);
        assertEquals(0.5, ball.getVelocityY(), 1e-9);
        assertTrue(ball.getY() + ball.getRadius() <= participant.getHitbox().getY() + 1e-9);
    }

    @Test
    void equalMassBallsExchangeNormalVelocityWithSeventyPercentRestitution() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball first = ball(session);
        Ball second = new Ball(450, 150, 10);
        session.getField().addBall(second);
        first.reposition(200, 70);
        second.reposition(219, 70);
        first.applyImpulse(10, 0);
        second.applyImpulse(-10, 0);

        session.updateGame();

        assertEquals(-7, first.getVelocityX(), 1e-9);
        assertEquals(7, second.getVelocityX(), 1e-9);
        assertEquals(20, second.getX() - first.getX(), 1e-9);
    }

    @Test
    void overlappingSeparatingBallsAreSeparatedWithoutASecondImpulse() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball first = ball(session);
        Ball second = new Ball(450, 150, 10);
        session.getField().addBall(second);
        first.reposition(200, 70);
        second.reposition(219, 70);
        first.applyImpulse(-5, 0);
        second.applyImpulse(5, 0);

        session.updateGame();

        assertEquals(-5, first.getVelocityX(), 1e-9);
        assertEquals(5, second.getVelocityX(), 1e-9);
        assertTrue(second.getX() - first.getX() >= 20);
    }

    @Test
    void outsideObjectsAndOverlappingParticipantsRaiseDomainExceptions() throws Exception {
        PlayerVsPlayer outsideSession = session(5);
        outsideSession.participantsRight.get(0).resetPosition(-1, 50);
        GameException outside = assertThrows(GameException.class, outsideSession::updateGame);
        assertEquals(GameException.OBJECT_OUT_OF_MATCH, outside.getMessage());

        PlayerVsPlayer overlapSession = session(5);
        SessionParticipant left = overlapSession.participantsLeft.get(0);
        SessionParticipant right = overlapSession.participantsRight.get(0);
        right.resetPosition(left.getHitbox().getX(), left.getHitbox().getY());
        GameException overlap = assertThrows(GameException.class, overlapSession::updateGame);
        assertEquals(GameException.OBJECTS_OVERLAPPED, overlap.getMessage());
    }

    @Test
    void participantTouchingTopBoundaryRaisesOutsideException() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        participant.resetPosition(100, 0);

        GameException exception = assertThrows(GameException.class, session::updateGame);

        assertEquals(GameException.OBJECT_OUT_OF_MATCH, exception.getMessage());
    }

    @Test
    void ballOutsideCourtRaisesDomainException() throws Exception {
        PlayerVsPlayer session = session(5);
        Ball ball = ball(session);
        ball.reposition(-3, 30); // fuera lateral y lejos del área de gol
        ball.applyImpulse(-10, 0);

        session.updateGame();

        assertEquals(11.5, ball.getX(), 1e-9);
        assertTrue(ball.getVelocityX() > 0);
    }

    @Test
    void timeModeReceivesCompletedMinutesAndEndsAfterWinnerAppears() throws Exception {
        PlayerVsPlayer session = session(PlayerVsPlayer.class, ByTime.class.getName(), List.of("150", "450", "0"));
        session.participantsLeft.get(0).resetPosition(50, 50);
        session.participantsRight.get(0).resetPosition(350, 50);
        session.updateGame();
        assertFalse(session.isFinished()); // zero-minute tie proceeds as sudden death
        ball(session).reposition(20, 125);
        session.updateGame();
        assertTrue(session.isFinished());
        assertEquals(5, session.getParticipantsRight().get(0).getStats().getPoints());
    }

    @Test
    void completedMinuteWithTieContinuesInSuddenDeath() throws Exception {
        PlayerVsPlayer session = session(PlayerVsPlayer.class, ByTime.class.getName(), List.of("150", "450", "1"));
        for (int tick = 0; tick < 3600; tick++) session.updateGame();

        assertEquals(60.0, session.getElapsedSeconds(), 1e-8);
        assertFalse(session.isFinished());
    }

    private static PlayerVsPlayer session(int limit) throws GameException {
        return session(PlayerVsPlayer.class, ByGoals.class.getName(),
                List.of("150", "450", Integer.toString(limit)));
    }

    private static PlayerVsPlayer session(Class<PlayerVsPlayer> type, String matchType,
            List<String> parameters) throws GameException {
        return GameSessionTestSupport.create(type, true, matchType, parameters, powers());
    }

    private static List<String> powers() {
        return List.of("domain.BlueAura", "domain.Lightning");
    }

    private static Ball ball(GameSession session) throws Exception {
        Field field = GameSession.class.getDeclaredField("field");
        field.setAccessible(true);
        return ((SoccerField) field.get(session)).balls()[0];
    }

    private static void setVelocityY(Ball ball, double value) throws Exception {
        Field velocity = Ball.class.getDeclaredField("velocityY");
        velocity.setAccessible(true);
        velocity.setDouble(ball, value);
    }
}
