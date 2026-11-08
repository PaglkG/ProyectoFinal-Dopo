package edu.eci.DOPOSoccer.domain.exception;

/**
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GameException extends IllegalArgumentException {
    public static final String NO_FOUND_TYPE_USER  = "this type user not exist in game"; 
    public static final String CLASS_NOT_FOUND  = "this type user not exist in game"; 
    public static final String CONSTRUCTOR_NOT_FOUND  = "this constructor whit params in class not exist"; 
    public static final String DUPLICATE_USER = "this user exist"; 

    /** * Crea una excepción con el mensaje indicado. */
    public GameException(String message) {
        super(message);
    }
    

    /** * Crea una excepción con el mensaje y el caso indicados. */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }   
}
