package domain.collision;

import domain.Ball;
import domain.SessionParticipant;

/** Resuelve contactos e impulsos entre balones y el empuje al caminar. */
public final class BallCollisionResolver {
    public void resolveAll(Ball[] balls, double restitution) {
        for (int first = 0; first < balls.length; first++) {
            for (int second = first + 1; second < balls.length; second++) {
                resolvePair(balls[first], balls[second], restitution);
            }
        }
    }

    public void resolveWalkingContact(Ball[] balls, SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants, double restitution, CourtGeometry geometry) {
        double movement = moved.getHitbox().getX() - previousX;
        if (movement == 0) return;
        int direction = movement > 0 ? 1 : -1;
        for (Ball ball : balls) {
            if (!ball.intersects(moved).isContact()) continue;
            double speed = Math.hypot(ball.getVelocityX(), ball.getVelocityY());
            if (speed > 1e-6) {
                BallParticipantCollisionResolver.resolve(ball, moved, restitution, 0.5);
                moved.reposition(previousX, moved.getHitbox().getY());
                continue;
            }
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
                    moved.reposition(previousX, moved.getHitbox().getY());
                    break;
                }
                movedDistance += step;
            }
        }
    }

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
