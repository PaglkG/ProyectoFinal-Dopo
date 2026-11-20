package domain;

import domain.Ball;
import domain.SessionParticipant;
import domain.SoccerField;

/** Coordina las respuestas de colisión usando la geometría y entidades de una cancha. */
public final class CollisionFacade {
    private final SoccerField field;
    private final CourtGeometry geometry;
    private final BallParticipantCollisionResolver participantResolver;
    private final BallCollisionResolver ballResolver;

    /** Crea la fachada asociada a la cancha y sus resolutores internos. */
    public CollisionFacade(SoccerField field) {
        this.field = field;
        geometry = new CourtGeometry(field);
        participantResolver = new BallParticipantCollisionResolver();
        ballResolver = new BallCollisionResolver();
    }

    /** Resuelve los contactos entre todos los pares de balones de la cancha. */
    public void resolveBallCollisions(double restitution) {
        ballResolver.resolveAll(field.balls(), restitution);
    }
    /** Resuelve los contactos entre los balones de la cancha y los participantes indicados. */
    public void resolveBallParticipantCollisions(Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        participantResolver.resolveAll(field.balls(), participants, restitution, minimumLandingSpeed);
    }
    /** Resuelve el contacto de un participante que caminó desde {@code previousX}. */
    public void resolveWalkingContact(SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants, double restitution) {
        ballResolver.resolveWalkingContact(field.balls(), moved, previousX, participants,
                restitution, geometry, participantResolver);
    }
    /** Corrige la posición del balón para mantenerlo dentro del área jugable. */
    public void keepBallInsideCourt(Ball ball, double retention) { geometry.keepBallInsideCourt(ball, retention); }
    /** Resuelve rebotes del balón contra paredes y marcos de arco. */
    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        geometry.bounceBallAtWallsAndGoalFrames(ball, retention);
    }
    /** Resuelve el rebote del balón contra el suelo. */
    public void bounceBallAtGround(Ball ball, double retention, double minimumSpeed) {
        geometry.bounceBallAtGround(ball, retention, minimumSpeed);
    }
    /** Indica si el balón rebasa el rectángulo exterior de la cancha. */
    public boolean isOutside(Ball ball) { return geometry.isOutside(ball); }
    /** Indica si el participante rebasa el rectángulo exterior de la cancha. */
    public boolean isOutside(SessionParticipant participant) { return geometry.isOutside(participant); }
    /** Indica si el participante toca el techo o un límite lateral restringido. */
    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        return geometry.touchesForbiddenBoundary(participant);
    }
    /** Indica si el balón está en contacto con el suelo. */
    public boolean touchesGround(Ball ball) { return geometry.touchesGround(ball); }
    /** Devuelve la coordenada vertical del centro del balón apoyado en el suelo. */
    public double groundCenterY(Ball ball) { return geometry.groundCenterY(ball); }
    /** Devuelve {@code left}, {@code right} o una cadena vacía según el arco que contiene el balón. */
    public String goalSideFor(Ball ball) { return geometry.goalSideFor(ball); }
}
