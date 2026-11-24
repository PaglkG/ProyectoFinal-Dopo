package domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas de selección y delegación de estrategias geométricas. */
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

    @Test
    void registeredStrategyCanHandleAnAdditionalShapePair() {
        CollisionDetector detector = new CollisionDetector();
        CollisionDetectionStrategy extraStrategy = new CollisionDetectionStrategy() {
            @Override
            public boolean supports(CollisionShape first, CollisionShape second) {
                return first instanceof TestShape && second instanceof TestShape;
            }

            @Override
            public boolean detect(CollisionShape first, CollisionShape second) {
                return true;
            }
        };
        detector.registerStrategy(extraStrategy);
        Collidable first = new TestCollidable(new TestShape(0, 0));
        Collidable second = new TestCollidable(new TestShape(0, 0));

        assertTrue(detector.detect(first, second).isContact());
    }

    private static final class TestParticipant extends SessionParticipant {
    }

    private record TestCollidable(CollisionShape collisionShape) implements Collidable {
        @Override
        public CollisionShape getCollisionShape() {
            return collisionShape;
        }
    }

    private record TestShape(double x, double y) implements CollisionShape {
        @Override
        public double getX() { return x; }

        @Override
        public double getY() { return y; }

        @Override
        public void reposition(double newX, double newY) { }
    }
}
