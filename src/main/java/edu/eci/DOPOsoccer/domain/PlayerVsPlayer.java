package edu.eci.DOPOSoccer.domain;
import edu.eci.DOPOSoccer.domain.exception.GameException;
import java.util.List;

/**
 * Sesión entre dos jugadores humanos.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class PlayerVsPlayer extends GameSession {

    /** Crea una sesión de jugador contra jugador. */
    public PlayerVsPlayer(boolean player,String typeMatche, List<String> parametersRules) throws GameException {
        super(player,typeMatche,parametersRules);
        Player player1 = new Player(1);
        Player player2 = new Player(2);
        participantsRight.add(player1); 
        participantsLeft.add(player2);
    }

    /**
     * Agrega los usuarios indicados a la sesión.
     * @param user identificadores de usuario
     */
    public void addParticipants(String[] user) {
    }
}
