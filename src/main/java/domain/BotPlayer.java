package domain;

import java.util.Set;

/**
 * Define el comportamiento común de los perfiles controlados por la máquina.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class BotPlayer extends SessionParticipant {

    /** Crea un participante controlado por la máquina sin estado de decisión inicial. */
    public BotPlayer() {
    }

    /**
     * Punto de extensión para elegir acciones a partir de la cancha. Actualmente
     * no evalúa el estado y devuelve {@code null}.
     * @param field cancha actual
     * @return conjunto de nombres de acción, o {@code null} mientras la decisión no esté implementada
     */
    public Set<String> decideAction(SoccerField field) {
        return null;
    }
}
