package domain.collision;

import domain.Ball;
import domain.SessionParticipant;
import domain.SoccerField;
import domain.hitbox.RectangularHitbox;

/** Consultas y respuestas de contacto determinadas por la geometría de cancha. */
public final class CourtGeometry {
    private final SoccerField field;

    public CourtGeometry(SoccerField field) {
        this.field = field;
    }

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

    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        RectangularHitbox[] walls = field.getHitboxField();
        double radius = ball.getRadius();
        double left = walls[2].getX() + walls[2].getWidth();
        double right = walls[3].getX() - radius;
        double top = walls[0].getY() + walls[0].getHeight() + radius;
        if (ball.getX() < left + radius) ball.bounceHorizontallyAt(left + radius, retention);
        else if (ball.getX() > right) ball.bounceHorizontallyAt(right, retention);
        if (ball.getY() < top) ball.bounceVerticallyAt(top, retention);
        for (RectangularHitbox goal : field.getGoalHitboxes()) {
            if (ball.getY() >= goal.getY() && ball.getY() <= goal.getY() + goal.getHeight()) continue;
            double closestX = Math.max(goal.getX(), Math.min(ball.getX(), goal.getX() + goal.getWidth()));
            double closestY = Math.max(goal.getY(), Math.min(ball.getY(), goal.getY() + goal.getHeight()));
            double dx = ball.getX() - closestX;
            double dy = ball.getY() - closestY;
            if (Math.hypot(dx, dy) > radius) continue;
            if (Math.abs(dx) >= Math.abs(dy)) {
                ball.bounceHorizontallyAt(closestX + (dx < 0 ? -1 : 1) * radius, retention);
            } else {
                ball.bounceVerticallyAt(closestY + (dy < 0 ? -1 : 1) * radius, retention);
            }
        }
    }

    public void bounceBallAtGround(Ball ball, double retention, double minimumSpeed) {
        if (touchesGround(ball)) ball.bounceAt(groundCenterY(ball), retention, minimumSpeed);
    }

    public boolean isOutside(Ball ball) {
        double radius = ball.getRadius();
        return ball.getX() - radius < field.courtX() || ball.getX() + radius > field.courtX() + field.courtWidth()
                || ball.getY() - radius < field.courtY() || ball.getY() + radius > field.courtY() + field.courtHeight();
    }

    public boolean isOutside(SessionParticipant participant) {
        var box = participant.getHitbox();
        return box.getX() < field.courtX() || box.getX() + box.getWidth() > field.courtX() + field.courtWidth()
                || box.getY() < field.courtY() || box.getY() + box.getHeight() > field.courtY() + field.courtHeight();
    }

    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        RectangularHitbox[] walls = field.getHitboxField();
        return walls[0].intersects(participant).isContact()
                || walls[2].intersects(participant).isContact()
                || walls[3].intersects(participant).isContact();
    }

    public boolean touchesGround(Ball ball) {
        return field.getHitboxField()[1].intersects(ball).isContact();
    }

    public double groundCenterY(Ball ball) { return field.groundY() - ball.getRadius(); }

    public String goalSideFor(Ball ball) {
        RectangularHitbox[] goals = field.getGoalHitboxes();
        if (containsCenter(goals[0], ball)) return "left";
        if (containsCenter(goals[1], ball)) return "right";
        return "";
    }

    private boolean containsCenter(RectangularHitbox goal, Ball ball) {
        return ball.getX() >= goal.getX() && ball.getX() <= goal.getX() + goal.getWidth()
                && ball.getY() >= goal.getY() && ball.getY() <= goal.getY() + goal.getHeight();
    }
}
