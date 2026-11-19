package domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import domain.StatusEffect;
import domain.hitbox.Hitbox;
import domain.hitbox.HitboxContact;
import domain.hitbox.HitboxIntersections;
import domain.hitbox.RectangularHitbox;
import domain.Power;
import domain.GameException;

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
    private int maxLife = 100;
    private int life = maxLife;

    /** Crea el participante con una hitbox provisional en el origen. */
    public SessionParticipant() {
        stats = new Stats();
        status = new ArrayList<>();
        UserHitbox = new RectangularHitbox(x, y, 450.0 / 20, 150.0 / 3);
    }

    /**
     * Desplaza horizontalmente al participante a 150 unidades por segundo
     * multiplicadas por su estadística {@code speed}, durante un paso fijo de
     * 1/60 segundo.
     * @param dir {@code true} para derecha, {@code false} para izquierda
     */
    public void move(boolean dir) {
        if (blocksMovement()) return;
        double direction = dir ? 1 : -1;
        double distance = direction * 150.0 * stats.getSpeed() / 60.0;
        reposition(x + distance, y);
    }

    /** Devuelve las estadísticas asociadas a este participante. */
    public Stats getStats() {
        return stats;
    }

    /** Devuelve la vida actual del participante. */
    public int getLife() { return life; }

    /** Devuelve la vida máxima del participante. */
    public int getMaxLife() { return maxLife; }

    /** @return {@code true} cuando algún efecto activo bloquea el movimiento */
    public boolean blocksMovement() {
        return status.stream().anyMatch(StatusEffect::blocksMovement);
    }

    /** Reduce la vida sin permitir que baje de cero y aturde al llegar a cero. */
    public void takeDamage(int damage) {
        if (damage <= 0) throw new GameException(GameException.INVALID_DAMAGE);
        life = Math.max(0, life - damage);
        if (life == 0) applyStatusEffect(new StatusEffect(StatusEffect.STUNNED,
                StatusEffect.STUN_DURATION_SECONDS));
    }

    /** Agrega un efecto y reemplaza cualquier efecto previo del mismo tipo. */
    public void applyStatusEffect(StatusEffect effect) {
        if (effect == null) throw new GameException(GameException.INVALID_STATUS_EFFECT);
        status.removeIf(active -> active.getType().equals(effect.getType()));
        status.add(effect);
    }

    /** Avanza los efectos un paso y restaura la vida cuando termina el aturdimiento. */
    public void updateStatusEffects() {
        Iterator<StatusEffect> iterator = status.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.update();
            if (effect.isExpired()) {
                if (StatusEffect.STUNNED.equals(effect.getType())) life = maxLife;
                iterator.remove();
            }
        }
    }

    /** Asocia estadísticas, por ejemplo las recuperadas por {@link Game} para un usuario. */
    public void setStats(Stats stats) {
        if (stats == null) {
            throw new domain.GameException(domain.GameException.INVALID_STATISTICS);
        }
        this.stats = stats;
    }

    /**
     * Devuelve la hitbox mediante la interfaz general de colisiones.
     *
     * @return la hitbox rectangular asociada al participante
     */
    public Hitbox getHitboxAsInterface(){
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

    /** Devuelve el poder asignado a este participante. */
    public Power getSpecialPower() {
        return specialPower;
    }

    /** Asigna el poder especial del participante. */
    public void setSpecialPower(Power specialPower) {
        this.specialPower = specialPower;
    }

    /**
     * Punto de extensión para mejorar la estadística indicada con puntos; el
     * cuerpo está vacío y no modifica ni las estadísticas ni los puntos.
     *
     * @param stats estadística que se mejorará
     * @param points puntos asignados a la mejora
     */
    public void improveStatistics(String stats) {
        this.stats.improveStatistics(stats);
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
    public void resizeHitbox(double width, double height) {
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
