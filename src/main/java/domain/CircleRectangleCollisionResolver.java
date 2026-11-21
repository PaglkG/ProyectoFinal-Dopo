package domain;

/** Resuelve el contacto exacto entre el círculo del balón y un rectángulo fijo. */
final class CircleRectangleCollisionResolver {

    private static final double EPSILON = 1e-9;

    private CircleRectangleCollisionResolver() {
    }

    /** Corrige el contacto sobre el eje clasificado y refleja solo su velocidad normal. */
    static Resolution resolve(Ball ball, RectangularHitbox rectangle,
            double previousBallX, double previousBallY,
            double previousRectangleX, double previousRectangleY, double restitution) {
        double left = Math.min(rectangle.getX(), rectangle.getX() + rectangle.getWidth());
        double right = Math.max(rectangle.getX(), rectangle.getX() + rectangle.getWidth());
        double top = Math.min(rectangle.getY(), rectangle.getY() + rectangle.getHeight());
        double bottom = Math.max(rectangle.getY(), rectangle.getY() + rectangle.getHeight());
        double closestX = Math.max(left, Math.min(ball.getX(), right));
        double closestY = Math.max(top, Math.min(ball.getY(), bottom));
        double distanceX = ball.getX() - closestX;
        double distanceY = ball.getY() - closestY;
        double distance = Math.hypot(distanceX, distanceY);
        double radius = Math.abs(ball.getRadius());
        if (distance > radius) return null;

        CollisionAxisResolver.Axis axis = CollisionAxisResolver.forCircleRectangle(
                ball, rectangle, previousBallX, previousBallY,
                previousRectangleX, previousRectangleY);
        double side = CollisionAxisResolver.sideForAxis(axis, ball, rectangle,
                previousBallX, previousBallY, previousRectangleX, previousRectangleY);
        double normalX = axis == CollisionAxisResolver.Axis.HORIZONTAL ? side : 0;
        double normalY = axis == CollisionAxisResolver.Axis.VERTICAL ? side : 0;
        double penetration = radius - distance;

        if (penetration > EPSILON) {
            double targetX = ball.getX();
            double targetY = ball.getY();
            if (axis == CollisionAxisResolver.Axis.HORIZONTAL) {
                double outsideDistanceY = ball.getY() < top ? top - ball.getY()
                        : ball.getY() > bottom ? ball.getY() - bottom : 0;
                double radialOffset = outsideDistanceY == 0 ? radius
                        : Math.sqrt(Math.max(0, radius * radius - outsideDistanceY * outsideDistanceY));
                targetX = (side < 0 ? left : right) + side * radialOffset;
            } else {
                double outsideDistanceX = ball.getX() < left ? left - ball.getX()
                        : ball.getX() > right ? ball.getX() - right : 0;
                double radialOffset = outsideDistanceX == 0 ? radius
                        : Math.sqrt(Math.max(0, radius * radius - outsideDistanceX * outsideDistanceX));
                targetY = (side < 0 ? top : bottom) + side * radialOffset;
            }
            ball.reposition(targetX, targetY);
        }

        double normalSpeed = ball.getVelocityX() * normalX + ball.getVelocityY() * normalY;
        boolean bounced = normalSpeed < 0;
        if (bounced) {
            double impulse = -(1 + restitution) * normalSpeed;
            ball.applyImpulse(impulse * normalX, impulse * normalY);
        }
        return new Resolution(axis, side, bounced);
    }

    record Resolution(CollisionAxisResolver.Axis axis, double side, boolean bounced) {
    }
}
