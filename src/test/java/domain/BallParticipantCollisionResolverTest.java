package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Comprueba la activación solicitada y el lado de los tiros de poder. */
class BallParticipantCollisionResolverTest {

    @Test
    void aFullPowerDoesNotFireUntilTheParticipantRequestsIt() {
        RecordingPower power = chargedPower();
        TestParticipant left = participant(30, power);
        Ball ball = new Ball(80, 80, 10);
        BallParticipantCollisionResolver resolver = new BallParticipantCollisionResolver();
        SoccerField field = new SoccerField();

        resolver.resolveAll(new Ball[] {ball}, field, List.of(left), List.of(), 0.7, 0.5);

        assertFalse(left.isPowerActive());
        assertFalse(power.trajectoryStarted);
        assertTrue(power.isUsable());
    }

    @Test
    void anArmedPowerUsesTheParticipantSideToChooseItsTrajectory() {
        RecordingPower leftPower = chargedPower();
        TestParticipant left = participant(30, leftPower);
        left.power();
        Ball leftBall = new Ball(80, 80, 10);
        BallParticipantCollisionResolver resolver = new BallParticipantCollisionResolver();
        SoccerField field = new SoccerField();

        resolver.resolveAll(new Ball[] {leftBall}, field, List.of(left), List.of(), 0.7, 0.5);

        assertTrue(leftPower.trajectoryAttackerOnLeft);
        assertEquals(400.0, leftBall.getVelocityX());
        assertFalse(left.isPowerActive());

        RecordingPower rightPower = chargedPower();
        TestParticipant right = participant(49.5, rightPower);
        right.power();
        Ball rightBall = new Ball(80, 80, 10);

        resolver.resolveAll(new Ball[] {rightBall}, field, List.of(), List.of(right), 0.7, 0.5);

        assertFalse(rightPower.trajectoryAttackerOnLeft);
        assertEquals(-400.0, rightBall.getVelocityX());
        assertFalse(right.isPowerActive());
    }

    @Test
    void aPowerShotAppliesItsImpactOnceToTheOpposingSide() {
        RecordingPower power = chargedPower();
        TestParticipant left = participant(30, power);
        TestParticipant right = participant(49.5, null);
        left.power();
        Ball ball = new Ball(80, 80, 10);
        BallParticipantCollisionResolver resolver = new BallParticipantCollisionResolver();
        SoccerField field = new SoccerField();

        resolver.resolveAll(new Ball[] {ball}, field, List.of(left), List.of(), 0.7, 0.5);
        ball.reposition(40, 40);
        resolver.resolveAll(new Ball[] {ball}, field, List.of(), List.of(right), 0.7, 0.5);
        resolver.resolveAll(new Ball[] {ball}, field, List.of(), List.of(right), 0.7, 0.5);

        assertSame(right, power.impactedParticipant);
        assertTrue(power.impactAttackerOnLeft);
        assertEquals(1, power.impactCount);
    }

    private static RecordingPower chargedPower() {
        RecordingPower power = new RecordingPower();
        for (int step = 0; step < 600; step++) power.update();
        return power;
    }

    private static TestParticipant participant(double x, Power power) {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(x, 15);
        participant.setSpecialPower(power);
        return participant;
    }

    private static final class TestParticipant extends SessionParticipant {
    }

    private static final class RecordingPower extends Power {
        private boolean trajectoryStarted;
        private boolean trajectoryAttackerOnLeft;
        private boolean impactAttackerOnLeft;
        private int impactCount;
        private SessionParticipant impactedParticipant;

        @Override
        public boolean startTrajectory(Ball ball, boolean attackerOnLeft) {
            trajectoryStarted = true;
            trajectoryAttackerOnLeft = attackerOnLeft;
            double targetSpeed = attackerOnLeft ? 400 : -400;
            ball.applyImpulse(targetSpeed - ball.getVelocityX(), 0);
            return true;
        }

        @Override
        public void applyImpact(SessionParticipant player, Ball ball, boolean attackerOnLeft) {
            impactAttackerOnLeft = attackerOnLeft;
            impactedParticipant = player;
            impactCount++;
        }
    }
}
