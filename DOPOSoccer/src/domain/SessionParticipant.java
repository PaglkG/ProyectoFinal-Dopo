package edu.eci.DOPOSoccer.domain;

import java.util.List;
import edu.eci.DOPOSoccer.domain.effects.StatusEffect;
import edu.eci.DOPOSoccer.domain.enums.Action;

/**
 * Define el estado compartido por jugadores y bots durante una sesión.
 * La vida máxima sigue vidaMáxima = 100 × (1 + 0,10 × (supervivencia - 1)).
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class SessionParticipant {

    private double x;
    private double y;
    private Outfit outfit;
    private Stats stats;
    private List<StatusEffect> status;

    /** Crea un participante de la sesión. */
    public SessionParticipant() {
    }

    /**
     * Desplaza al participante según la acción indicada.
     * @param dir acción de movimiento
     */
    public void move(Action dir) {
    }

    /** Inicia el salto del participante. */
    public void jump() {
    }

    /**
     * Restablece la posición del participante.
     * @param x coordenada horizontal
     * @param y coordenada vertical
     */
    public void resetPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }


    
}
