package domain.powers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import domain.exception.GameException;

/** Pruebas de carga de los tipos concretos de poder seleccionables. */
class PowerTest {

    @Test
    void factoryCreatesAuraAzulByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(AuraAzul.class.getName());

        assertInstanceOf(AuraAzul.class, power);
    }

    @Test
    void factoryCreatesRayoByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(Rayo.class.getName());

        assertInstanceOf(Rayo.class, power);
    }

    @Test
    void factoryCreatesTornadoByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(Tornado.class.getName());

        assertInstanceOf(Tornado.class, power);
    }

    @Test
    void factoryCreatesIlusionByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(Ilusion.class.getName());

        assertInstanceOf(Ilusion.class, power);
    }

    @Test
    void factoryRejectsAClassThatIsNotAPower() {
        GameException exception = assertThrows(GameException.class,
                () -> PowerFactory.create(String.class.getName()));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    @Test
    void factoryReportsWhenTheNamedPowerClassDoesNotExist() {
        GameException exception = assertThrows(GameException.class,
                () -> PowerFactory.create("domain.powers.DoesNotExist"));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    @Test
    void factoryReportsAnAbstractPowerAsAnInstantiationFailure() {
        GameException exception = assertThrows(GameException.class,
                () -> PowerFactory.create(Power.class.getName()));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void factoryMapsAnInaccessibleConstructorLikeTheSessionFactory() {
        GameException exception = assertThrows(GameException.class,
                () -> PowerFactory.create(PowerWithPrivateConstructor.class.getName()));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    private static class PowerWithPrivateConstructor extends Power {
        private PowerWithPrivateConstructor() {
        }
    }
}
