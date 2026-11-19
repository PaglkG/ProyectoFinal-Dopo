package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import domain.GameException;

/** Pruebas de carga de los tipos concretos de poder seleccionables. */
class PowerTest {

    @Test
    void factoryCreatesBlueAuraByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(BlueAura.class.getName());

        assertInstanceOf(BlueAura.class, power);
    }

    @Test
    void factoryCreatesLightningByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(Lightning.class.getName());

        assertInstanceOf(Lightning.class, power);
    }

    @Test
    void factoryCreatesTornadoByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(Tornado.class.getName());

        assertInstanceOf(Tornado.class, power);
    }

    @Test
    void factoryCreatesIllusionByItsFullClassName() throws GameException {
        Power power = PowerFactory.create(Illusion.class.getName());

        assertInstanceOf(Illusion.class, power);
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
                () -> PowerFactory.create("domain.DoesNotExist"));

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
