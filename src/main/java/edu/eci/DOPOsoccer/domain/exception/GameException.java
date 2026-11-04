package edu.eci.DOPOsoccer.domain.exception;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class GameException extends IllegalArgumentException {

    

    /**
     * Creates an exception with the specified message.
     */
    // Crea una excepción con el mensaje indicado.
    public GameException(String message) {
        super(message);
    }
    
}
