package domain;

/** Poder de Estados Unidos: divide visualmente el balón en ocho copias. */
public final class Illusion extends Power {
    /** Daño base que causa el impacto del balón ilusorio. */
    private static final int BASE_DAMAGE = 6;

    /** Crea el poder Illusion. */
    public Illusion() { }

    /**
     * Impulsa el balón horizontalmente hacia el arco contrario.
     * @param ball balón que inicia el tiro
     * @param attackerOnLeft indica si el participante atacante está a la izquierda
     * @return {@code true} porque el tiro se inicia
     */
    @Override
    public boolean startTrajectory(Ball ball, boolean attackerOnLeft) {
        double speed = attackerOnLeft ? 400.0 : -400.0;
        ball.applyImpulse(speed - ball.getVelocityX(), 0);
        return true;
    }

    /**
     * Aplica al rival el daño base de Illusion multiplicado por su nivel de patada.
     * El resolutor de colisiones solo invoca este método cuando el balón alcanza
     * al lado opuesto al atacante.
     * @param player participante rival alcanzado
     * @param ball balón que conserva la atribución del tiro
     * @param attackerOnLeft indica si el participante atacante estaba a la izquierda
     */
    @Override
    public void applyImpact(SessionParticipant player, Ball ball, boolean attackerOnLeft) {
        double kickMultiplier = ball.getPowerShotKickMultiplier();
        if (kickMultiplier <= 0) kickMultiplier = 1.0;
        int damage = (int) Math.round(BASE_DAMAGE * kickMultiplier);
        player.takeDamage(damage);
    }
}
