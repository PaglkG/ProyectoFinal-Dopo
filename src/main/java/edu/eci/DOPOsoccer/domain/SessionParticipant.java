package edu.eci.DOPOsoccer.domain;

import java.util.List;
import java.util.Set;
import edu.eci.DOPOsoccer.domain.enums.Action;
import edu.eci.DOPOsoccer.domain.effects.StatusEffect;
import edu.eci.DOPOsoccer.domain.powers.Power;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public abstract class SessionParticipant {

    private int side;
    private double x;
    private double y;
    private int speed;
    private int jump;
    private int power;

    private Outfit outfit;
    private List<StatusEffect> status;

    /**
     * Creates a session participant.
     */
    // Inicializa un participante de la sesión.
    public SessionParticipant() {
    }

    /**
     * Performs the actions received from the participant.
     */
    // Ejecuta las acciones recibidas del participante.
    public void perform(Set<Action> actions) {
    }

    /**
     * Moves the participant in the selected direction.
     */
    // Mueve al participante en la dirección seleccionada.
    public void move(Action dir) {
    }

    /**
     * Requests a participant jump.
     */
    // Solicita el salto del participante.
    public void jump() {
    }

    /**
     * Requests the use of the stored power.
     */
    // Solicita el uso del poder almacenado.
    public void power() {
    }

    /**
     * Kicks the specified ball.
     */
    // Patea el balón indicado.
    public void kick(Ball ball) {
    }

    /**
     * Attempts to pick up the specified power.
     */
    // Intenta recoger el poder indicado.
    public boolean pickUp(Power power) {
        return false;
    }

    /**
     * Indicates whether the participant can store a power.
     */
    // Indica si el participante puede almacenar un poder.
    public boolean canStorePower() {
        return false;
    }

    /**
     * Uses the power stored by the participant.
     */
    // Utiliza el poder que tiene almacenado el participante.
    public void takeStoredPower() {
    }

    /**
     * Adds a status effect to the participant.
     */
    // Agrega un efecto de estado al participante.
    public void addEffect(StatusEffect effect) {
    }

    /**
     * Updates the participant effects using elapsed time.
     */
    // Actualiza los efectos del participante según el tiempo transcurrido.
    public void updateEffects(double dt) {
    }

    /**
     * Removes the active effects from the participant.
     */
    // Elimina los efectos activos del participante.
    public void clearEffects() {
    }

    /**
     * Sets the participant position to the specified coordinates.
     */
    // Establece la posición del participante en las coordenadas indicadas.
    public void resetPosition(double x, double y) {
    }

    /**
     * Returns the participant speed.
     */
    // Devuelve la velocidad del participante.
    public int getSpeed() {
        return 0;
    }

    /**
     * Returns the participant jump power.
     */
    // Devuelve la potencia de salto del participante.
    public int getJumpPower() {
        return 0;
    }

    /**
     * Indicates whether the participant can move.
     */
    // Indica si el participante puede moverse.
    public boolean canMove() {
        return false;
    }
}
