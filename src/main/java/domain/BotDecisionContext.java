package domain;

/** Observación de un paso de juego que no permite modificar la cancha ni sus entidades. */
final class BotDecisionContext {
    private final double playerX;
    private final double ownGoalFrontX;
    private final double halfStep;
    private final double ballX;
    private final boolean hasBall;
    private final boolean ballInReach;
    /** Indica si un salto vertical habilitaría una patada a un balón alto. */
    private final boolean elevatedBallInReach;
    private final int goalDifference;

    /** Captura las coordenadas pertinentes, el alcance y la diferencia de goles.
     * El bot de PlayerVsBot protege el arco derecho.
     * @param bot participante que observa la jugada
     * @param field cancha de la partida
     * @param goalDifference goles a favor menos goles en contra
     * @param supportY altura del suelo o del participante que sostiene al bot
     */
    BotDecisionContext(BotPlayer bot, SoccerField field, int goalDifference, double supportY) {
        RectangularHitbox hitbox = bot.getHitbox();
        playerX = hitbox.getX() + hitbox.getWidth() / 2;
        double playerY = hitbox.getY() + hitbox.getHeight() / 2;
        RectangularHitbox ownGoal = field.getGoalHitboxes()[1];
        ownGoalFrontX = ownGoal.getX();
        halfStep = bot.horizontalStep() / 2;
        this.goalDifference = goalDifference;

        double closestDistance = Double.POSITIVE_INFINITY;
        double closestX = playerX;
        boolean reachable = false;
        boolean reachableAfterJump = false;
        double ceilingY = field.courtY() + field.boundaryThickness();
        RectangularHitbox jumpReach = null;
        if (bot.canStartJump(supportY, ceilingY)) {
            double jumpRise = SessionParticipant.maximumJumpRise(GameSession.STEP_SECONDS);
            double highestY = Math.max(Math.nextUp(ceilingY), hitbox.getY() - jumpRise);
            jumpReach = new RectangularHitbox(hitbox.getX(), highestY, hitbox.getWidth(),
                    hitbox.getY() + hitbox.getHeight() - highestY);
        }
        for (Ball ball : field.balls()) {
            double distance = Math.hypot(ball.getX() - playerX, ball.getY() - playerY);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestX = ball.getX();
            }
            reachable |= GameSession.isBallInKickReach(hitbox, ball);
            reachableAfterJump |= jumpReach != null && ball.getY() < hitbox.getY()
                    && GameSession.isBallInKickReach(jumpReach, ball);
        }
        ballX = closestX;
        hasBall = closestDistance < Double.POSITIVE_INFINITY;
        ballInReach = reachable;
        elevatedBallInReach = reachableAfterJump;
    }

    /** Indica si hay al menos un balón que observar. */
    boolean hasBall() { return hasBall; }

    /** Devuelve la coordenada horizontal del balón más cercano. */
    double ballX() { return ballX; }

    /** Devuelve la coordenada horizontal de la entrada del arco propio. */
    double ownGoalFrontX() { return ownGoalFrontX; }

    /** Indica si algún balón está en el alcance de patada del bot. */
    boolean ballInReach() { return ballInReach; }

    /** Prioriza el salto si hay un balón alto alcanzable y ninguno está al alcance actual. */
    boolean shouldJumpForBall() { return elevatedBallInReach && !ballInReach; }

    /** Devuelve los goles a favor menos los goles en contra del bot. */
    int goalDifference() { return goalDifference; }

    /** Elige dirección hacia el objetivo o espera si un paso no lo acerca. */
    String moveToward(double targetX) {
        double distance = targetX - playerX;
        if (Math.abs(distance) <= halfStep) return "";
        return distance > 0 ? "moveRight" : "moveLeft";
    }
}
