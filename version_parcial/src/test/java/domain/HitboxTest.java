package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import domain.Ball;
import domain.SessionParticipant;
import domain.SoccerField;

/** Pruebas de contacto entre entidades y reposicionamiento de sus hitboxes. */
class HitboxTest {

    @Test
    void collisionEntitiesOwnShapesWithoutImplementingHitbox() {
        assertFalse(Hitbox.class.isAssignableFrom(Ball.class));
        assertFalse(Hitbox.class.isAssignableFrom(SessionParticipant.class));
        assertFalse(Hitbox.class.isAssignableFrom(SoccerField.class));
        assertInstanceOf(CircularHitbox.class, new Ball(0, 0, 2).getHitbox());
        assertInstanceOf(RectangularHitbox.class, new TestParticipant().getHitbox());
        assertInstanceOf(RectangularHitbox.class, new SoccerField().getFieldHitbox());
    }

    @Test
    void ballsReportTheOtherBallForOverlappingTouchingAndSeparatedCircles() {
        Ball first = new Ball(0, 0, 2);
        Ball overlapping = new Ball(4, 0, 2);
        Ball touching = new Ball(8, 0, 2);
        Ball separated = new Ball(8.02, 0, 2);

        assertContact(first.intersects(overlapping), "Ball", overlapping.getHitbox());
        assertContact(first.intersects(touching), "Ball", touching.getHitbox());
        assertNoContact(first.intersects(separated));
    }

    @Test
    void participantsReportTheOtherParticipantForOverlappingTouchingAndSeparatedRectangles() {
        TestParticipant first = new TestParticipant();
        TestParticipant overlapping = new TestParticipant();
        TestParticipant touching = new TestParticipant();
        TestParticipant separated = new TestParticipant();
        first.resetPosition(0, 0);
        overlapping.resetPosition(20, 7);
        touching.resetPosition(22.5, 0);
        separated.resetPosition(22.51, 0);

        assertContact(first.intersects(overlapping), "SessionParticipant", overlapping.getHitbox());
        assertContact(first.intersects(touching), "SessionParticipant", touching.getHitbox());
        assertNoContact(first.intersects(separated));
    }

    @Test
    void ballAndParticipantReportEachOtherForCircleRectangleContacts() {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(0, 0);
        Ball tangentBall = new Ball(0, 0, 5);
        tangentBall.reposition(-3, -4);
        Ball separatedBall = new Ball(0, 0, 4.99);
        separatedBall.reposition(-3, -4);
        Ball containedBall = new Ball(0, 0, 1);
        containedBall.reposition(5, 5);

        assertContact(tangentBall.intersects(participant), "SessionParticipant", participant.getHitbox());
        assertContact(participant.intersects(tangentBall), "Ball", tangentBall.getHitbox());
        assertContact(containedBall.intersects(participant), "SessionParticipant", participant.getHitbox());
        assertNoContact(separatedBall.intersects(participant));
    }

    @Test
    void noContactUsesAnEmptyNameAndNullHitbox() {
        CircularHitbox circle = new CircularHitbox(0, 0, 1);
        Hitbox unsupported = new Hitbox() {
            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }

            @Override
            public HitboxContact intersects(Object target) {
                return HitboxContact.none();
            }

            @Override
            public void reposition(double x, double y) {
            }
        };

        assertNoContact(circle.intersects(null));
        assertNoContact(circle.intersects(unsupported));
        assertNoContact(unsupported.intersects(circle));
    }

    @Test
    void fieldReportsBallAndFieldPartsForBoundaryGoalAndContainedBallContacts() {
        SoccerField field = new SoccerField();
        Ball boundaryBall = ballAt(100, 0, 2);
        Ball goalBall = ballAt(20, 125, 2);
        Ball containedBall = ballAt(225, 75, 2);
        Ball farBall = ballAt(500, 200, 2);

        assertContact(field.intersects(boundaryBall), "Ball", boundaryBall.getHitbox());
        assertContact(boundaryBall.intersects(field), "SoccerField", field.getFieldHitbox());
        assertContact(field.intersects(goalBall), "Ball", goalBall.getHitbox());
        assertContact(field.intersects(containedBall), "Ball", containedBall.getHitbox());
        assertNoContact(field.intersects(farBall));
    }

    @Test
    void fieldAndParticipantReportEachOtherAndTheContactingFieldPart() {
        SoccerField field = new SoccerField();
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(0, 70);

        assertContact(field.intersects(participant), "SessionParticipant", participant.getHitbox());
        assertContact(participant.intersects(field), "SoccerField", field.getFieldHitbox());
    }

    @Test
    void rectangularHitboxMovesItsUpperLeftCornerAndKeepsItsDimensions()
            throws ReflectiveOperationException {
        RectangularHitbox hitbox = new RectangularHitbox(1, 2, 10, 20);

        hitbox.reposition(30, 40);

        assertEquals(30.0, readValue(hitbox, "x"));
        assertEquals(40.0, readValue(hitbox, "y"));
        assertEquals(10.0, readValue(hitbox, "width"));
        assertEquals(20.0, readValue(hitbox, "height"));
    }

    @Test
    void circularHitboxMovesItsCenterAndKeepsItsRadius() throws ReflectiveOperationException {
        CircularHitbox hitbox = new CircularHitbox(1, 2, 5);

        hitbox.reposition(30, 40);

        assertEquals(30.0, readValue(hitbox, "x"));
        assertEquals(40.0, readValue(hitbox, "y"));
        assertEquals(5.0, readValue(hitbox, "radius"));
    }

    private static double readValue(Object hitbox, String fieldName) throws ReflectiveOperationException {
        Field field = hitbox.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(hitbox);
    }

    private static Ball ballAt(double x, double y, double radius) {
        Ball ball = new Ball(0, 0, radius);
        ball.reposition(x, y);
        return ball;
    }

    private static void assertContact(HitboxContact contact, String expectedName, Hitbox expectedHitbox) {
        assertEquals(expectedName, contact.name());
        assertSame(expectedHitbox, contact.hitbox());
    }

    private static void assertNoContact(HitboxContact contact) {
        assertEquals("", contact.name());
        assertNull(contact.hitbox());
        assertFalse(contact.isContact());
    }

    private static final class TestParticipant extends SessionParticipant {
    }
}
