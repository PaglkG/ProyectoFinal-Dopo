package domain;
import domain.GameException;
import domain.PowerFactory;
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
    private PlayerVsPlayer(boolean player, String typeMatche, List<String> parametersRules,
            List<String> parametersPowers) throws GameException {
        super(player,typeMatche,parametersRules);
        Player player1 = new Player(1);
        Player player2 = new Player(2);
        if (parametersPowers == null || parametersPowers.size() != 2) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        player1.setSpecialPower(PowerFactory.create(parametersPowers.get(0)));
        player2.setSpecialPower(PowerFactory.create(parametersPowers.get(1)));
        participantsRight.add(player1); 
        participantsLeft.add(player2);
        positionParticipants();
    }


}
