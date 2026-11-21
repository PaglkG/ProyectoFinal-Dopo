package domain;

import domain.Ball;
import domain.SessionParticipant;
import domain.RectangularHitbox;

/** Resuelve la penetración y el rebote de balones contra hitboxes de participantes. */
public final class BallParticipantCollisionResolver {
    /** Resuelve los contactos de cada balón con cada participante. */
    public void resolveAll(Ball[] balls, Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        for (Ball ball : balls) {
            for (SessionParticipant participant : participants) {
                resolve(ball, participant, restitution, minimumLandingSpeed);
            }
        }
    }

    /** Separa el balón de la hitbox y refleja su velocidad normal cuando hay aproximación. */
    public void resolve(Ball ball, SessionParticipant participant,
            double restitution, double minimumLandingSpeed) {
        RectangularHitbox box = participant.getHitbox();
        double closestX = Math.max(box.getX(), Math.min(ball.getX(), box.getX() + box.getWidth()));
        double closestY = Math.max(box.getY(), Math.min(ball.getY(), box.getY() + box.getHeight()));
        double dx = ball.getX() - closestX;
        double dy = ball.getY() - closestY;
        double distance = Math.hypot(dx, dy);
        if (distance > ball.getRadius()) return;
        double nx;
        double ny;
        if (distance > 1e-9) {
            nx = dx / distance;
            ny = dy / distance;
        } else {
            double left = Math.abs(ball.getX() - box.getX());
            double right = Math.abs(box.getX() + box.getWidth() - ball.getX());
            double top = Math.abs(ball.getY() - box.getY());
            double bottom = Math.abs(box.getY() + box.getHeight() - ball.getY());
            double nearest = Math.min(Math.min(left, right), Math.min(top, bottom));
            nx = nearest == left ? -1 : nearest == right ? 1 : 0;
            ny = nearest == top ? -1 : nearest == bottom ? 1 : 0;
            distance = -nearest;
        }
        double penetration = ball.getRadius() - distance;
        ball.reposition(ball.getX() + nx * (penetration + 1e-6),
                ball.getY() + ny * (penetration + 1e-6));
        double normalSpeed = ball.getVelocityX() * nx + ball.getVelocityY() * ny;
        boolean bounced = normalSpeed < 0;
        if (bounced) {
            double delta = (1 + restitution) * normalSpeed;
            ball.applyImpulse(-delta * nx, -delta * ny);
        }
        if (bounced && ny < -0.7 && ball.getVelocityY() < minimumLandingSpeed) {
            ball.applyImpulse(0, minimumLandingSpeed - ball.getVelocityY());
        }
    }
}
