package domain.powers;

/**
 * Poder que puede reaparecer en una posición de la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class Power {

    private int chargePower;

    /** Crea un poder. */
    public Power() {
    }

    /** Activa el poder si tiene carga suficiente. */
    public void usePower() {
    }

    /** Comprueba si el poder se puede usar. */
    private void isUsable() {
    }
}
