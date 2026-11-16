package domain.exception;

/**
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GameException extends IllegalArgumentException {
    public final static String NO_FOUND_TYPE_GAME = "el tipo de juego seleccionado no se encontro";
    public final static String NO_FOUND_SESSION = "no se encontro la session de juego";
    public final static String NO_CONSTRUCTOR_FOUND = "no se encontro el constructor de la clase";
    
    public final static String NO_INSTANTIATE_CLASS = "no se puede instancia la clase";
    public final static String NO_ACCESSIBLE_CONSTRUCTOR = "no se puede acceder al  método";
    public final static String EXCEPTION_IN_METHOD = "El constructor de la clase lanzó una excepción interna";
    public final static String MINUTES_NOT_NEGATIVE= "Los minutos no pueden ser negativos";
    public final static String GOALS_NOT_NEGATIVE= "Los goles no pueden ser negativos";
    public static final String OBJECT_OUT_OF_MATCH = "objeto fuera de la partida";
    public static final String OBJECTS_OVERLAPPED = "dos objetos super puestos";

    /**
     * Crea una excepción propia del dominio con el mensaje indicado.
     * @param message mensaje explicativo del error
     */
    public GameException(String message) {
        super(message);
    }
    

    /**
     * Crea una excepción propia del dominio con el mensaje y causa indicados.
     * @param message mensaje explicativo del error
     * @param cause excepción que originó el error
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }   
}
