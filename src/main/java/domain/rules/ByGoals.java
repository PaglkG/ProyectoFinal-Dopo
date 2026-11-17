package domain.rules;
import java.util.ArrayList;
import java.util.List;
import domain.exception.GameException;
/**
 * Tipo de partido que termina al alcanzar un límite de goles.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByGoals extends TypeOfMatch {

    private int goals;

    /**
     * Lee el primer parámetro como límite de goles y rechaza valores negativos.
     *
     * @param parametersRules lista cuyo primer elemento representa el límite
     * @throws GameException si el límite es negativo
     * @throws GameException si los parámetros no son válidos o el límite es negativo
     */
    public ByGoals(List<String> parametersRules) throws GameException {
        goals = parseInteger(parametersRules, 0, GameException.INVALID_RULE_PARAMETERS);
        if (goals < 0) {
            throw new GameException(GameException.GOALS_NOT_NEGATIVE);
        }
    }

    /**
     * Compara los marcadores recibidos con el límite de goles configurado.
     *
     * @param parameters marcadores del jugador izquierdo y derecho, en ese orden
     * @return {@code true} si alguno de los marcadores es igual al límite
     * @throws GameException si faltan marcadores o algún valor no es entero
     */
    @Override
    public boolean check(List<String> parameters) {
        int playerLeft = parseInteger(parameters, 0, GameException.INVALID_SCORE_PARAMETERS);
        int playerRight = parseInteger(parameters, 1, GameException.INVALID_SCORE_PARAMETERS);
        return isGoals(playerLeft, playerRight);
    }

    private int parseInteger(List<String> parameters, int index, String message) {
        if (parameters == null || parameters.size() <= index) {
            throw new GameException(message);
        }
        try {
            return Integer.parseInt(parameters.get(index));
        } catch (NumberFormatException exception) {
            throw new GameException(message, exception);
        }
    }

    /**
     * Determina si alguno de los participantes alcanzó exactamente el límite.
     *
     * @param playerLeft marcador del jugador izquierdo
     * @param playerRight marcador del jugador derecho
     * @return {@code true} si uno de los marcadores coincide con el límite
     */
    private boolean isGoals(int playerLeft,int playerRight) {
        if (playerLeft == goals || playerRight == goals ) {
            return true;
        }
        return false;
    }
}
