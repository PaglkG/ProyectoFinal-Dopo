package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        assertEquals(Stats.POINTS_PER_WIN, session.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(Stats.POINTS_PER_LOSS, session.getParticipantsLeft().get(0).getStats().getPoints());
        session.updateGame();
        assertEquals(elapsed, session.getElapsedSeconds());
    }

    @Test
    void finishedMatchAwardsWinnerOnlyOnce() {
        PlayerVsPlayer session = session(1);

        session.goal("right");
        session.checkTypeOfMatch();
        session.checkTypeOfMatch();

        assertEquals(Stats.POINTS_PER_WIN, session.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(Stats.POINTS_PER_LOSS, session.getParticipantsLeft().get(0).getStats().getPoints());
    }

    @Test
    void timeLimitWithTiedScoreWaitsForSuddenDeathWinnerAndAwardsPoints() throws Exception {
        PlayerVsPlayer session = session(PlayerVsPlayer.class, ByTime.class.getName(),
                List.of("150", "450", "0"));

        session.checkTypeOfMatch();
        assertFalse(session.isFinished());
        session.goal("right");

        assertTrue(session.isFinished());
        assertEquals(Stats.POINTS_PER_WIN,
                session.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(Stats.POINTS_PER_LOSS,
                session.getParticipantsLeft().get(0).getStats().getPoints());
    }

    @Test
    void improveStatisticsSelectsRightForTrueAndLeftForFalse() {
        PlayerVsPlayer session = session(5);
        Stats rightStats = session.getParticipantsRight().get(0).getStats();
        Stats leftStats = session.getParticipantsLeft().get(0).getStats();
        for (int win = 0; win < 5; win++) {
            rightStats.awardWin();
            leftStats.awardWin();
        }

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
    void opposingWalksDoNotStopTheMatchAndPlayersCanMoveApart() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant left = session.getParticipantsLeft().get(0);
        SessionParticipant right = session.getParticipantsRight().get(0);
        double initialLeft = left.getHitbox().getX();
        double initialRight = right.getHitbox().getX();
        for (int step = 0; step < 50; step++) {
            session.movePlayer(left, true);
            session.movePlayer(right, false);
            session.updateGame();
        }

        double nearLeft = left.getHitbox().getX();
        double nearRight = right.getHitbox().getX();
        assertTrue(nearLeft > initialLeft);
        assertTrue(nearRight < initialRight);
        assertFalse(left.intersects(right).isContact());

        session.movePlayer(left, false);
        session.movePlayer(right, true);
        session.updateGame();

        assertTrue(left.getHitbox().getX() < nearLeft);
        assertTrue(right.getHitbox().getX() > nearRight);
    }

    @Test
    void participantsAlreadyOverlappingCanSeparateWhileTheMatchContinues() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant left = session.getParticipantsLeft().get(0);
        SessionParticipant right = session.getParticipantsRight().get(0);
        left.resetPosition(200, 100);
        right.resetPosition(200, 100);

        assertDoesNotThrow(session::updateGame);
        for (int step = 0; step < 6; step++) {
            session.movePlayer(left, false);
            session.movePlayer(right, true);
            session.updateGame();
        }

        assertFalse(left.intersects(right).isContact());
        assertTrue(session.getElapsedSeconds() > 0.1);
    }

    @Test
    void bothGoalsStopWalksWithoutStoppingTheMatch() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant left = session.getParticipantsLeft().get(0);
        SessionParticipant right = session.getParticipantsRight().get(0);
        RectangularHitbox leftGoal = session.getField().getGoalHitboxes()[0];
        RectangularHitbox rightGoal = session.getField().getGoalHitboxes()[1];
        double leftX = leftGoal.getX() + leftGoal.getWidth() + 1;
        double rightX = rightGoal.getX() - right.getHitbox().getWidth() - 1;
        left.resetPosition(leftX, left.getHitbox().getY());
        right.resetPosition(rightX, right.getHitbox().getY());

        session.movePlayer(left, false);
        session.movePlayer(right, true);
        session.updateGame();

        assertEquals(leftX, left.getHitbox().getX());
        assertEquals(rightX, right.getHitbox().getX());
        assertTrue(session.getElapsedSeconds() > 0);
    }

    @Test
    void playerCanWalkAlongTheTopEdgeOfAnotherPlayer() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant upper = session.getParticipantsLeft().get(0);
        RectangularHitbox lower = session.getParticipantsRight().get(0).getHitbox();
        double height = upper.getHitbox().getHeight();
        double initialX = lower.getX() + 2;
        upper.resetPosition(initialX, lower.getY() - height);

        session.movePlayer(upper, false);

        assertTrue(upper.getHitbox().getX() < initialX);
        assertEquals(lower.getY(), upper.getHitbox().getY() + height, 1e-9);
    }

    @Test
    void jumpCanLandWhileTheSideEdgesOfTwoPlayersAreAligned() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant jumper = session.getParticipantsLeft().get(0);
        RectangularHitbox neighbor = session.getParticipantsRight().get(0).getHitbox();
        RectangularHitbox box = jumper.getHitbox();
        double groundY = box.getY();
        jumper.resetPosition(neighbor.getX() - box.getWidth(), groundY);
        session.jumpPlayer(jumper);

        for (int step = 0; step < 100; step++) session.updateGame();

        assertEquals(groundY, box.getY(), 1e-9);
        assertTrue(session.getElapsedSeconds() > 1);
    }

    @Test
    void lowerPlayerWalksOutFromUnderTheUpperPlayerWhoFallsInPlace() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant upper = session.getParticipantsLeft().get(0);
        SessionParticipant lower = session.getParticipantsRight().get(0);
        landOnOtherPlayer(session, upper, lower);
        double upperX = upper.getHitbox().getX();
        double upperY = upper.getHitbox().getY();
        double lowerX = lower.getHitbox().getX();

        session.movePlayer(lower, true);
        session.updateGame();

        assertEquals(lowerX + lower.horizontalStep(), lower.getHitbox().getX(), 1e-9);
        assertEquals(upperX, upper.getHitbox().getX(), 1e-9);
        assertEquals(upperY, upper.getHitbox().getY(), 1e-9);
        for (int step = 0; step < 8; step++) {
            session.movePlayer(lower, true);
            session.updateGame();
        }
        assertEquals(upperX + upper.getHitbox().getWidth(), lower.getHitbox().getX(), 1e-9);
        assertEquals(upperX, upper.getHitbox().getX(), 1e-9);
        assertTrue(upper.getHitbox().getY() > upperY);
    }

    @Test
    void upperPlayerCanJumpAgainFromTheOtherPlayersHead() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant upper = session.getParticipantsLeft().get(0);
        SessionParticipant lower = session.getParticipantsRight().get(0);
        landOnOtherPlayer(session, upper, lower);
        double supportedY = upper.getHitbox().getY();

        session.jumpPlayer(upper);
        session.updateGame();

        assertEquals(supportedY - 118.0 / 60, upper.getHitbox().getY(), 1e-9);
    }

    private void landOnOtherPlayer(PlayerVsPlayer session, SessionParticipant upper, SessionParticipant lower)
            throws Exception {
        RectangularHitbox upperBox = upper.getHitbox();
        RectangularHitbox lowerBox = lower.getHitbox();
        ball(session).reposition(80, 75);
        lower.resetPosition(200, lowerBox.getY());
        upper.resetPosition(lowerBox.getX() - upperBox.getWidth() - 2.5, upperBox.getY());
        session.jumpPlayer(upper);
        for (int step = 0; step < 90; step++) {
            if (upperBox.getX() < lowerBox.getX()) session.movePlayer(upper, true);
            session.updateGame();
        }
        assertEquals(lowerBox.getX(), upperBox.getX(), 1e-9);
        assertEquals(lowerBox.getY() - upperBox.getHeight(), upperBox.getY(), 1e-9);
    }

    @Test
    void humanCanCrossAboveAnOpponentAndLandOnTheOtherSide() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant jumper = session.getParticipantsLeft().get(0);
        SessionParticipant opponent = session.getParticipantsRight().get(0);
        RectangularHitbox box = jumper.getHitbox();
        double groundY = box.getY();
        double opponentX = opponent.getHitbox().getX();
        double opponentRight = opponentX + opponent.getHitbox().getWidth();
        jumper.resetPosition(opponentX - box.getWidth() - 1, groundY);
        session.jumpPlayer(jumper);

        boolean crossedWhileAbove = false;
        for (int step = 0; step < 100; step++) {
            if (box.getX() <= opponentRight) session.movePlayer(jumper, true);
            session.updateGame();
            crossedWhileAbove |= box.getX() > opponentRight
                    && box.getY() + box.getHeight() <= opponent.getHitbox().getY();
        }

        assertTrue(crossedWhileAbove);
        assertEquals(groundY, box.getY(), 1e-9);
    }

    @Test
    void registeredPlayerHitboxesAreAlignedToTheGroundAndTheHeadHeight() throws Exception {
        PlayerVsPlayer session = session(5);
        RectangularHitbox box = session.getParticipantsLeft().get(0).getHitbox();
        SoccerField field = session.getField();

        assertEquals(field.courtHeight() * 0.2212, box.getHeight(), 1e-9);
        assertEquals(field.courtWidth() / 20, box.getWidth(), 1e-9);
        assertEquals(field.groundY(), box.getY() + box.getHeight(), 1e-9);
    }

    @Test
    void stationaryBallRollsToTheRightFromTheCenterOfTheHeadAndFalls() throws Exception {
        PlayerVsPlayer session = session(5);
        RectangularHitbox head = session.getParticipantsLeft().get(0).getHitbox();
        Ball ball = ball(session);
        double centerX = head.getX() + head.getWidth() / 2;
        double supportedY = head.getY() - ball.getRadius();
        ball.reposition(centerX, supportedY);

        session.updateGame();

        assertEquals(centerX + 4.0 / 60, ball.getX(), 1e-9);
        assertEquals(supportedY, ball.getY(), 1e-9);
        assertEquals(0, ball.getVelocityY(), 1e-9);
        for (int step = 0; step < 220; step++) session.updateGame();
        assertTrue(ball.getX() > head.getX() + head.getWidth());
        assertTrue(ball.getY() > supportedY);
    }

    @Test
    void lowBounceSettlesOnTheHeadAndStartsRolling() throws Exception {
        PlayerVsPlayer session = session(5);
        RectangularHitbox head = session.getParticipantsLeft().get(0).getHitbox();
        Ball ball = ball(session);
        double x = head.getX() + head.getWidth() / 2;
        ball.reposition(x, head.getY() - ball.getRadius() - 0.005);

        session.updateGame();
        session.updateGame();

        assertEquals(head.getY(), ball.getY() + ball.getRadius(), 1e-9);
        assertEquals(0, ball.getVelocityY(), 1e-9);
        assertEquals(x + 4.0 / 60, ball.getX(), 1e-9);
    }

    @Test
    void jumpRaisesTheRealHitboxAndCarriesTheBallWhileItRollsLeft() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.getParticipantsLeft().get(0);
        RectangularHitbox head = participant.getHitbox();
        Ball ball = ball(session);
        double groundTop = head.getY();
        double ballX = head.getX() + 5;
        ball.reposition(ballX, groundTop - ball.getRadius());
        session.jumpPlayer(participant);

        session.updateGame();

        assertEquals(groundTop - 118.0 / 60, head.getY(), 1e-9);
        assertEquals(head.getY(), ball.getY() + ball.getRadius(), 1e-9);
        assertEquals(ballX - 4.0 / 60, ball.getX(), 1e-9);
    }

    @Test
    void jumpingAgainInTheAirDoesNotRestartTheVerticalImpulse() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant participant = session.getParticipantsLeft().get(0);
        double groundTop = participant.getHitbox().getY();
        session.jumpPlayer(participant);
        session.updateGame();

        session.jumpPlayer(participant);
        session.updateGame();

        assertEquals(groundTop - (118.0 + 115.0) / 60, participant.getHitbox().getY(), 1e-9);
        for (int step = 0; step < 90; step++) session.updateGame();
        assertEquals(groundTop, participant.getHitbox().getY(), 1e-9);
    }

    @Test
    void jumpStopsBelowTheCeilingWithoutStoppingTheMatch() throws Exception {
        PlayerVsPlayer session = GameSessionTestSupport.create(PlayerVsPlayer.class, true,
                ByGoals.class.getName(), List.of("46", "450", "5"),
                List.of("domain.BlueAura", "domain.Lightning"));
        SessionParticipant participant = session.getParticipantsLeft().get(0);
        double ceiling = session.getField().courtY() + session.getField().boundaryThickness();
        double groundTop = participant.getHitbox().getY();
        session.jumpPlayer(participant);

        double highestY = groundTop;
        for (int step = 0; step < 100; step++) {
            session.updateGame();
            highestY = Math.min(highestY, participant.getHitbox().getY());
        }

        assertTrue(highestY > ceiling && highestY - ceiling < 1e-9);
        assertEquals(groundTop, participant.getHitbox().getY(), 1e-9);
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
    void kickDamageIncreasesByTenPercentForEachKickSkillLevel() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant attacker = session.participantsLeft.get(0);
        SessionParticipant target = session.participantsRight.get(0);
        attacker.resetPosition(100, 50);
        target.resetPosition(130, 50);
        for (int win = 0; win < 5; win++) attacker.getStats().awardWin();
        attacker.getStats().improveStatistics("kick");

        assertTrue(session.kickPlayer(attacker));

        assertEquals(89, target.getLife());
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
        assertEquals(Stats.POINTS_PER_WIN, session.getParticipantsRight().get(0).getStats().getPoints());
        assertFalse(session.kickPlayer(left));
    }

    @Test
    void fastBallBouncesOnTheEntrySideAndThePlayerCanKeepMoving() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant player = session.getParticipantsRight().get(0);
        player.resetPosition(300, 90);
        Ball ball = ball(session);
        ball.reposition(270, 115);
        ball.applyImpulse(4500, 0);

        session.updateGame();

        assertEquals(290, ball.getX(), 1e-9);
        assertEquals(-3150, ball.getVelocityX(), 1e-9);
        session.movePlayer(player, true);
        session.updateGame();
        assertTrue(player.getHitbox().getX() > 300);
    }

    @Test
    void fastBallCannotScoreThroughAParticipantInItsPath() throws Exception {
        PlayerVsPlayer session = session(5);
        session.getParticipantsRight().get(0).resetPosition(380, 90);
        Ball ball = ball(session);
        ball.reposition(340, 115);
        ball.applyImpulse(4500, 0);

        session.updateGame();

        assertEquals(0, session.getGoalsLeft());
        assertEquals(370, ball.getX(), 1e-9);
        assertTrue(ball.getVelocityX() < 0);
    }

    @Test
    void fastBallCanPassThroughTheEmptySpaceOutsideARoundedCorner() throws Exception {
        PlayerVsPlayer session = session(5);
        SessionParticipant player = session.getParticipantsRight().get(0);
        player.resetPosition(300, 90);
        Ball ball = ball(session);
        ball.reposition(280, 83);
        ball.applyImpulse(780, -120 - 40.0 / 60);

        session.updateGame();

        assertEquals(293, ball.getX(), 1e-9);
        assertEquals(780, ball.getVelocityX(), 1e-9);
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
        double aboveGoal = session.getField().getGoalHitboxes()[1].getY()
                - participant.getHitbox().getHeight() - 1;
        participant.resetPosition(405, aboveGoal);
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
        session.jumpPlayer(participant);
        for (int step = 0; step < 48; step++) session.updateGame();
        RectangularHitbox box = participant.getHitbox();
        Ball ball = ball(session);
        ball.reposition(box.getX() + box.getWidth() / 2,
                box.getY() + box.getHeight() + ball.getRadius() + 0.1);
        ball.applyImpulse(0, -ball.getVelocityY() - 40.0 / 60);

        session.updateGame();

        assertEquals(0.5, ball.getVelocityY(), 1e-9);
        assertEquals(box.getY() + box.getHeight() + ball.getRadius(), ball.getY(), 1e-9);
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
    void outsideObjectsRaiseDomainException() throws Exception {
        PlayerVsPlayer outsideSession = session(5);
        outsideSession.participantsRight.get(0).resetPosition(-1, 50);
        GameException outside = assertThrows(GameException.class, outsideSession::updateGame);
        assertEquals(GameException.OBJECT_OUT_OF_MATCH, outside.getMessage());
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
        assertEquals(Stats.POINTS_PER_WIN, session.getParticipantsRight().get(0).getStats().getPoints());
        assertEquals(Stats.POINTS_PER_LOSS, session.getParticipantsLeft().get(0).getStats().getPoints());
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
