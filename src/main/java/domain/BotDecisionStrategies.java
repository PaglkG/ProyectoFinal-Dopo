package domain;

import java.util.concurrent.ThreadLocalRandom;

/** Agrupa las políticas de los cuatro perfiles incluidos en el juego. */
final class BotDecisionStrategies {
    /** Las políticas se instancian por participante. */
    private BotDecisionStrategies() { }

    /** Elige un movimiento al azar y lo conserva durante varios pasos de juego. */
    static final class RandomMovement implements BotDecisionStrategy {
        private static final long serialVersionUID = 1L;
        private static final int DECISION_STEPS = 18;
        private int remainingSteps;
        private String action = "";

        /** {@inheritDoc} */
        @Override
        public String chooseAction(BotDecisionContext context) {
            if (remainingSteps == 0) {
                action = switch (ThreadLocalRandom.current().nextInt(3)) {
                    case 0 -> "moveLeft";
                    case 1 -> "moveRight";
                    default -> "";
                };
                remainingSteps = DECISION_STEPS;
            }
            remainingSteps--;
            return action;
        }
    }

    /** Busca el balón más cercano y solicita una patada cuando está al alcance. */
    static final class Attack implements BotDecisionStrategy {
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public String chooseAction(BotDecisionContext context) {
            if (!context.hasBall()) return "";
            if (context.ballInReach()) return "kick";
            return context.moveToward(context.ballX());
        }
    }

    /** Se coloca entre el balón y el arco propio y solicita una patada al alcance. */
    static final class DefendGoal implements BotDecisionStrategy {
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public String chooseAction(BotDecisionContext context) {
            if (!context.hasBall()) return context.moveToward(context.ownGoalFrontX());
            if (context.ballInReach()) return "kick";
            double target = (context.ownGoalFrontX() + context.ballX()) / 2;
            return context.moveToward(target);
        }
    }

    /** Reutiliza la defensa con ventaja y el ataque en empate o desventaja. */
    static final class AdaptToScore implements BotDecisionStrategy {
        private static final long serialVersionUID = 1L;
        private final Attack attack = new Attack();
        private final DefendGoal defense = new DefendGoal();

        /** {@inheritDoc} */
        @Override
        public String chooseAction(BotDecisionContext context) {
            return context.goalDifference() > 0
                    ? defense.chooseAction(context) : attack.chooseAction(context);
        }
    }
}
