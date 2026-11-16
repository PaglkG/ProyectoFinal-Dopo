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
     * @throws IndexOutOfBoundsException si la lista no contiene un elemento
     * @throws NumberFormatException si el primer elemento no representa un entero
     */
    public ByGoals(List<String> parametersRules) throws GameException {
        goals = Integer.parseInt(parametersRules.get(0));
        if (goals < 0) {
            throw new GameException(GameException.GOALS_NOT_NEGATIVE);
        }
    }

    /**
     * Compara los marcadores recibidos con el límite de goles configurado.
     *
     * @param parameters marcadores del jugador izquierdo y derecho, en ese orden
     * @return {@code true} si alguno de los marcadores es igual al límite
     * @throws IndexOutOfBoundsException si no hay dos marcadores
     * @throws NumberFormatException si un marcador no representa un entero
     */
    @Override
    public boolean check(List<String> parameters) {
        int playerLeft = Integer.parseInt(parameters.get(0));
        int playerRight = Integer.parseInt(parameters.get(1));
        return isGoals(playerLeft, playerRight);
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
