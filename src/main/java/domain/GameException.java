package domain;

/** Excepción usada para informar errores definidos por las reglas y el dominio.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GameException extends IllegalArgumentException {
    /** Mensaje para un nombre de tipo de sesión inexistente. */
    public final static String NO_FOUND_TYPE_GAME = "el tipo de juego seleccionado no se encontro";
    /** Mensaje para una sesión singleton que no existe. */
    public final static String NO_FOUND_SESSION = "no se encontro la session de juego";
    /** Mensaje para un constructor requerido que no existe. */
    public final static String NO_CONSTRUCTOR_FOUND = "no se encontro el constructor de la clase";
    
    /** Mensaje para una clase que no se puede instanciar. */
    public final static String NO_INSTANTIATE_CLASS = "no se puede instancia la clase";
    /** Mensaje para un constructor o método inaccesible. */
    public final static String NO_ACCESSIBLE_CONSTRUCTOR = "no se puede acceder al  método";
    /** Mensaje para una excepción interna de constructor. */
    public final static String EXCEPTION_IN_METHOD = "El constructor de la clase lanzó una excepción interna";
    /** Mensaje para una duración negativa en una regla por tiempo. */
    public final static String MINUTES_NOT_NEGATIVE= "Los minutos no pueden ser negativos";
    /** Mensaje para un límite negativo en una regla por goles. */
    public final static String GOALS_NOT_NEGATIVE= "Los goles no pueden ser negativos";
    /** Mensaje para una entidad fuera de la partida. */
    public static final String OBJECT_OUT_OF_MATCH = "objeto fuera de la partida";
    /** Mensaje para dos entidades participantes superpuestas. */
    public static final String OBJECTS_OVERLAPPED = "dos objetos super puestos";
    /** Mensaje cuando faltan puntos para mejorar una estadística. */
    public static final String INSUFFICIENT_STAT_POINTS = "no hay puntos suficientes para mejorar una habilidad";
    /** Mensaje para el nombre de una estadística no reconocido. */
    public static final String UNKNOWN_STAT = "la habilidad seleccionada no existe";
    /** Mensaje para un índice de sesión inválido. */
    public static final String INVALID_SESSION_INDEX = "el índice de sesión no es válido";
    /** Mensaje para un índice de balón inválido. */
    public static final String INVALID_BALL_INDEX = "el índice de balón no es válido";
    /** Mensaje para un balón inválido. */
    public static final String INVALID_BALL = "el balón no es válido";
    /** Mensaje para un identificador de participante inválido. */
    public static final String INVALID_PARTICIPANT_ID = "el identificador del participante no es válido";
    /** Mensaje para estadísticas nulas. */
    public static final String INVALID_STATISTICS = "las estadísticas no pueden ser nulas";
    /** Mensaje para un efecto de estado inválido. */
    public static final String INVALID_STATUS_EFFECT = "el efecto de estado no es válido";
    /** Mensaje para una cantidad de daño no positiva. */
    public static final String INVALID_DAMAGE = "el daño debe ser positivo";
    /** Mensaje para parámetros inválidos de una regla de partido. */
    public static final String INVALID_RULE_PARAMETERS = "los parámetros de la regla no son válidos";
    /** Mensaje para parámetros inválidos de marcador. */
    public static final String INVALID_SCORE_PARAMETERS = "los parámetros del marcador no son válidos";

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
