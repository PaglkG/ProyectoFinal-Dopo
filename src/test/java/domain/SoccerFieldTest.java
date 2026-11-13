package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import domain.hitbox.CircularHitbox;
import domain.hitbox.RectangularHitbox;

/** Pruebas de las hitboxes y del reinicio de los balones de la cancha. */
class SoccerFieldTest {

    @Test
    void getHitboxFieldReturnsTheFourBoundaryHitboxes() throws ReflectiveOperationException {
        SoccerField field = new SoccerField();

        RectangularHitbox[] hitboxes = field.getHitboxField();

        assertEquals(4, hitboxes.length);
        assertEquals(450.0, readValue(hitboxes[0], "width"));
        assertEquals(1.5, readValue(hitboxes[0], "height"));
    }

    @Test
    void repositionMovesTheFieldBoundariesAsAGroup() throws ReflectiveOperationException {
        SoccerField field = new SoccerField();
        RectangularHitbox[] originalFieldHitboxes = field.getHitboxField();
        RectangularHitbox originalLeftGoal = readGoal(field, "LeftGoalHitbox");

        field.reposition(20.0, 30.0);

        RectangularHitbox[] hitboxes = field.getHitboxField();
        assertSame(originalFieldHitboxes, hitboxes);
        assertSame(originalLeftGoal, readGoal(field, "LeftGoalHitbox"));
        assertEquals(20.0, readValue(hitboxes[0], "x"));
        assertEquals(30.0, readValue(hitboxes[0], "y"));
        assertEquals(20.0, readValue(hitboxes[2], "x"));
        assertEquals(30.0, readValue(hitboxes[2], "y"));
    }

    @Test
    void constructorCreatesCenteredGoalHitboxesFromTheProvisionalFieldDimensions()
            throws ReflectiveOperationException {
        SoccerField field = new SoccerField();

        RectangularHitbox leftGoal = readGoal(field, "LeftGoalHitbox");
        RectangularHitbox rightGoal = readGoal(field, "RightGoalHitbox");

        assertEquals(0.0, readValue(leftGoal, "x"));
        assertEquals(50.0, readValue(leftGoal, "y"));
        assertEquals(45.0, readValue(leftGoal, "width"));
        assertEquals(50.0, readValue(leftGoal, "height"));
        assertEquals(405.0, readValue(rightGoal, "x"));
        assertEquals(50.0, readValue(rightGoal, "y"));
    }

    @Test
    void getHitboxBallsReturnsTheHitboxesOfContainedBalls() throws ReflectiveOperationException {
        SoccerField field = new SoccerField();

        CircularHitbox[] hitboxes = field.getHitboxBalls();

        assertEquals(1, hitboxes.length);
        assertSame(readBalls(field).get(0).getHitbox(), hitboxes[0]);
    }

    @Test
    void resetBallPlacesTheBallCenterAtTheRequestedCoordinatesAndClearsVelocity()
            throws ReflectiveOperationException {
        SoccerField field = new SoccerField();
        Ball ball = readBalls(field).get(0);
        setValue(ball, "velocityX", 6.0);
        setValue(ball, "velocityY", -4.0);

        field.resetBall(30.0, 40.0);

        assertEquals(30.0, readValue(ball.getHitbox(), "x"));
        assertEquals(40.0, readValue(ball.getHitbox(), "y"));
        assertEquals(0.0, readValue(ball, "velocityX"));
        assertEquals(0.0, readValue(ball, "velocityY"));
    }

    @SuppressWarnings("unchecked")
    private static List<Ball> readBalls(SoccerField field) throws ReflectiveOperationException {
        Field ballsField = SoccerField.class.getDeclaredField("balls");
        ballsField.setAccessible(true);
        return (List<Ball>) ballsField.get(field);
    }

    private static RectangularHitbox readGoal(SoccerField field, String fieldName)
            throws ReflectiveOperationException {
        Field hitboxField = SoccerField.class.getDeclaredField(fieldName);
        hitboxField.setAccessible(true);
        return (RectangularHitbox) hitboxField.get(field);
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
