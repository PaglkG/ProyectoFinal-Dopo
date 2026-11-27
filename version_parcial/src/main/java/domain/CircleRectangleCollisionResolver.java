package domain;

/** Clasifica y resuelve el contacto exacto entre el balón y una hitbox rectangular. */
final class CircleRectangleCollisionResolver {

    private static final double EPSILON = 1e-9;

    private CircleRectangleCollisionResolver() {
    }

    /** Resuelve el contacto con un límite o marco que conserva su posición entre pasos. */
    static Resolution resolveAgainstFixedRectangle(Ball ball, RectangularHitbox rectangle,
            double restitution) {
        return resolve(ball, rectangle, ball.getPreviousX(), ball.getPreviousY(),
                rectangle.getX(), rectangle.getY(), restitution);
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
        if (distance > radius + EPSILON) return null;

        boolean horizontal = isHorizontalContact(
                ball, rectangle, previousBallX, previousBallY,
                previousRectangleX, previousRectangleY);
        double side = sideForAxis(horizontal, ball, rectangle,
                previousBallX, previousBallY, previousRectangleX, previousRectangleY);
        double normalX = horizontal ? side : 0;
        double normalY = horizontal ? 0 : side;
        double penetration = radius - distance;

        if (penetration > EPSILON) {
            double targetX = ball.getX();
            double targetY = ball.getY();
            if (horizontal) {
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
        return new Resolution(horizontal, side, bounced);
    }

    /**
     * Devuelve la primera fracción del recorrido que toca el rectángulo, o infinito.
     * La unión de dos franjas y los cuatro discos de las esquinas representa
     * exactamente el espacio alcanzable por el centro del círculo.
     */
    static double firstContactFraction(RectangularHitbox box, double radius,
            double x, double y, double dx, double dy) {
        double left = box.getX();
        double right = left + box.getWidth();
        double top = box.getY();
        double bottom = top + box.getHeight();
        double endX = x + dx;
        double endY = y + dy;
        if (Math.max(x, endX) < left - radius || Math.min(x, endX) > right + radius
                || Math.max(y, endY) < top - radius || Math.min(y, endY) > bottom + radius) {
            return Double.POSITIVE_INFINITY;
        }

        double distance = distanceToBox(x, y, left, right, top, bottom);
        if (distance <= radius + EPSILON) {
            // Un balón que ya se separa desde el contacto debe conservar su avance.
            double probe = Math.min(1, 1e-5 / Math.max(Math.hypot(dx, dy), EPSILON));
            double after = distanceToBox(x + dx * probe, y + dy * probe, left, right, top, bottom);
            return after < distance - EPSILON ? 0 : Double.POSITIVE_INFINITY;
        }

        double first = Math.min(
                enterBox(x, y, dx, dy, left - radius, right + radius, top, bottom),
                enterBox(x, y, dx, dy, left, right, top - radius, bottom + radius));
        first = Math.min(first, enterDisk(x - left, y - top, dx, dy, radius));
        first = Math.min(first, enterDisk(x - right, y - top, dx, dy, radius));
        first = Math.min(first, enterDisk(x - left, y - bottom, dx, dy, radius));
        return Math.min(first, enterDisk(x - right, y - bottom, dx, dy, radius));
    }

    /** Mide la distancia exacta desde un centro hasta la superficie del rectángulo. */
    private static double distanceToBox(double x, double y,
            double left, double right, double top, double bottom) {
        return Math.hypot(x - Math.clamp(x, left, right), y - Math.clamp(y, top, bottom));
    }

    /** Interseca los intervalos de tiempo de los dos ejes de la franja. */
    private static double enterBox(double x, double y, double dx, double dy,
            double left, double right, double top, double bottom) {
        if ((dx == 0 && (x < left || x > right)) || (dy == 0 && (y < top || y > bottom))) {
            return Double.POSITIVE_INFINITY;
        }
        double first = 0;
        double last = 1;
        if (dx != 0) {
            double a = (left - x) / dx;
            double b = (right - x) / dx;
            first = Math.max(first, Math.min(a, b));
            last = Math.min(last, Math.max(a, b));
        }
        if (dy != 0) {
            double a = (top - y) / dy;
            double b = (bottom - y) / dy;
            first = Math.max(first, Math.min(a, b));
            last = Math.min(last, Math.max(a, b));
        }
        return first <= last ? first : Double.POSITIVE_INFINITY;
    }

    /** Calcula el primer corte del recorrido con un disco de esquina. */
    private static double enterDisk(double x, double y, double dx, double dy, double radius) {
        double speedSquared = dx * dx + dy * dy;
        if (speedSquared == 0) return Double.POSITIVE_INFINITY;
        double cross = x * dy - y * dx;
        double discriminant = speedSquared * radius * radius - cross * cross;
        if (discriminant < 0) return Double.POSITIVE_INFINITY;
        double first = (-x * dx - y * dy - Math.sqrt(discriminant)) / speedSquared;
        return first >= 0 && first <= 1 ? first : Double.POSITIVE_INFINITY;
    }

    /**
     * Clasifica un contacto círculo-rectángulo usando la proyección AABB del
     * círculo solo para determinar el eje; la detección geométrica sigue usando
     * el círculo real.
     */
    private static boolean isHorizontalContact(Ball ball, RectangularHitbox rectangle,
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

        if (wasSeparatedX && wasOverlappingY) return true;
        if (wasSeparatedY && wasOverlappingX) return false;

        if (overlapX + EPSILON < overlapY) return true;
        if (overlapY + EPSILON < overlapX) return false;
        return Math.abs(ball.getVelocityX()) > Math.abs(ball.getVelocityY());
    }

    /** Devuelve -1 o 1 según el lado previo/actual del círculo respecto al rectángulo.
     * @param horizontal {@code true} si el contacto se clasifica como horizontal
     */
    private static double sideForAxis(boolean horizontal, Ball ball,
            RectangularHitbox rectangle, double previousBallX, double previousBallY,
            double previousRectangleX, double previousRectangleY) {
        if (horizontal) {
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

    /** Resultado inmutable de la clasificación y respuesta de un contacto. */
    static final class Resolution {
        private final boolean horizontal;
        private final double side;
        private final boolean bounced;

        /** Crea el resultado de la resolución del contacto.
         * @param horizontal {@code true} si se resolvió horizontalmente
         * @param side lado del rectángulo respecto al que se separó el balón
         * @param bounced indica si se aplicó un rebote
         */
        Resolution(boolean horizontal, double side, boolean bounced) {
            this.horizontal = horizontal;
            this.side = side;
            this.bounced = bounced;
        }

        /** Indica si el contacto se resolvió horizontalmente.
         * @return {@code true} si fue horizontal
         */
        boolean isHorizontal() { return horizontal; }

        /** Devuelve el lado elegido para la separación.
         * @return lado con valor -1 o 1
         */
        double side() { return side; }

        /** Indica si el contacto produjo un rebote.
         * @return {@code true} si se aplicó un impulso de rebote
         */
        boolean bounced() { return bounced; }
    }
}
