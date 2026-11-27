package domain;

import domain.Ball;
import domain.SessionParticipant;

/** Resuelve contactos e impulsos entre balones y el empuje al caminar. */
public final class BallCollisionResolver {
    /** Crea el resolutor de contactos entre balones. */
    public BallCollisionResolver() { }

    /** Resuelve los contactos entre cada par de balones recibidos.
     * @param balls balones cuyo contacto por pares se evaluará
     * @param restitution fracción de velocidad normal conservada en el rebote
     */
    public void resolveAll(Ball[] balls, double restitution) {
        for (int first = 0; first < balls.length; first++) {
            for (int second = first + 1; second < balls.length; second++) {
                if (balls[first].isIllusionDecoy() || balls[second].isIllusionDecoy()) continue;
                resolvePair(balls[first], balls[second], restitution);
            }
        }
    }

    /** Empuja en pasos pequeños cualquier balón en contacto con el participante que caminó.
     * @param balls balones susceptibles de ser empujados
     * @param moved participante que terminó de caminar
     * @param previousX coordenada horizontal del participante antes del movimiento
     * @param participants participantes que bloquean el desplazamiento de los balones
     * @param geometry validador de posiciones dentro de la cancha
     */
    public void resolveWalkingContact(Ball[] balls, SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants, CourtGeometry geometry) {
        double movement = moved.getHitbox().getX() - previousX;
        if (movement == 0) return;
        int direction = movement > 0 ? 1 : -1;
        for (Ball ball : balls) {
            if (ball.isIllusionDecoy()) continue;
            if (!ball.intersects(moved).isContact()) continue;
            RectangularHitbox box = moved.getHitbox();
            if (BallParticipantCollisionResolver.restsOnHead(ball, previousX, box.getY(), box.getWidth())) continue;
            double halfWidth = box.getWidth() / 2;
            double previousOverlap = ball.getRadius() + halfWidth
                    - Math.abs(ball.getX() - (previousX + halfWidth));
            double currentOverlap = ball.getRadius() + halfWidth
                    - Math.abs(ball.getX() - (moved.getHitbox().getX() + halfWidth));
            if (currentOverlap <= previousOverlap + 1e-9) continue;

            double overlap = direction > 0
                    ? moved.getHitbox().getX() + moved.getHitbox().getWidth() + ball.getRadius() - ball.getX()
                    : ball.getX() + ball.getRadius() - moved.getHitbox().getX();
            double distance = Math.max(0, overlap) + 1e-6;
            double originalX = ball.getX();
            double movedDistance = 0;
            while (movedDistance + 1e-9 < distance) {
                double step = Math.min(0.1, distance - movedDistance);
                ball.reposition(ball.getX() + direction * step, ball.getY());
                if (!geometry.isValidBallPosition(ball, moved, participants)) {
                    ball.reposition(originalX + direction * movedDistance, ball.getY());
                    moved.repositionForCollision(previousX, moved.getHitbox().getY());
                    break;
                }
                movedDistance += step;
            }
        }
    }

    /** Corrige la penetración del par y aplica impulso normal si se aproximan. */
    private void resolvePair(Ball first, Ball second, double restitution) {
        double dx = second.getX() - first.getX();
        double dy = second.getY() - first.getY();
        double distance = Math.hypot(dx, dy);
        double minDistance = first.getRadius() + second.getRadius();
        if (distance > minDistance) return;
        double nx = distance == 0 ? 1 : dx / distance;
        double ny = distance == 0 ? 0 : dy / distance;
        double overlap = minDistance - distance;
        first.reposition(first.getX() - nx * overlap / 2, first.getY() - ny * overlap / 2);
        second.reposition(second.getX() + nx * overlap / 2, second.getY() + ny * overlap / 2);
        double relativeNormal = (second.getVelocityX() - first.getVelocityX()) * nx
                + (second.getVelocityY() - first.getVelocityY()) * ny;
        if (relativeNormal >= 0) return;
        double impulse = -(1 + restitution) * relativeNormal / 2;
        first.applyImpulse(-impulse * nx, -impulse * ny);
        second.applyImpulse(impulse * nx, impulse * ny);
    }
}
