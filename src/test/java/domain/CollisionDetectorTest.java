package domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas de detección geométrica y de las entidades conservadas en el resultado. */
class CollisionDetectorTest {

    @Test
    void detectsCircleCircleAndReturnsTheEvaluatedEntities() {
        Ball first = new Ball(0, 0, 2);
        Ball second = new Ball(0, 0, 2);
        second.reposition(4, 0);

        CollisionContact contact = new CollisionDetector().detect(first, second);

        assertTrue(contact.isContact());
        assertSame(first, contact.getFirst());
        assertSame(second, contact.getSecond());
    }

    @Test
    void detectsRectangleRectangleAtTheBoundary() {
        TestParticipant first = new TestParticipant();
        TestParticipant second = new TestParticipant();
        first.resetPosition(0, 0);
        second.resetPosition(22.5, 0);

        CollisionContact contact = new CollisionDetector().detect(first, second);

        assertTrue(contact.isContact());
    }

    @Test
    void detectsCircleRectangleInEitherOrder() {
        Ball ball = new Ball(0, 0, 5);
        ball.reposition(-3, -4);
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(0, 0);
        CollisionDetector detector = new CollisionDetector();

        assertTrue(detector.detect(ball, participant).isContact());
        assertTrue(detector.detect(participant, ball).isContact());
    }

    @Test
    void unsupportedShapePairProducesNoContactAndKeepsBothEntities() {
        Collidable first = new TestCollidable(new TestShape(0, 0));
        Collidable second = new TestCollidable(new TestShape(0, 0));

        CollisionContact contact = new CollisionDetector().detect(first, second);

        assertFalse(contact.isContact());
        assertSame(first, contact.getFirst());
        assertSame(second, contact.getSecond());
    }

    private static final class TestParticipant extends SessionParticipant {
    }

    private static final class TestCollidable implements Collidable {
        private final CollisionShape collisionShape;

        private TestCollidable(CollisionShape collisionShape) {
            this.collisionShape = collisionShape;
        }

        @Override
        public CollisionShape getCollisionShape() {
            return collisionShape;
        }
    }

    private static final class TestShape implements CollisionShape {
        private double x;
        private double y;

        private TestShape(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public double getX() { return x; }

        @Override
        public double getY() { return y; }

        @Override
        public void reposition(double newX, double newY) { }
    }
}
