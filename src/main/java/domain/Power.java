package domain;

/**
 * Poder que puede reaparecer en una posición de la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class Power {

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
