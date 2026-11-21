package domain;

/** Selecciona el eje de resolución comparando solapamientos actuales y previos. */
final class CollisionAxisResolver {

    enum Axis { HORIZONTAL, VERTICAL }

    private static final double EPSILON = 1e-9;

    private CollisionAxisResolver() {
    }

    /**
     * Clasifica un contacto círculo-rectángulo usando la proyección AABB del
     * círculo solo para determinar el eje; la detección geométrica sigue usando
     * el círculo real.
     */
    static Axis forCircleRectangle(Ball ball, RectangularHitbox rectangle,
            double previousBallX, double previousBallY,
            double previousRectangleX, double previousRectangleY) {
        double halfWidth = Math.abs(rectangle.getWidth()) / 2;
        double halfHeight = Math.abs(rectangle.getHeight()) / 2;
        double radius = Math.abs(ball.getRadius());
        double rectangleCenterX = rectangle.getX() + rectangle.getWidth() / 2;
        double rectangleCenterY = rectangle.getY() + rectangle.getHeight() / 2;
        double previousRectangleCenterX = previousRectangleX + rectangle.getWidth() / 2;
        double previousRectangleCenterY = previousRectangleY + rectangle.getHeight() / 2;

        double overlapX = radius + halfWidth - Math.abs(ball.getX() - rectangleCenterX);
        double overlapY = radius + halfHeight - Math.abs(ball.getY() - rectangleCenterY);
        double previousOverlapX = radius + halfWidth - Math.abs(previousBallX - previousRectangleCenterX);
        double previousOverlapY = radius + halfHeight - Math.abs(previousBallY - previousRectangleCenterY);

        boolean wasSeparatedX = previousOverlapX <= 0;
        boolean wasSeparatedY = previousOverlapY <= 0;
        boolean wasOverlappingX = previousOverlapX > 0;
        boolean wasOverlappingY = previousOverlapY > 0;

        if (wasSeparatedX && wasOverlappingY) return Axis.HORIZONTAL;
        if (wasSeparatedY && wasOverlappingX) return Axis.VERTICAL;

        if (overlapX + EPSILON < overlapY) return Axis.HORIZONTAL;
        if (overlapY + EPSILON < overlapX) return Axis.VERTICAL;
        return Math.abs(ball.getVelocityX()) > Math.abs(ball.getVelocityY())
                ? Axis.HORIZONTAL : Axis.VERTICAL;
    }

    /** Devuelve -1 o 1 según el lado previo/actual del círculo respecto al rectángulo. */
    static double sideForAxis(CollisionAxisResolver.Axis axis, Ball ball,
            RectangularHitbox rectangle, double previousBallX, double previousBallY,
            double previousRectangleX, double previousRectangleY) {
        if (axis == Axis.HORIZONTAL) {
            double currentDifference = ball.getX() - (rectangle.getX() + rectangle.getWidth() / 2);
            if (Math.abs(currentDifference) > EPSILON) return Math.signum(currentDifference);
            double previousDifference = previousBallX - (previousRectangleX + rectangle.getWidth() / 2);
            if (Math.abs(previousDifference) > EPSILON) return Math.signum(previousDifference);
            return ball.getVelocityX() >= 0 ? -1 : 1;
        }

        double currentDifference = ball.getY() - (rectangle.getY() + rectangle.getHeight() / 2);
        if (Math.abs(currentDifference) > EPSILON) return Math.signum(currentDifference);
        double previousDifference = previousBallY - (previousRectangleY + rectangle.getHeight() / 2);
        if (Math.abs(previousDifference) > EPSILON) return Math.signum(previousDifference);
        return ball.getVelocityY() >= 0 ? -1 : 1;
    }
}
