package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.Ball;
import domain.Player;
import domain.SoccerField;
import org.junit.jupiter.api.Test;

class CollisionFacadeTest {

    @Test
    void delegatesBallPairResolutionToTheBallResolver() {
        SoccerField field = new SoccerField();
        Ball first = field.balls()[0];
        Ball second = new Ball(450, 150, 10);
        field.addBall(second);
        first.reposition(100, 75);
        second.reposition(118, 75);
        first.applyImpulse(10, 0);
        second.applyImpulse(-10, 0);
        CollisionFacade collisions = new CollisionFacade(field);

        collisions.resolveBallCollisions(0.70);

        assertEquals(20, Math.hypot(second.getX() - first.getX(), second.getY() - first.getY()), 1e-9);
        assertEquals(-7, first.getVelocityX(), 1e-9);
        assertEquals(7, second.getVelocityX(), 1e-9);
    }

    @Test
    void delegatesParticipantContactResolutionToItsResolver() {
        SoccerField field = new SoccerField();
        Ball ball = field.balls()[0];
        Player participant = new Player(1);
        participant.resetPosition(100, 100);
        ball.reposition(110, 125);
        ball.applyImpulse(5, 0);
        CollisionFacade collisions = new CollisionFacade(field);

        collisions.resolveBallParticipantCollisions(java.util.List.of(participant), 0.70, 0.5);

        assertFalse(ball.intersects(participant).isContact());
        assertTrue(ball.getVelocityX() < 0);
    }

    @Test
    void delegatesGoalAndBoundaryQueriesToCourtGeometry() {
        SoccerField field = new SoccerField();
        Ball ball = field.balls()[0];
        ball.reposition(20, 120);
        CollisionFacade collisions = new CollisionFacade(field);

        assertEquals("left", collisions.goalSideFor(ball));
        assertFalse(collisions.isOutside(ball));
        ball.reposition(-1, 75);
        assertTrue(collisions.isOutside(ball));
    }
}
