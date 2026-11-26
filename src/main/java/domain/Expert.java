package domain;

/**
 * Perfil de bot experto.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Expert extends BotPlayer {
    private static final long serialVersionUID = 7973562913108852002L;
    /** Crea el perfil de bot experto. */
    public Expert() { }

    /** {@inheritDoc} */
    @Override
    protected BotDecisionStrategy createDecisionStrategy() {
        return new BotDecisionStrategies.AdaptToScore();
    }
}
