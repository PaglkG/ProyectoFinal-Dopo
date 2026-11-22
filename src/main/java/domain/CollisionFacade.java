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

    /** Crea la fachada asociada a la cancha y sus resolutores internos.
     * @param field cancha cuyas colisiones se resolverán
     */
    public CollisionFacade(SoccerField field) {
        this.field = field;
        geometry = new CourtGeometry(field);
        participantResolver = new BallParticipantCollisionResolver();
        ballResolver = new BallCollisionResolver();
    }

    /** Resuelve los contactos entre todos los pares de balones de la cancha.
     * @param restitution fracción de velocidad normal conservada en los rebotes
     */
    public void resolveBallCollisions(double restitution) {
        ballResolver.resolveAll(field.balls(), restitution);
    }
    /** Resuelve los contactos entre los balones de la cancha y los participantes indicados.
     * @param participants participantes que pueden contactar con los balones
     * @param restitution fracción de velocidad normal conservada en los rebotes
     * @param minimumLandingSpeed velocidad vertical mínima al aterrizar sobre un balón
     */
    public void resolveBallParticipantCollisions(Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        participantResolver.resolveAll(field.balls(), participants, restitution, minimumLandingSpeed);
    }
    /** Empuja los balones que alcanza un participante al caminar desde {@code previousX}.
     * @param moved participante que caminó
     * @param previousX coordenada horizontal que tenía antes de moverse
     * @param participants participantes que pueden bloquear el empuje
     */
    public void resolveWalkingContact(SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants) {
        ballResolver.resolveWalkingContact(field.balls(), moved, previousX, participants,
                geometry);
    }
    /** Corrige la posición del balón para mantenerlo dentro del área jugable.
     * @param ball balón que se mantendrá dentro de la cancha
     * @param retention fracción de velocidad conservada tras el rebote
     */
    public void keepBallInsideCourt(Ball ball, double retention) { geometry.keepBallInsideCourt(ball, retention); }
    /** Resuelve rebotes del balón contra paredes y marcos de arco.
     * @param ball balón cuyo contacto se resolverá
     * @param retention fracción de velocidad normal conservada tras el rebote
     */
    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        geometry.bounceBallAtWallsAndGoalFrames(ball, retention);
    }
    /** Resuelve el rebote del balón contra el suelo.
     * @param ball balón cuyo contacto se resolverá
     * @param retention fracción de velocidad vertical conservada tras el rebote
     * @param minimumSpeed velocidad mínima para conservar el rebote
     */
    public void bounceBallAtGround(Ball ball, double retention, double minimumSpeed) {
        geometry.bounceBallAtGround(ball, retention, minimumSpeed);
    }
    /** Indica si el balón rebasa el rectángulo exterior de la cancha.
     * @param ball balón que se comprobará
     * @return {@code true} si alguna parte del balón rebasa la cancha
     */
    public boolean isOutside(Ball ball) { return geometry.isOutside(ball); }
    /** Indica si el participante rebasa el rectángulo exterior de la cancha.
     * @param participant participante que se comprobará
     * @return {@code true} si alguna parte de su hitbox rebasa la cancha
     */
    public boolean isOutside(SessionParticipant participant) { return geometry.isOutside(participant); }
    /** Indica si el participante toca el techo o un límite lateral restringido.
     * @param participant participante que se comprobará
     * @return {@code true} si está en contacto con alguno de esos límites
     */
    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        return geometry.touchesForbiddenBoundary(participant);
    }
    /** Indica si el balón está en contacto con el suelo.
     * @param ball balón que se comprobará
     * @return {@code true} si toca la hitbox del suelo
     */
    public boolean touchesGround(Ball ball) { return geometry.touchesGround(ball); }
    /** Devuelve la coordenada vertical del centro del balón apoyado en el suelo.
     * @param ball balón cuya posición de apoyo se calculará
     * @return coordenada vertical del centro cuando el balón toca el suelo
     */
    public double groundCenterY(Ball ball) { return geometry.groundCenterY(ball); }
    /** Devuelve {@code left}, {@code right} o una cadena vacía según el arco que contiene el balón.
     * @param ball balón que se comprobará
     * @return identificador simple del arco que contiene su centro, o cadena vacía
     */
    public String goalSideFor(Ball ball) { return geometry.goalSideFor(ball); }
}
