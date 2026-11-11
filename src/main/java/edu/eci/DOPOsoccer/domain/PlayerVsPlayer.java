package edu.eci.DOPOSoccer.domain;
import edu.eci.DOPOSoccer.domain.exception.GameException;
import java.util.List;

/**
 * Sesión entre dos jugadores humanos.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class PlayerVsPlayer extends GameSession {

    /**
     * Crea una sesión entre dos jugadores humanos y construye la modalidad
     * del partido solicitada.
     *
     * @param player indicador reenviado al constructor base; esta clase agrega
     *        ambos jugadores humanos independientemente de su valor
     * @param typeMatche nombre completo de la clase de modalidad del partido
     * @param parametersRules parámetros entregados a la modalidad del partido
     * @throws GameException si la modalidad no existe o no se puede instanciar
     */
    public PlayerVsPlayer(boolean player,String typeMatche, List<String> parametersRules) throws GameException {
        super(player,typeMatche,parametersRules);
        Player player1 = new Player(1);
        Player player2 = new Player(2);
        participantsRight.add(player1); 
        participantsLeft.add(player2);
    }

    @Override 
    public void resetConfiguration(boolean player) {
        if (player) {
            participantsRight.get(0).resetConfiguration();
        }
        else {
            participantsLeft.get(0).resetConfiguration();
        }
    }

    /**
     * Agrega los usuarios indicados a la sesión.
     * @param user identificadores de usuario
     */
    public void addParticipants(String[] user) {
    }
}
