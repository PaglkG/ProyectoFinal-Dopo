package edu.eci.DOPOSoccer.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/** Pruebas de inicialización y reinicio de las teclas predeterminadas. */
class KeyBindingsTest {

    @Test
    void constructorInitializesEveryBindingForPlayerOne() throws ReflectiveOperationException {
        KeyBindings bindings = new KeyBindings(1);

        assertNotNull(getField(bindings, "right"));
        assertNotNull(getField(bindings, "left"));
        assertNotNull(getField(bindings, "jump"));
        assertNotNull(getField(bindings, "power"));
    }

    @Test
    void constructorInitializesEveryBindingForPlayerTwo() throws ReflectiveOperationException {
        KeyBindings bindings = new KeyBindings(2);

        assertNotNull(getField(bindings, "right"));
        assertNotNull(getField(bindings, "left"));
        assertNotNull(getField(bindings, "jump"));
        assertNotNull(getField(bindings, "power"));
    }

    @Test
    void rebootRestoresPlayerOneBindings() throws ReflectiveOperationException {
        KeyBindings bindings = new KeyBindings(1);
        dirtyBindings(bindings);

        bindings.reboot(1);

        assertBindingsEqual(new KeyBindings(1), bindings);
    }

    @Test
    void rebootRestoresPlayerTwoBindings() throws ReflectiveOperationException {
        KeyBindings bindings = new KeyBindings(2);
        dirtyBindings(bindings);

        bindings.reboot(2);

        assertBindingsEqual(new KeyBindings(2), bindings);
    }

    private static void dirtyBindings(KeyBindings bindings) throws ReflectiveOperationException {
        setField(bindings, "right", "custom-right");
        setField(bindings, "left", "custom-left");
        setField(bindings, "jump", "custom-jump");
        setField(bindings, "power", "custom-power");
    }

    private static void assertBindingsEqual(KeyBindings expected, KeyBindings actual)
            throws ReflectiveOperationException {
        assertEquals(getField(expected, "right"), getField(actual, "right"));
        assertEquals(getField(expected, "left"), getField(actual, "left"));
        assertEquals(getField(expected, "jump"), getField(actual, "jump"));
        assertEquals(getField(expected, "power"), getField(actual, "power"));
    }

    private static Object getField(Object target, String name) throws ReflectiveOperationException {
        Field field = KeyBindings.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = KeyBindings.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
