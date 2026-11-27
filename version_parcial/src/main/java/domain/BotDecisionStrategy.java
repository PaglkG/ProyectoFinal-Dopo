package domain;

import java.io.Serializable;

/** Define una política intercambiable que el bot consulta para decidir. */
@FunctionalInterface
interface BotDecisionStrategy extends Serializable {
    /** Elige una acción usando únicamente los datos observados del paso.
     * @param context observación de cancha, balón y marcador
     * @return nombre de la acción, o texto vacío si decide esperar
     */
    String chooseAction(BotDecisionContext context);
}
