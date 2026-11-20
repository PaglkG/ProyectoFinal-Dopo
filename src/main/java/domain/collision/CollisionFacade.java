package domain.collision;

import domain.Ball;
import domain.SessionParticipant;
import domain.SoccerField;

/** Coordina las respuestas de colisión usando la geometría y entidades de una cancha. */
public final class CollisionFacade {
    private final SoccerField field;
    private final CourtGeometry geometry;
    private final BallCollisionResolver ballResolver;
    private final BallParticipantCollisionResolver participantResolver;

    public CollisionFacade(SoccerField field) {
        this.field = field;
        geometry = new CourtGeometry(field);
        ballResolver = new BallCollisionResolver();
        participantResolver = new BallParticipantCollisionResolver();
    }

    public void resolveBallCollisions(double restitution) {
        ballResolver.resolveAll(field.balls(), restitution);
    }
    public void resolveBallParticipantCollisions(Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        participantResolver.resolveAll(field.balls(), participants, restitution, minimumLandingSpeed);
    }
    public void resolveWalkingContact(SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants, double restitution) {
        ballResolver.resolveWalkingContact(field.balls(), moved, previousX, participants, restitution, geometry);
    }
    public void keepBallInsideCourt(Ball ball, double retention) { geometry.keepBallInsideCourt(ball, retention); }
    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        geometry.bounceBallAtWallsAndGoalFrames(ball, retention);
    }
    public void bounceBallAtGround(Ball ball, double retention, double minimumSpeed) {
        geometry.bounceBallAtGround(ball, retention, minimumSpeed);
    }
    public boolean isOutside(Ball ball) { return geometry.isOutside(ball); }
    public boolean isOutside(SessionParticipant participant) { return geometry.isOutside(participant); }
    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        return geometry.touchesForbiddenBoundary(participant);
    }
    public boolean touchesGround(Ball ball) { return geometry.touchesGround(ball); }
    public double groundCenterY(Ball ball) { return geometry.groundCenterY(ball); }
    public String goalSideFor(Ball ball) { return geometry.goalSideFor(ball); }
}
