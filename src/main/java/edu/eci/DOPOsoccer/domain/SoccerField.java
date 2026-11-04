package edu.eci.DOPOsoccer.domain;

import java.util.List;
import edu.eci.DOPOsoccer.domain.enums.Side;
import edu.eci.DOPOsoccer.domain.powers.Power;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class SoccerField {

    private double width;
    private double height;
    private Ball ball;
    private List<Power> specialPower;

    /**
     * Updates the field state using the elapsed time.
     */
    // Actualiza el estado de la cancha según el tiempo transcurrido.
    public void update(double dt) {
    }

    /**
     * Resets the ball position.
     */
    // Restablece la posición del balón.
    public void resetBall() {
    }

    /**
     * Checks whether the ball has entered a goal.
     */
    // Comprueba si el balón ha entrado en una portería.
    public Side checkGoal() {
        return null;
    }

    /**
     * Finds a power touching the specified participant.
     */
    // Busca un poder que esté tocando al participante indicado.
    public Power findPowerTouching(SessionParticipant participant) {
        return null;
    }

    /**
     * Creates or selects a power to spawn on the field.
     */
    // Crea o selecciona un poder para aparecer en la cancha.
    public Power spawnPower() {
        return null;
    }
}
