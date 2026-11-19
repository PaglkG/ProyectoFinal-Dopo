package domain;

import java.lang.reflect.InvocationTargetException;

import domain.GameException;

/** Crea poderes y perfiles de bot por nombre de clase con errores de dominio. */
public final class PowerFactory {

    /** Evita instanciar la fábrica, cuyas operaciones son estáticas. */
    private PowerFactory() {
    }

    /** Instancia un poder nombrado por su nombre de clase completo. */
    public static Power create(String className) throws GameException {
        return instantiate(className, Power.class);
    }



    /**
     * Instancia dinámicamente una clase en tiempo de ejecución utilizando reflexión
     * y verifica que sea compatible con el tipo base esperado.
     *
     * @param <T>          El tipo genérico del objeto que se espera retornar.
     * @param className    El nombre completamente calificado de la clase a instanciar
     *                     (incluyendo el paquete, por ejemplo: "domain.Lightning").
     * @param expectedType La referencia {@code Class} del tipo base o interfaz que la
     *                     clase instanciada debe extender o implementar.
     * @return Una nueva instancia de {@code className} casteada de forma segura al tipo {@code T}.
     * @throws GameException Si el nombre de la clase es nulo/vacío, la clase no existe,
     *                       no hereda de {@code expectedType}, carece de un constructor
     *                       público sin parámetros o falla durante su inicialización.
     */
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
