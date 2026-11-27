package domain;

/** Poder de Camerún: tiro recto cubierto de electricidad. */
public final class Lightning extends Power {
    /** Daño base del impacto eléctrico. */
    private static final int BASE_DAMAGE = 12;

    /** Crea el poder Lightning. */
    public Lightning() { }

    @Override
    public boolean startTrajectory(Ball ball, boolean attackerOnLeft) {
        double speed = attackerOnLeft ? 400.0 : -400.0;
        ball.applyImpulse(speed - ball.getVelocityX(), 0);
        return true;
    }

    @Override
    public void applyImpact(SessionParticipant player, Ball ball, boolean attackerOnLeft) {
        StatusEffect effect = new StatusEffect(StatusEffect.ELECTROCUTED, 3.0);
        player.applyStatusEffect(effect);
        int damage = (int) Math.round(BASE_DAMAGE * powerShotKickMultiplier(ball));
        player.takeDamage(damage);
    }

    /** Lee el multiplicador del atacante conservado en el balón del tiro. */
    private double powerShotKickMultiplier(Ball ball) {
        double multiplier = ball.getPowerShotKickMultiplier();
        return multiplier > 0 ? multiplier : 1.0;
    }
}
