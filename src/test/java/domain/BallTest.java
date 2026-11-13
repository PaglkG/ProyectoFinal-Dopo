package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import domain.hitbox.CircularHitbox;

/** Pruebas del balón y de su hitbox circular. */
class BallTest {

    @Test
    void constructorCreatesItsHitboxAtTheGeometricCenter() throws ReflectiveOperationException {
        Ball ball = new Ball(100.0, 60.0, 3.0);
        CircularHitbox hitbox = ball.getHitbox();

        assertEquals(50.0, readValue(hitbox, "x"));
        assertEquals(30.0, readValue(hitbox, "y"));
        assertEquals(3.0, readValue(hitbox, "radius"));
    }

    @Test
    void repositionMovesTheHitboxCenterAndKeepsItsRadius() throws ReflectiveOperationException {
        Ball ball = new Ball(100.0, 60.0, 3.0);

        ball.reposition(12.5, 48.25);

        assertEquals(12.5, readValue(ball.getHitbox(), "x"));
        assertEquals(48.25, readValue(ball.getHitbox(), "y"));
        assertEquals(3.0, readValue(ball.getHitbox(), "radius"));
    }

    @Test
    void resetBallCentersTheHitboxPreservesItsRadiusAndClearsBothVelocities()
            throws ReflectiveOperationException {
        Ball ball = new Ball(100.0, 60.0, 3.0);
        setValue(ball, "velocityX", 12.0);
        setValue(ball, "velocityY", -7.5);

        ball.resetBall(400.0, 200.0);

        assertEquals(200.0, readValue(ball.getHitbox(), "x"));
        assertEquals(100.0, readValue(ball.getHitbox(), "y"));
        assertEquals(3.0, readValue(ball.getHitbox(), "radius"));
        assertEquals(0.0, readValue(ball, "velocityX"));
        assertEquals(0.0, readValue(ball, "velocityY"));
    }

    private static double readValue(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(target);
    }

    private static void setValue(Ball ball, String fieldName, double value) throws ReflectiveOperationException {
        Field field = Ball.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setDouble(ball, value);
    }
}
