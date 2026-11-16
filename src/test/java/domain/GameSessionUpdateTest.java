package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import domain.exception.GameException;
import domain.rules.ByGoals;
import domain.rules.ByTime;

class GameSessionUpdateTest {

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
        session.updateGame();
        assertEquals(elapsed, session.getElapsedSeconds());
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
        assertEquals(55, ball.getX(), 1e-9);
        assertEquals(7, ball.getVelocityX(), 1e-9);
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
        assertFalse(session.kickPlayer(left));
    }

    @Test
    void participantBallImpulseRunsOnceUntilTheySeparate() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.participantsLeft.get(0);
        Ball ball = ball(session);
        ball.reposition(participant.getHitbox().getX() + participant.getHitbox().getWidth() / 2, 120);

        session.updateGame();
        double afterFirstContact = ball.getVelocityX();
        session.updateGame();
        assertEquals(afterFirstContact, ball.getVelocityX(), 1e-9);

        ball.reposition(225, 75);
        session.updateGame();
        ball.reposition(participant.getHitbox().getX() + participant.getHitbox().getWidth() / 2, 120);
        session.updateGame();
        assertEquals(afterFirstContact + 4, ball.getVelocityX(), 1e-9);
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
        PlayerVsPlayer session = new PlayerVsPlayer(true, ByTime.class.getName(), List.of("0"));
        session.participantsLeft.get(0).resetPosition(50, 50);
        session.participantsRight.get(0).resetPosition(350, 50);
        session.updateGame();
        assertFalse(session.isFinished()); // zero-minute tie proceeds as sudden death
        ball(session).reposition(20, 125);
        session.updateGame();
        assertTrue(session.isFinished());
    }

    @Test
    void completedMinuteWithTieContinuesInSuddenDeath() throws Exception {
        PlayerVsPlayer session = new PlayerVsPlayer(true, ByTime.class.getName(), List.of("1"));
        for (int tick = 0; tick < 3600; tick++) session.updateGame();

        assertEquals(60.0, session.getElapsedSeconds(), 1e-8);
        assertFalse(session.isFinished());
    }

    private static PlayerVsPlayer session(int limit) throws GameException {
        return new PlayerVsPlayer(true, ByGoals.class.getName(), List.of(Integer.toString(limit)));
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
