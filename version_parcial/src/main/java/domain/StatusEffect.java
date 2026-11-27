package domain;

import java.io.Serializable;
import domain.GameException;

/**
 * Efecto temporal que modifica el estado de un participante.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class StatusEffect implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador del efecto de aturdimiento. */
    public static final String STUNNED = "stunned";
    /** Identificador del efecto eléctrico que reduce velocidad e impide saltar. */
    public static final String ELECTROCUTED = "electrocuted";
    /** Duración del efecto de aturdimiento, en segundos. */
    public static final double STUN_DURATION_SECONDS = 3.0;
    private static final double STEP_SECONDS = 1.0 / 60.0;

    /** Identificador del efecto temporal. */
    private String type;
    /** Tiempo inicial del efecto en segundos. */
    private double duration;
    /** Segundos que quedan antes de que expire el efecto. */
    private double remaining;

    /** Crea un efecto temporal con el tipo y la duración indicados.
     * @param type identificador no vacío del efecto
     * @param duration duración positiva y finita en segundos
     * @throws GameException si el tipo o la duración no son válidos
     */
    public StatusEffect(String type, double duration) {
        if (type == null || type.isBlank() || !Double.isFinite(duration) || duration <= 0) {
            throw new GameException(GameException.INVALID_STATUS_EFFECT);
        }
        this.type = type;
        this.duration = duration;
        this.remaining = duration;
    }

    /** Avanza el efecto un paso fijo de 1/60 segundo. */
    public void update() {
        remaining = remaining <= STEP_SECONDS + 1e-12 ? 0 : remaining - STEP_SECONDS;
    }

    /**
     * Comprueba si el tiempo restante llegó a cero.
     * @return {@code true} si el efecto terminó
     */
    public boolean isExpired() {
        return remaining <= 0;
    }

    /** Reinicia la duración del efecto. */
    public void refresh() {
        remaining = duration;
    }

    /** Devuelve el identificador del efecto.
     * @return tipo de efecto
     */
    public String getType() { return type; }

    /** Devuelve la duración inicial del efecto.
     * @return duración en segundos
     */
    public double getDuration() { return duration; }

    /** Devuelve el tiempo restante del efecto.
     * @return segundos restantes
     */
    public double getRemaining() { return remaining; }

    /**
     * Devuelve el multiplicador de velocidad aplicado por el efecto.
     * @return cero durante aturdimiento, la mitad durante electrocución y uno en otro caso
     */
    public double getSpeedFactor() {
        if (blocksMovement()) return 0.0;
        return ELECTROCUTED.equals(type) && !isExpired() ? 0.5 : 1.0;
    }

    /**
     * Devuelve el multiplicador de salto aplicado por el efecto.
     * @return cero durante aturdimiento o electrocución y uno en otro caso
     */
    public double getJumpFactor() {
        return blocksJump() ? 0.0 : 1.0;
    }

    /**
     * Indica si el efecto impide moverse.
     * @return {@code true} si el efecto activo es aturdimiento
     */
    public boolean blocksMovement() {
        return STUNNED.equals(type) && !isExpired();
    }

    /** Indica si el efecto vigente impide saltar.
     * @return {@code true} durante aturdimiento o electrocución
     */
    public boolean blocksJump() {
        return (STUNNED.equals(type) || ELECTROCUTED.equals(type)) && !isExpired();
    }
}
