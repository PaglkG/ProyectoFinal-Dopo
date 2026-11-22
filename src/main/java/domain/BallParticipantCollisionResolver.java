package domain;

import domain.Ball;
import domain.SessionParticipant;
import domain.RectangularHitbox;

/** Resuelve la penetración y el rebote de balones contra hitboxes de participantes. */
public final class BallParticipantCollisionResolver {
    /** Crea el resolutor de contactos entre balones y participantes. */
    public BallParticipantCollisionResolver() { }

    /** Resuelve los contactos de cada balón con cada participante.
     * @param balls balones que se comprobarán
     * @param participants participantes que pueden contactar con los balones
     * @param restitution fracción de velocidad normal conservada en el rebote
     * @param minimumLandingSpeed velocidad vertical mínima al aterrizar sobre un balón
     */
    public void resolveAll(Ball[] balls, Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        for (Ball ball : balls) {
            for (SessionParticipant participant : participants) {
                resolve(ball, participant, ball.getPreviousX(), ball.getPreviousY(),
                        participant.getPreviousX(), participant.getPreviousY(),
                        restitution, minimumLandingSpeed);
            }
        }
    }

    /** Resuelve el contacto usando las posiciones previas registradas por las entidades.
     * @param ball balón que se comprueba
     * @param participant participante que se comprueba
     * @param restitution fracción de velocidad normal conservada en el rebote
     * @param minimumLandingSpeed velocidad vertical mínima al aterrizar sobre el balón
     */
    public void resolve(Ball ball, SessionParticipant participant,
            double restitution, double minimumLandingSpeed) {
        resolve(ball, participant, ball.getPreviousX(), ball.getPreviousY(),
                participant.getPreviousX(), participant.getPreviousY(),
                restitution, minimumLandingSpeed);
    }

    /**
     * Resuelve un contacto surgido al caminar. Las posiciones recibidas son
     * las del instante inmediatamente anterior al movimiento del participante.
     */
    void resolve(Ball ball, SessionParticipant participant,
            double previousBallX, double previousBallY,
            double previousParticipantX, double previousParticipantY,
            double restitution, double minimumLandingSpeed) {
        RectangularHitbox box = participant.getHitbox();
        CircleRectangleCollisionResolver.Resolution result = CircleRectangleCollisionResolver.resolve(
                ball, box, previousBallX, previousBallY,
                previousParticipantX, previousParticipantY, restitution);
        if (result == null || result.axis() != CollisionAxisResolver.Axis.VERTICAL
                || result.side() >= 0 || !result.bounced()) {
            return;
        }

        if (ball.getVelocityY() < minimumLandingSpeed) {
            ball.applyImpulse(0, minimumLandingSpeed - ball.getVelocityY());
        }
    }
}
