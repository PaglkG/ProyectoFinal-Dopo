package domain;

import java.io.Serializable;
/**
 * Poder que puede reaparecer en una posición de la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class Power implements Serializable {

    private static final long serialVersionUID = 1L;

    private int chargePower;

    /** Crea un poder con la carga predeterminada de Java, actualmente cero. */
    public Power() {
    }

    /** Punto de extensión para activar el poder; actualmente no consume carga ni produce efectos. */
    public void usePower() {
    }

    /** Punto de extensión para validar la carga; actualmente no comprueba ni devuelve un resultado. */
    private void isUsable() {
    }
}
