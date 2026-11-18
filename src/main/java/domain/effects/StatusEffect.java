package domain.effects;

import domain.exception.GameException;

/**
 * Efecto temporal que modifica el estado de un participante.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class StatusEffect {

    public static final String STUNNED = "stunned";
    public static final double STUN_DURATION_SECONDS = 3.0;
    private static final double STEP_SECONDS = 1.0 / 60.0;

    private String type;
    private double duration;
    private double remaining;

    /** Crea un efecto temporal con el tipo y la duración indicados. */
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

    /** Devuelve el identificador del efecto. */
    public String getType() { return type; }

    /** Devuelve la duración inicial del efecto en segundos. */
    public double getDuration() { return duration; }

    /** Devuelve el tiempo restante del efecto en segundos. */
    public double getRemaining() { return remaining; }

    /**
     * Devuelve el multiplicador de velocidad aplicado por el efecto.
     * @return cero durante aturdimiento y uno en otro caso
     */
    public double getSpeedFactor() {
        return blocksMovement() ? 0.0 : 1.0;
    }

    /**
     * Devuelve el multiplicador de salto aplicado por el efecto.
     * @return cero durante aturdimiento y uno en otro caso
     */
    public double getJumpFactor() {
        return blocksMovement() ? 0.0 : 1.0;
    }

    /**
     * Indica si el efecto impide moverse.
     * @return {@code true} si el efecto activo es aturdimiento
     */
    public boolean blocksMovement() {
        return STUNNED.equals(type) && !isExpired();
    }
}
