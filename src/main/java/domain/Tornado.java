package domain;

/** Poder de Nigeria: remolino a ras del suelo que rebota rápidamente. */
public final class Tornado extends Power {
    /** Daño base del impacto del remolino. */
    private static final int BASE_DAMAGE = 8;

    /** Crea el poder Tornado. */
    public Tornado() { }


    @Override
    public boolean startTrajectory(Ball ball, boolean attackerOnLeft) {
        double speed = attackerOnLeft ? 400.0 : -400.0;
        ball.applyImpulse(speed - ball.getVelocityX(), 0);
        return true;
    }

    @Override
    public void applyImpact(SessionParticipant player, Ball ball, boolean attackerOnLeft) {
        // Inicia el impulso antes del aturdimiento, que bloquea los saltos voluntarios.
        player.forceJump();
        StatusEffect effect = new StatusEffect(StatusEffect.STUNNED, 3.0);
        player.applyStatusEffect(effect);
        double multiplier = ball.getPowerShotKickMultiplier();
        if (multiplier <= 0) multiplier = 1.0;
        player.takeDamage((int) Math.round(BASE_DAMAGE * multiplier));
    }
}
