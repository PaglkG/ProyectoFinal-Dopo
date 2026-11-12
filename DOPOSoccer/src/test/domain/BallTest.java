package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/** Pruebas del reinicio de posición implementado en {@link Ball}. */
class BallTest {

    @Test
    void resetBallSetsThePositionToTheCenterOfTheSuppliedDimensions() throws ReflectiveOperationException {
        Ball ball = new Ball(100.0, 60.0, 3.0);

        ball.resetBall(400.0, 200.0);

        assertEquals(200.0, readValue(ball, "x"));
        assertEquals(100.0, readValue(ball, "y"));
    }

    @Test
    void resetBallCentersTheBallAndClearsBothVelocityComponents() throws ReflectiveOperationException {
        Ball ball = new Ball(100.0, 60.0, 3.0);
        setValue(ball, "velocityX", 12.0);
        setValue(ball, "velocityY", -7.5);

        ball.resetBall(400.0, 200.0);

        assertEquals(200.0, readValue(ball, "x"));
        assertEquals(100.0, readValue(ball, "y"));
        assertEquals(0.0, readValue(ball, "velocityX"));
        assertEquals(0.0, readValue(ball, "velocityY"));
    }

    private static double readValue(Ball ball, String fieldName) throws ReflectiveOperationException {
        Field field = Ball.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(ball);
    }

    private static void setValue(Ball ball, String fieldName, double value) throws ReflectiveOperationException {
        Field field = Ball.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setDouble(ball, value);
    }
}
