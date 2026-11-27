package domain;

/** Poder de Corea del Sur: tiro recto y horizontal de alta velocidad. */
public final class BlueAura extends Power {
    /** Crea el poder BlueAura. */
    public BlueAura() { }

    @Override
    public boolean startTrajectory(Ball ball, boolean attackerOnLeft) {
        double speed = attackerOnLeft ? 400.0 : -400.0;
        ball.applyImpulse(speed - ball.getVelocityX(), 0);
        return true;
    }

    @Override
    public void applyImpact(SessionParticipant player, Ball ball, boolean attackerOnLeft) {
        player.takeDamage(100);
    }
}
