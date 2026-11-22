package domain;

import domain.Ball;
import domain.SessionParticipant;
import domain.SoccerField;
import domain.RectangularHitbox;

/** Consultas y respuestas de contacto determinadas por la geometría de cancha. */
public final class CourtGeometry {
    private final SoccerField field;

    /** Crea el evaluador geométrico asociado a {@code field}.
     * @param field cancha que contiene los límites y los arcos
     */
    public CourtGeometry(SoccerField field) {
        this.field = field;
    }

    /** Indica si la posición del balón cabe en la cancha sin solaparse con otras entidades.
     * @param candidate balón y posición que se validan
     * @param moved participante que está desplazando el balón
     * @param participants participantes que pueden bloquear esa posición
     * @return {@code true} si el balón cabe dentro de los límites y no se solapa
     */
    public boolean isValidBallPosition(Ball candidate, SessionParticipant moved,
            Iterable<SessionParticipant> participants) {
        double radius = candidate.getRadius();
        if (candidate.getX() - radius < field.courtX() + field.boundaryThickness()
                || candidate.getX() + radius > field.courtX() + field.courtWidth() - field.boundaryThickness()
                || candidate.getY() - radius < field.courtY() + field.boundaryThickness()
                || candidate.getY() + radius > field.groundY()) return false;
        for (Ball other : field.balls()) {
            if (other != candidate && candidate.intersects(other).isContact()) return false;
        }
        for (SessionParticipant participant : participants) {
            if (participant != moved && candidate.intersects(participant).isContact()) return false;
        }
        return true;
    }

    /** Corrige salidas del área jugable y aplica el rebote correspondiente hacia dentro.
     * @param ball balón que se mantendrá dentro de la cancha
     * @param retention fracción de velocidad que se conserva tras el rebote
     */
    public void keepBallInsideCourt(Ball ball, double retention) {
        double radius = ball.getRadius();
        double left = field.courtX() + field.boundaryThickness() + radius;
        double right = field.courtX() + field.courtWidth() - field.boundaryThickness() - radius;
        double top = field.courtY() + field.boundaryThickness() + radius;
        double bottom = field.groundY() - radius;
        if (ball.getX() < left) ball.bounceHorizontallyAt(left, ball.getVelocityX() < 0 ? retention : 1);
        if (ball.getX() > right) ball.bounceHorizontallyAt(right, ball.getVelocityX() > 0 ? retention : 1);
        if (ball.getY() < top) ball.bounceVerticallyAt(top, ball.getVelocityY() < 0 ? retention : 1);
        if (ball.getY() > bottom) ball.bounceVerticallyAt(bottom, ball.getVelocityY() > 0 ? retention : 1);
    }

    /** Resuelve los rebotes contra paredes y marcos de arco.
     * @param ball balón cuyo contacto se resolverá
     * @param retention fracción de velocidad normal conservada tras el rebote
     */
    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        RectangularHitbox[] walls = field.getHitboxField();
        for (int index : new int[] {0, 2, 3}) {
            RectangularHitbox wall = walls[index];
            CircleRectangleCollisionResolver.resolve(ball, wall,
                    ball.getPreviousX(), ball.getPreviousY(), wall.getX(), wall.getY(), retention);
        }
        for (RectangularHitbox goal : field.getGoalHitboxes()) {
            // La franja vertical del arco es una boca abierta para permitir el gol.
            if (ball.getY() >= goal.getY() && ball.getY() <= goal.getY() + goal.getHeight()) continue;
            CircleRectangleCollisionResolver.resolve(ball, goal,
                    ball.getPreviousX(), ball.getPreviousY(), goal.getX(), goal.getY(), retention);
        }
    }

    /** Rebota el balón al tocar el suelo y aplica una velocidad mínima si corresponde.
     * @param ball balón cuyo contacto con el suelo se resolverá
     * @param retention fracción de velocidad vertical conservada tras el rebote
     * @param minimumSpeed velocidad mínima para mantener el rebote
     */
    public void bounceBallAtGround(Ball ball, double retention, double minimumSpeed) {
        RectangularHitbox ground = field.getHitboxField()[1];
        CircleRectangleCollisionResolver.Resolution result = CircleRectangleCollisionResolver.resolve(
                ball, ground, ball.getPreviousX(), ball.getPreviousY(),
                ground.getX(), ground.getY(), retention);
        if (result != null && result.axis() == CollisionAxisResolver.Axis.VERTICAL
                && result.side() < 0 && result.bounced()
                && Math.abs(ball.getVelocityY()) < minimumSpeed) {
            ball.applyImpulse(0, -ball.getVelocityY());
        }
    }

    /** Indica si alguna parte del balón rebasa el rectángulo exterior de cancha.
     * @param ball balón que se comprobará
     * @return {@code true} si el balón excede alguno de los cuatro bordes exteriores
     */
    public boolean isOutside(Ball ball) {
        double radius = ball.getRadius();
        return ball.getX() - radius < field.courtX() || ball.getX() + radius > field.courtX() + field.courtWidth()
                || ball.getY() - radius < field.courtY() || ball.getY() + radius > field.courtY() + field.courtHeight();
    }

    /** Indica si la hitbox del participante rebasa el rectángulo exterior de cancha.
     * @param participant participante que se comprobará
     * @return {@code true} si alguna parte de su hitbox excede la cancha
     */
    public boolean isOutside(SessionParticipant participant) {
        var box = participant.getHitbox();
        return box.getX() < field.courtX() || box.getX() + box.getWidth() > field.courtX() + field.courtWidth()
                || box.getY() < field.courtY() || box.getY() + box.getHeight() > field.courtY() + field.courtHeight();
    }

    /** Indica si el participante toca el techo o uno de los límites laterales.
     * @param participant participante que se comprobará
     * @return {@code true} si toca un límite prohibido
     */
    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        RectangularHitbox[] walls = field.getHitboxField();
        return walls[0].intersects(participant).isContact()
                || walls[2].intersects(participant).isContact()
                || walls[3].intersects(participant).isContact();
    }

    /** Indica si el balón toca la hitbox del suelo.
     * @param ball balón que se comprobará
     * @return {@code true} si ambas hitboxes están en contacto
     */
    public boolean touchesGround(Ball ball) {
        return field.getHitboxField()[1].intersects(ball).isContact();
    }

    /** Calcula la coordenada vertical del centro del balón cuando descansa en el suelo.
     * @param ball balón cuya altura de apoyo se calculará
     * @return coordenada vertical del centro cuando el borde inferior toca el suelo
     */
    public double groundCenterY(Ball ball) { return field.groundY() - ball.getRadius(); }

    /** Detecta qué arco contiene el centro del balón; devuelve cadena vacía si ninguno lo contiene.
     * @param ball balón que se comprobará
     * @return {@code left}, {@code right} o cadena vacía si el centro no está en un arco
     */
    public String goalSideFor(Ball ball) {
        RectangularHitbox[] goals = field.getGoalHitboxes();
        if (containsCenter(goals[0], ball)) return "left";
        if (containsCenter(goals[1], ball)) return "right";
        return "";
    }

    /** Comprueba si el centro del balón está dentro del rectángulo del arco. */
    private boolean containsCenter(RectangularHitbox goal, Ball ball) {
        return ball.getX() >= goal.getX() && ball.getX() <= goal.getX() + goal.getWidth()
                && ball.getY() >= goal.getY() && ball.getY() <= goal.getY() + goal.getHeight();
    }
}
