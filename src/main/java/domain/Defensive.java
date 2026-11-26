package domain;

/**
 * Perfil de bot defensivo.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Defensive extends BotPlayer {
    private static final long serialVersionUID = -6707101878582858648L;
    /** Crea un perfil de bot defensivo. */
    public Defensive() { }

    /** {@inheritDoc} */
    @Override
    protected BotDecisionStrategy createDecisionStrategy() {
        return new BotDecisionStrategies.DefendGoal();
    }
}
