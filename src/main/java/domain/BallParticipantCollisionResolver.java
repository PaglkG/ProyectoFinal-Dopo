package domain;

/** Corrige la penetración y aplica el rebote de los balones contra los participantes. */
public final class BallParticipantCollisionResolver {
    private static final double HEAD_ROLL_SPEED = 4.0;
    private static final double SUPPORT_TOLERANCE = 1e-9;

    /** Crea el resolutor de contactos entre balones y participantes. */
    public BallParticipantCollisionResolver() { }

    /** Traslada el apoyo del paso anterior y rueda el balón hacia el borde más cercano. */
    void rollSupportedBalls(Ball[] balls, Iterable<SessionParticipant> participants,
            double stepSeconds, double gravity, CourtGeometry geometry) {
        double gravityStep = gravity * stepSeconds;
        for (Ball ball : balls) {
            if (Math.abs(ball.getVelocityY() - gravityStep) > SUPPORT_TOLERANCE
                    || Math.abs(ball.getVelocityX()) > HEAD_ROLL_SPEED + SUPPORT_TOLERANCE) continue;
            for (SessionParticipant participant : participants) {
                RectangularHitbox box = participant.getHitbox();
                double previousLeft = participant.getPreviousX();
                double width = box.getWidth();
                if (!touchesHead(ball.getPreviousX(), ball.getPreviousY(), ball.getRadius(),
                        previousLeft, participant.getPreviousY(), width)) continue;
                double relativeX = ball.getPreviousX() - previousLeft;
                double speed = relativeX < width / 2 ? -HEAD_ROLL_SPEED : HEAD_ROLL_SPEED;
                double offset = relativeX + speed * stepSeconds;
                double freeX = ball.getX();
                double freeY = ball.getY();
                ball.reposition(box.getX() + offset, box.getY() - ball.getRadius());
                if (!geometry.isValidBallPosition(ball, participant, participants)) {
                    ball.reposition(freeX, freeY);
                    continue;
                }
                double verticalImpulse = offset >= 0 && offset <= width ? -ball.getVelocityY() : 0;
                ball.applyImpulse(speed - ball.getVelocityX(), verticalImpulse);
                break;
            }
        }
    }

    /** Reconoce el apoyo horizontal previo sin confundirlo con el contacto de un costado. */
    static boolean restsOnHead(Ball ball, double left, double top, double width) {
        return ball.getVelocityY() == 0 && Math.abs(ball.getVelocityX()) <= HEAD_ROLL_SPEED + SUPPORT_TOLERANCE
                && touchesHead(ball.getX(), ball.getY(), ball.getRadius(), left, top, width);
    }

    /** Comprueba que el centro quede sobre el segmento superior y el círculo sea tangente. */
    private static boolean touchesHead(double x, double y, double radius, double left, double top,
            double width) {
        return x >= left && x <= left + width && Math.abs(y + radius - top) <= SUPPORT_TOLERANCE;
    }

    /**
     * Recupera el primer contacto cruzado entre dos posiciones, antes de que
     * se pueda registrar un gol con el balón al otro lado del participante.
     * La resolución habitual aplica después la respuesta física.
     * @param balls balones que avanzaron en el paso
     * @param participants participantes que pueden interceptar los balones
     */
    public void rewindMissedContacts(Ball[] balls, Iterable<SessionParticipant> participants) {
        for (Ball ball : balls) {
            double endX = ball.getX();
            double endY = ball.getY();
            double radius = Math.abs(ball.getRadius());
            double first = Double.POSITIVE_INFINITY;
            double hitX = endX;
            double hitY = endY;
            for (SessionParticipant participant : participants) {
                RectangularHitbox box = participant.getHitbox();
                // Expresa la posición inicial en el mismo sistema que la caja actual.
                double startX = ball.getPreviousX() + box.getX() - participant.getPreviousX();
                double startY = ball.getPreviousY() + box.getY() - participant.getPreviousY();
                if (CollisionDetector.intersects(ball.getHitbox(), box)
                        && !passedCenter(startX, endX, box.getX(), box.getWidth(), radius)
                        && !passedCenter(startY, endY, box.getY(), box.getHeight(), radius)) continue;

                double dx = endX - startX;
                double dy = endY - startY;
                double entry = CircleRectangleCollisionResolver.firstContactFraction(
                        box, radius, startX, startY, dx, dy);
                if (entry < first) {
                    first = entry;
                    hitX = startX + entry * dx;
                    hitY = startY + entry * dy;
                }
            }
            if (Double.isFinite(first)) ball.reposition(hitX, hitY);
        }
    }

    /** Detecta cuándo el final del recorrido quedó del lado contrario al de entrada. */
    private boolean passedCenter(double start, double end, double edge, double size, double radius) {
        double center = edge + size / 2;
        return (start <= edge - radius && end >= center)
                || (start >= edge + size + radius && end <= center);
    }

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
                resolve(ball, participant, restitution, minimumLandingSpeed);
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

    /** Usa las posiciones anteriores de ambas entidades para elegir el eje del rebote. */
    void resolve(Ball ball, SessionParticipant participant,
            double previousBallX, double previousBallY,
            double previousParticipantX, double previousParticipantY,
            double restitution, double minimumLandingSpeed) {
        RectangularHitbox box = participant.getHitbox();
        CircleRectangleCollisionResolver.Resolution result = CircleRectangleCollisionResolver.resolve(
                ball, box, previousBallX, previousBallY,
                previousParticipantX, previousParticipantY, restitution);
        if (result == null || result.isHorizontal()) return;

        if (result.side() > 0 && box.getY() > previousParticipantY
                && ball.getVelocityY() < minimumLandingSpeed) {
            ball.applyImpulse(0, minimumLandingSpeed - ball.getVelocityY());
        } else if (result.side() < 0 && result.bounced() && Math.abs(ball.getVelocityY()) < minimumLandingSpeed
                && Math.abs(ball.getVelocityX()) <= HEAD_ROLL_SPEED + SUPPORT_TOLERANCE
                && touchesHead(ball.getX(), ball.getY(), ball.getRadius(), box.getX(), box.getY(), box.getWidth())) {
            ball.applyImpulse(0, -ball.getVelocityY());
        }
    }
}
