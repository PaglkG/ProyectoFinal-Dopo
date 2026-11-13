package domain.hitbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/** Pruebas de reposicionamiento para las formas de hitbox. */
class HitboxTest {

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
}
