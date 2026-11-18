package domain.powers;

import java.lang.reflect.InvocationTargetException;

import domain.exception.GameException;

/** Crea poderes y perfiles de bot por nombre de clase con errores de dominio. */
public final class PowerFactory {

    /** Evita instanciar la fábrica, cuyas operaciones son estáticas. */
    private PowerFactory() {
    }

    /** Instancia un poder nombrado por su nombre de clase completo. */
    public static Power create(String className) throws GameException {
        return instantiate(className, Power.class);
    }

    private static <T> T instantiate(String className, Class<T> expectedType) throws GameException {
        if (className == null || className.isBlank()) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        try {
            Class<?> targetClass = Class.forName(className);
            if (!expectedType.isAssignableFrom(targetClass)) {
                throw new GameException(GameException.NO_FOUND_SESSION);
            }
            return expectedType.cast(targetClass.getDeclaredConstructor().newInstance());
        }
        catch (ClassNotFoundException e) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        catch (NoSuchMethodException | IllegalAccessException e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        }
        catch (InstantiationException | InvocationTargetException e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
    }
}
