package edu.eci.DOPOSoccer.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/** Pruebas del reinicio de la configuración y sus controles. */
class ConfigurationTest {

    @Test
    void rebootRestoresDefaultStateAndBindingsForPlayerOne() throws ReflectiveOperationException {
        Configuration configuration = new Configuration(1);
        setField(configuration, "sound", false);
        setField(configuration, "music", false);
        KeyBindings bindings = (KeyBindings) getField(configuration, "keyBindings");
        dirtyBindings(bindings);

        configuration.reboot(1);

        assertEquals(true, getField(configuration, "sound"));
        assertEquals(true, getField(configuration, "music"));
        assertBindingsEqual(new KeyBindings(1), bindings);
    }

    @Test
    void rebootRestoresDefaultStateAndBindingsForPlayerTwo() throws ReflectiveOperationException {
        Configuration configuration = new Configuration(2);
        setField(configuration, "sound", false);
        setField(configuration, "music", false);
        KeyBindings bindings = (KeyBindings) getField(configuration, "keyBindings");
        dirtyBindings(bindings);

        configuration.reboot(2);

        assertEquals(true, getField(configuration, "sound"));
        assertEquals(true, getField(configuration, "music"));
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
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
