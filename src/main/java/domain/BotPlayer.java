package domain;

import java.util.Set;
import domain.enums.Action;

/**
 * Define el comportamiento común de los perfiles controlados por la máquina.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class BotPlayer extends SessionParticipant {

    /** Crea un participante controlado por la máquina. */
    public BotPlayer() {
    }

    /**
     * Decide las acciones del bot según el estado de la cancha.
     * @param field cancha actual
     * @return conjunto de acciones
     */
    public Set<Action> decideAction(SoccerField field) {
        return null;
    }
}
