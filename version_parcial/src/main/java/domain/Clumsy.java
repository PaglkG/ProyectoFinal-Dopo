package domain;

/**
 * Perfil de bot descuidado.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Clumsy extends BotPlayer {
    private static final long serialVersionUID = -2295575088660928418L;
    /** Crea un perfil de bot descuidado. */
    public Clumsy() { }

    /** {@inheritDoc} */
    @Override
    public BotDecisionStrategy createDecisionStrategy() {
        return new BotDecisionStrategies.RandomMovement();
    }
}
