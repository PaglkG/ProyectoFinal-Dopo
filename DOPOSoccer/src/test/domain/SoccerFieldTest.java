package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Pruebas de reinicio de los balones que contiene {@link SoccerField}. */
class SoccerFieldTest {

    @Test
    void resetBallRecentersEveryBallUsingTheSuppliedDimensions() throws ReflectiveOperationException {
        SoccerField field = new SoccerField();
        List<Ball> balls = readBalls(field);
        Ball additionalBall = new Ball(100.0, 100.0, 2.0);
        balls.add(additionalBall);
        setValue(balls.get(0), "x", 10.0);
        setValue(balls.get(0), "y", 20.0);
        setValue(additionalBall, "x", 30.0);
        setValue(additionalBall, "y", 40.0);

        field.resetBall(600.0, 240.0);

        for (Ball ball : balls) {
            assertEquals(300.0, readValue(ball, "x"));
            assertEquals(120.0, readValue(ball, "y"));
        }
    }

    @Test
    void resetBallClearsTheVelocityOfEveryBall() throws ReflectiveOperationException {
        SoccerField field = new SoccerField();
        List<Ball> balls = readBalls(field);
        balls.add(new Ball(100.0, 100.0, 2.0));
        for (Ball ball : balls) {
            setValue(ball, "velocityX", 6.0);
            setValue(ball, "velocityY", -4.0);
        }

        field.resetBall(600.0, 240.0);

        for (Ball ball : balls) {
            assertEquals(0.0, readValue(ball, "velocityX"));
            assertEquals(0.0, readValue(ball, "velocityY"));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Ball> readBalls(SoccerField field) throws ReflectiveOperationException {
        Field ballsField = SoccerField.class.getDeclaredField("balls");
        ballsField.setAccessible(true);
        return (List<Ball>) ballsField.get(field);
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
