package domain;

/**
 * Perfil de bot ofensivo.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Offensive extends BotPlayer {
    private static final long serialVersionUID = -8264711302025151300L;
    /** Crea un perfil de bot ofensivo. */
    public Offensive() { }

    /** {@inheritDoc} */
    @Override
    protected BotDecisionStrategy createDecisionStrategy() {
        return new BotDecisionStrategies.Attack();
    }
}
