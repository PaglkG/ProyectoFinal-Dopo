package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import domain.StatusEffect;
import domain.Hitbox;
import domain.HitboxContact;
import domain.HitboxIntersections;
import domain.RectangularHitbox;
import domain.Power;
import domain.GameException;

/**
 * Define el estado compartido por jugadores y bots durante una sesión.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class SessionParticipant implements Serializable, Collidable {

    private static final long serialVersionUID = 1L;
    private static final ParticipantState NORMAL_STATE = new NormalState();
    private static final StunnedState STUNNED_STATE = new StunnedState();
    private static final double JUMP_SPEED = 121.0;
    private static final double JUMP_GRAVITY = 180.0;

    /** Coordenada horizontal anterior usada para resolver el recorrido de colisión. */
    private double previousX;
    /** Coordenada vertical anterior usada para resolver el recorrido de colisión. */
    private double previousY;
    /** Indica si la trayectoria vertical del salto sigue activa. */
    private boolean jumping;
    /** Velocidad vertical del salto en unidades por segundo, positiva hacia abajo. */
    private double jumpVelocity;
    /** Rectángulo que almacena la posición física del participante. */
    private RectangularHitbox UserHitbox;
    /** Estadísticas y puntos que pertenecen al participante. */
    private Stats stats;
    /** Efectos temporales activos sobre el participante. */
    private List<StatusEffect> status;
    /** Poder asociado al participante al configurar la partida. */
    private Power specialPower;
    /** Vida que conserva el participante durante la partida. */
    private int life = Stats.BASE_LIFE;

    /** Crea el participante con una hitbox provisional en el origen. */
    public SessionParticipant() {
        stats = new Stats();
        status = new ArrayList<>();
        UserHitbox = new RectangularHitbox(0, 0, 450.0 / 20, 150.0 / 3);
    }

    /**
     * Desplaza horizontalmente al participante a 150 unidades por segundo
     * multiplicadas por su estadística {@code speed}, durante un paso fijo de
     * 1/60 segundo.
     * @param dir {@code true} para derecha, {@code false} para izquierda
     */
    public void move(boolean dir) {
        currentState().move(this, dir);
    }

    /** Distancia horizontal de un paso de movimiento para la velocidad actual. */
    final double horizontalStep() {
        return 150.0 * stats.getSpeed() / 60.0;
    }

    /** Devuelve las estadísticas asociadas a este participante.
     * @return estadísticas actuales del participante
     */
    public Stats getStats() {
        return stats;
    }

    /** Devuelve la vida actual del participante.
     * @return vida restante
     */
    public int getLife() { return life; }

    /** Devuelve la vida máxima calculada por la estadística {@code survival}.
     * @return vida máxima entre 100 y 190
     */
    public int getMaxLife() { return stats.getMaxLife(); }

    /** Indica si algún efecto activo bloquea el movimiento.
     * @return {@code true} si el participante no puede moverse
     */
    public boolean blocksMovement() {
        return currentState().blocksMovement();
    }

    /** Deriva el comportamiento de los efectos vigentes, incluso tras cargar una partida. */
    private ParticipantState currentState() {
        return status.stream().anyMatch(StatusEffect::blocksMovement) ? STUNNED_STATE : NORMAL_STATE;
    }

    /** Reduce la vida sin permitir que baje de cero y aturde al llegar a cero.
     * @param damage cantidad de daño positivo que se aplicará
     */
    public void takeDamage(int damage) {
        if (damage <= 0) throw new GameException(GameException.INVALID_DAMAGE);
        life = Math.max(0, life - damage);
        if (life == 0) STUNNED_STATE.enter(this);
    }

    /** Agrega un efecto y reemplaza cualquier efecto previo del mismo tipo.
     * @param effect efecto temporal que se aplicará
     */
    public void applyStatusEffect(StatusEffect effect) {
        if (effect == null) throw new GameException(GameException.INVALID_STATUS_EFFECT);
        status.removeIf(active -> active.getType().equals(effect.getType()));
        status.add(effect);
    }

    /** Avanza los efectos un paso y restaura la vida máxima actual al terminar el aturdimiento. */
    public void updateStatusEffects() {
        Iterator<StatusEffect> iterator = status.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.update();
            if (effect.isExpired()) {
                if (StatusEffect.STUNNED.equals(effect.getType())) STUNNED_STATE.recover(this);
                iterator.remove();
            }
        }
    }

    /** Asocia estadísticas y ajusta la vida actual a la vida máxima resultante.
     * @param stats estadísticas no nulas que se asociarán
     */
    public void setStats(Stats stats) {
        if (stats == null) {
            throw new domain.GameException(domain.GameException.INVALID_STATISTICS);
        }
        this.stats = stats;
        life = stats.getMaxLife();
    }

    /** Restaura vida, efectos y salto al comenzar un partido, sin tocar estadísticas. */
    void resetForNewMatch() {
        life = stats.getMaxLife();
        status.clear();
        finishJump();
        if (specialPower != null) specialPower.resetCharge();
    }

    /**
     * Devuelve la hitbox mediante la interfaz general de colisiones.
     *
     * @return la hitbox rectangular asociada al participante
     */
    public Hitbox getHitboxAsInterface(){
        return  UserHitbox;
    }

    /** Inicia el impulso de un salto si el participante puede actuar y no tiene
     * una trayectoria vertical activa. La sesión valida el apoyo antes de invocarlo.
     */
    public void jump() {
        if (jumping || blocksMovement()) return;
        jumping = true;
        jumpVelocity = -JUMP_SPEED;
    }

    /** Indica si los pies están sobre el apoyo identificado por la sesión y permiten despegar. */
    boolean canStartJump(double supportY, double ceilingY) {
        return jumpVelocity >= 0 && !blocksMovement()
                && UserHitbox.getY() == supportY - UserHitbox.getHeight()
                && UserHitbox.getY() > ceilingY;
    }

    /** Rearma el impulso al saltar desde un apoyo, incluida la cabeza de otro participante. */
    void jumpFromSupport(double supportY, double ceilingY) {
        if (!canStartJump(supportY, ceilingY)) return;
        jumping = false;
        jump();
    }

    /** Calcula la subida máxima con el mismo paso discreto que usa la simulación. */
    static double maximumJumpRise(double stepSeconds) {
        double decelerationPerStep = JUMP_GRAVITY * stepSeconds;
        double risingSteps = Math.floor(JUMP_SPEED / decelerationPerStep);
        return risingSteps * stepSeconds * (JUMP_SPEED - decelerationPerStep * (risingSteps + 1) / 2);
    }

    /** Propone la próxima altura. La sesión comprueba la cancha antes de aplicarla. */
    double nextJumpY(double stepSeconds) {
        if (!jumping) return UserHitbox.getY();
        jumpVelocity += JUMP_GRAVITY * stepSeconds;
        return UserHitbox.getY() + jumpVelocity * stepSeconds;
    }

    /** Desplaza la hitbox sin perder el origen horizontal del paso y resuelve el límite del salto. */
    void applyJumpStep(double nextY, double allowedY, double groundTop) {
        if (!jumping) return;
        setPosition(UserHitbox.getX(), allowedY);
        if (allowedY >= groundTop) finishJump();
        else if (allowedY != nextY) jumpVelocity = 0;
    }

    /** Restablece el estado del salto al tocar el suelo o reiniciar la ronda. */
    private void finishJump() {
        jumping = false;
        jumpVelocity = 0;
    }

    /**
     * Restablece la posición del participante.
     * @param x coordenada horizontal
     * @param y coordenada vertical
     */
    public void resetPosition(double x, double y) {
        finishJump();
        setPosition(x, y);
        rememberCurrentPosition();
    }

    /** Método reservado para activar el poder especial; aún no tiene implementación. */
    public void power() {
    }

    /** Devuelve el poder asignado a este participante.
     * @return poder actual o {@code null} si aún no se asignó uno
     */
    public Power getSpecialPower() {
        return specialPower;
    }

    /** Asigna el poder especial del participante.
     * @param specialPower poder que se asociará
     */
    public void setSpecialPower(Power specialPower) {
        this.specialPower = specialPower;
    }

    /**
     * Mejora la estadística indicada usando el saldo del participante. Si cambia
     * {@code survival}, aumenta la vida actual y máxima en la misma cantidad.
     *
     * @param stats estadística que se mejorará
     */
    public void improveStatistics(String stats) {
        int previousMaxLife = this.stats.getMaxLife();
        this.stats.improveStatistics(stats);
        int newMaxLife = this.stats.getMaxLife();
        if (newMaxLife > previousMaxLife) {
            int increase = newMaxLife - previousMaxLife;
            life = Math.min(newMaxLife, life + increase);
        }
    }

    /**
     * Devuelve la hitbox rectangular del participante.
     *
     * @return hitbox del participante
     */
    public RectangularHitbox getHitbox() {
        return UserHitbox;
    }

    /** @return hitbox rectangular usada para detectar contactos */
    @Override
    public CollisionShape getCollisionShape() {
        return UserHitbox;
    }

    double getPreviousX() { return previousX; }

    double getPreviousY() { return previousY; }

    /** Marca la posición vigente como base para el siguiente paso de simulación. */
    void rememberCurrentPosition() {
        previousX = UserHitbox.getX();
        previousY = UserHitbox.getY();
    }

    /** Corrige la posición sin reemplazar el origen del movimiento del frame. */
    void repositionForCollision(double x, double y) {
        setPosition(x, y);
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
     * Comprueba el contacto con otra entidad o hitbox mediante la geometría centralizada.
     *
     * @param target entidad o hitbox que se comparará
     * @return nombre de la entidad contactada y su hitbox; si no hay contacto,
     *         un resultado con nombre vacío
     */
    public HitboxContact intersects(Object target) {
        return HitboxIntersections.intersects(this, target);
    }

    /**
     * Reposiciona al participante y la esquina superior izquierda de su hitbox rectangular.
     *
     * @param x nueva coordenada horizontal de la esquina superior izquierda
     * @param y nueva coordenada vertical de la esquina superior izquierda
     */
    public void reposition(double x, double y) {
        previousX = UserHitbox.getX();
        previousY = UserHitbox.getY();
        setPosition(x, y);
    }

    private void setPosition(double x, double y) {
        UserHitbox.reposition(x, y);
    }

    /** Comportamiento del participante según los efectos que permiten o bloquean sus acciones. */
    private interface ParticipantState {
        /** Indica si el estado bloquea el movimiento y la patada.
         * @return {@code true} cuando el participante tiene las acciones bloqueadas
         */
        boolean blocksMovement();

        /** Procesa un intento de movimiento horizontal en el estado vigente.
         * @param participant participante que intenta moverse
         * @param right {@code true} para mover a la derecha, {@code false} a la izquierda
         */
        void move(SessionParticipant participant, boolean right);
    }

    /** Permite el movimiento horizontal cuando los efectos activos no lo impiden. */
    private static final class NormalState implements ParticipantState {
        /** {@inheritDoc} */
        @Override
        public boolean blocksMovement() { return false; }

        /** {@inheritDoc} */
        @Override
        public void move(SessionParticipant participant, boolean right) {
            double direction = right ? 1 : -1;
            double distance = direction * participant.horizontalStep();
            RectangularHitbox hitbox = participant.UserHitbox;
            participant.reposition(hitbox.getX() + distance, hitbox.getY());
        }
    }

    /** Impide el movimiento durante el aturdimiento y restaura la vida al vencer ese efecto. */
    private static final class StunnedState implements ParticipantState {
        /** {@inheritDoc} */
        @Override
        public boolean blocksMovement() { return true; }

        /** {@inheritDoc} */
        @Override
        public void move(SessionParticipant participant, boolean right) {
            // El intento no modifica la posición durante el bloqueo.
        }

        /** Activa o renueva el aturdimiento cuando el daño deja la vida en cero. */
        private void enter(SessionParticipant participant) {
            participant.applyStatusEffect(new StatusEffect(StatusEffect.STUNNED,
                    StatusEffect.STUN_DURATION_SECONDS));
        }

        /** Devuelve al participante su vida máxima al vencer el efecto de aturdimiento. */
        private void recover(SessionParticipant participant) {
            participant.life = participant.getMaxLife();
        }
    }
}
