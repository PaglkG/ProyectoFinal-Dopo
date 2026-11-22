package domain;

/**
 * Acciones que puede realizar un participante.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public enum Action {
    /** Movimiento horizontal hacia la izquierda. */
    LEFT,
    /** Movimiento horizontal hacia la derecha. */
    RIGHT,
    /** Solicitud de salto. */
    JUMP,
    /** Activación del poder especial. */
    POWER
}
