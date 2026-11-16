package domain;

import java.util.List;
import domain.effects.StatusEffect;
import domain.hitbox.Hitbox;
import domain.hitbox.HitboxContact;
import domain.hitbox.HitboxIntersections;
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

    /** Crea el participante con una hitbox provisional en el origen. */
    public SessionParticipant() {
        UserHitbox = new RectangularHitbox(x, y, 450.0 / 20, 150.0 / 3);
    }

    /**
     * Punto de extensión para desplazar al participante; actualmente el cuerpo
     * está vacío y no cambia su posición.
     * @param dir dirección de movimiento
     */
    public void move(boolean dir) {
    }

    /**
     * Devuelve la hitbox mediante la interfaz general de colisiones.
     *
     * @return la hitbox rectangular asociada al participante
     */
    public Hitbox gerHitbox(){
        return  UserHitbox;
    }

    /** Método reservado para iniciar el salto; actualmente no modifica el estado. */
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

    /** Método reservado para activar el poder especial; aún no tiene implementación. */
    public void power() {
    }

    /**
     * Punto de extensión para mejorar la estadística indicada con puntos; el
     * cuerpo está vacío y no modifica ni las estadísticas ni los puntos.
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
     * Cambia el ancho y el alto de la hitbox sin modificar su posición.
     *
     * @param width nuevo ancho
     * @param height nuevo alto
     */
    void resizeHitbox(double width, double height) {
        UserHitbox.resize(width, height);
    }

    /**
     * Comprueba la intersección con otra hitbox.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return nombre de la entidad contactada y su hitbox; si no hay contacto,
     *         un resultado con nombre vacío
     */
    @Override
    public HitboxContact intersects(Hitbox hitbox) {
        return HitboxIntersections.intersects(this, hitbox);
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
