package domain;

import java.util.List;
import domain.effects.StatusEffect;
import domain.hitbox.Hitbox;
import domain.hitbox.RectangularHitbox;
import domain.powers.Power;

/**
 * Define el estado compartido por jugadores y bots durante una sesión.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class SessionParticipant implements Hitbox {

    private double x;
    private double y;
    private RectangularHitbox UserHitbox;
    private Stats stats;
    private List<StatusEffect> status;
    private Power specialPower;

    /** Crea un participante de la sesión con una hitbox de tamaño provisional. */
    public SessionParticipant() {
        UserHitbox = new RectangularHitbox(x, y, 450.0 / 20, 150.0 / 3);
    }

    /**
     * Desplaza al participante en la dirección indicada.
     * @param dir dirección de movimiento
     */
    public void move(boolean dir) {
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
        reposition(x, y);
    }

    /** Activa el poder especial del participante. */
    public void power() {
    }

    /**
     * Mejora la estadística indicada usando los puntos disponibles.
     *
     * @param stats estadística que se mejorará
     * @param points puntos asignados a la mejora
     */
    public void ImproveStatistics(String stats, int points) {
    }

    /**
     * Devuelve la hitbox rectangular del participante.
     *
     * @return hitbox del participante
     */
    public RectangularHitbox getHitbox() {
        return UserHitbox;
    }

    /**
     * Comprueba la intersección con otra hitbox.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return {@code false} hasta que se definan las reglas de intersección
     */
    @Override
    public boolean intersects(Hitbox hitbox) {
        return false;
    }

    /**
     * Reposiciona al participante y su hitbox rectangular.
     *
     * @param x nueva coordenada horizontal de la esquina superior izquierda
     * @param y nueva coordenada vertical de la esquina superior izquierda
     */
    @Override
    public void reposition(double x, double y) {
        this.x = x;
        this.y = y;
        if (UserHitbox != null) {
            UserHitbox.reposition(x, y);
        }
    }
}
