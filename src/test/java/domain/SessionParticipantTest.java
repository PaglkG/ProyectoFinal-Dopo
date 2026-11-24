package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import domain.RectangularHitbox;

/** Pruebas del comportamiento de posición común de los participantes. */
class SessionParticipantTest {

    @Test
    void resetPositionSetsBothCoordinates() {
        TestParticipant participant = new TestParticipant();

        participant.resetPosition(12.5, 48.25);

        assertEquals(12.5, participant.getHitbox().getX());
        assertEquals(48.25, participant.getHitbox().getY());
    }

    @Test
    void resetPositionReplacesThePreviousCoordinates() {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(10.0, 20.0);

        participant.resetPosition(30.5, 40.5);

        assertEquals(30.5, participant.getHitbox().getX());
        assertEquals(40.5, participant.getHitbox().getY());
    }

    @Test
    void resetPositionRepositionsTheParticipantHitbox() throws ReflectiveOperationException {
        TestParticipant participant = new TestParticipant();

        participant.resetPosition(30.5, 40.5);

        RectangularHitbox hitbox = participant.getHitbox();
        assertEquals(30.5, readCoordinate(hitbox, "x"));
        assertEquals(40.5, readCoordinate(hitbox, "y"));
        assertEquals(450.0 / 20, readCoordinate(hitbox, "width"));
        assertEquals(150.0 / 3, readCoordinate(hitbox, "height"));
    }

    @Test
    void moveRightScalesTheFixedStepByTheSpeedStatisticAndMovesTheHitbox() throws Exception {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(10, 20);
        setSpeed(participant, 3);

        participant.move(true);

        assertEquals(17.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(20, participant.getHitbox().getY(), 1e-9);
    }

    @Test
    void moveLeftUsesTheSameSpeedScaledStepInTheOppositeDirection() throws Exception {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(10, 20);
        setSpeed(participant, 3);

        participant.move(false);

        assertEquals(2.5, participant.getHitbox().getX(), 1e-9);
        assertEquals(20, participant.getHitbox().getY(), 1e-9);
    }

    @Test
    void participantStartsWithSpeedStatisticOne() {
        TestParticipant participant = new TestParticipant();

        assertNotNull(participant.getStats());
        assertEquals(1, participant.getStats().getSpeed());
    }

    @Test
    void participantStartsWithOneHundredLife() {
        TestParticipant participant = new TestParticipant();

        assertEquals(100, participant.getLife());
        assertEquals(100, participant.getMaxLife());
    }

    @Test
    void damageCannotLowerLifeBelowZeroAndAppliesTheStunEffect() {
        TestParticipant participant = new TestParticipant();

        participant.takeDamage(150);

        assertEquals(0, participant.getLife());
        assertTrue(participant.blocksMovement());
    }

    @Test
    void stunnedParticipantCannotMoveAndRecoversAfterThreeSeconds() {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(40, 30);
        participant.takeDamage(100);

        participant.move(true);
        assertEquals(40, participant.getHitbox().getX());
        assertTrue(participant.blocksMovement());

        for (int tick = 0; tick < 180; tick++) participant.updateStatusEffects();

        assertEquals(100, participant.getLife());
        assertFalse(participant.blocksMovement());
        participant.move(true);
        assertEquals(42.5, participant.getHitbox().getX(), 1e-9);
    }

    @SuppressWarnings("unchecked")
    private static void setSpeed(SessionParticipant participant, int speed) throws Exception {
        Field statsField = SessionParticipant.class.getDeclaredField("stats");
        statsField.setAccessible(true);
        Stats stats = (Stats) statsField.get(participant);
        Field actionField = Stats.class.getDeclaredField("action");
        actionField.setAccessible(true);
        ((java.util.Map<String, Integer>) actionField.get(stats)).put("speed", speed);
    }

    private static double readCoordinate(RectangularHitbox hitbox, String fieldName)
            throws ReflectiveOperationException {
        Field field = RectangularHitbox.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(hitbox);
    }

    private static final class TestParticipant extends SessionParticipant {
    }
}
