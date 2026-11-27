package domain;

import java.util.Set;

/**
 * Define el comportamiento común de los perfiles controlados por la máquina.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class BotPlayer extends SessionParticipant {

    private static final long serialVersionUID = 6537174384334549052L;
    /** Estrategia creada al tomar la primera decisión del bot. */
    private BotDecisionStrategy decisionStrategy;

    /** Crea un participante cuyo perfil se inicia al decidir por primera vez. */
    public BotPlayer() {
    }

    /**
     * Decide con la cancha actual y un marcador neutral mediante el flujo común
     * de observación y selección. La sesión aporta el marcador al jugar.
     * @param field cancha actual
     * @return conjunto inmutable con una acción propuesta, o vacío si espera
     */
    public final Set<String> decideAction(SoccerField field) {
        return field == null ? Set.of() : decideAction(field, 0, field.groundY());
    }

    /** Observa la cancha, prioriza un salto a un balón alto alcanzable y delega las demás decisiones.
     * @param field cancha actual
     * @param goalDifference goles a favor menos goles en contra
     * @param supportY altura del apoyo que observó la sesión
     * @return acción propuesta o conjunto vacío si espera
     */
    final Set<String> decideAction(SoccerField field, int goalDifference, double supportY) {
        if (field == null || blocksMovement()) return Set.of();
        if (decisionStrategy == null) decisionStrategy = createDecisionStrategy();
        BotDecisionContext context = new BotDecisionContext(this, field, goalDifference, supportY);
        String action = context.shouldJumpForBall() ? "jump" : decisionStrategy.chooseAction(context);
        return action.isEmpty() ? Set.of() : Set.of(action);
    }

    /** Crea la política que el flujo común usará durante el partido.
     * @return estrategia de decisión, pasiva en el tipo base
     */
    public BotDecisionStrategy createDecisionStrategy() {
        return context -> "";
    }
}
