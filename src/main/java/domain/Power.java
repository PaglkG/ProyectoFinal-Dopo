package domain;

import java.io.Serializable;
/**
 * Mantiene la recarga común de los poderes especiales en pasos de 1/60 segundo.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class Power implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Pasos de 1/60 segundo acumulados, entre cero y seiscientos. */
    private int chargePower;
    private static final int FULL_CHARGE_STEPS = 600;

    /** Crea un poder sin carga, con diez segundos de recarga pendientes. */
    public Power() {
    }


    /** Aplica el efecto del tiro cuando el balón impacta a un participante rival.
     * @param player participante alcanzado por el balón
     * @param ball balón que conserva la atribución del tiro
     * @param attackerOnLeft indica si el participante atacante estaba a la izquierda
     */
    public abstract void applyImpact(SessionParticipant player, Ball ball, boolean attackerOnLeft);

    /** Inicia la trayectoria del poder desde el contacto con el balón.
     * @param ball balón que recibirá el impulso
     * @param attackerOnLeft indica si el participante atacante está a la izquierda
     * @return {@code true} si la trayectoria se inició
     */
    public abstract boolean startTrajectory(Ball ball, boolean attackerOnLeft);

    /** Consume la carga completa; sin carga suficiente no hace cambios.
     * Los efectos específicos del tiro siguen pendientes.
     */
    public void usePower() {
        if (isUsable()) chargePower = 0;
    }

    /** Avanza la recarga un paso fijo sin superar el máximo de diez segundos. */
    public final void update() {
        chargePower = Math.min(FULL_CHARGE_STEPS, chargePower + 1);
    }

    /** Añade un cuarto de carga por un gol recibido, sin superar el máximo. */
    public final void addGoalBonus() {
        chargePower = Math.min(FULL_CHARGE_STEPS, chargePower + FULL_CHARGE_STEPS / 4);
    }

    /** Reinicia la carga al comenzar una partida nueva. */
    final void resetCharge() {
        chargePower = 0;
    }

    /** Devuelve el tiempo que falta para completar la carga.
     * @return segundos restantes entre cero y diez
     */
    public final double getRemainingSeconds() {
        return (FULL_CHARGE_STEPS - chargePower) / 60.0;
    }

    /** Indica si la carga está completa.
     * @return {@code true} cuando el poder tiene toda su carga
     */
    public final boolean isUsable() {
        return chargePower == FULL_CHARGE_STEPS;
    }
}
