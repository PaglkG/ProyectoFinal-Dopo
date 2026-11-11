package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/** Pruebas del comportamiento de posición común de los participantes. */
class SessionParticipantTest {

    @Test
    void resetPositionSetsBothCoordinates() throws ReflectiveOperationException {
        TestParticipant participant = new TestParticipant();

        participant.resetPosition(12.5, 48.25);

        assertEquals(12.5, readCoordinate(participant, "x"));
        assertEquals(48.25, readCoordinate(participant, "y"));
    }

    @Test
    void resetPositionReplacesThePreviousCoordinates() throws ReflectiveOperationException {
        TestParticipant participant = new TestParticipant();
        participant.resetPosition(10.0, 20.0);

        participant.resetPosition(30.5, 40.5);

        assertEquals(30.5, readCoordinate(participant, "x"));
        assertEquals(40.5, readCoordinate(participant, "y"));
    }

    private static double readCoordinate(SessionParticipant participant, String fieldName)
            throws ReflectiveOperationException {
        Field field = SessionParticipant.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(participant);
    }

    private static final class TestParticipant extends SessionParticipant {
    }
}
