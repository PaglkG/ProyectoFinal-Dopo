package edu.eci.DOPOSoccer.domain.rules;
import java.util.ArrayList;
import java.util.List;
import edu.eci.DOPOSoccer.domain.exception.GameException;
/**
 * Tipo de partido que termina al alcanzar un límite de goles.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByGoals extends TypeOfMatch {

    private int goals;

    /** Crea una modalidad por goles. */
    public ByGoals(List<String> parametersRules) throws GameException {
        goals = Integer.parseInt(parametersRules.get(0));
        if (goals < 0) {
            throw new GameException(GameException.GOALS_NOT_NEGATIVE);
        }
    }

    @Override 
    public boolean check(List<String> parameters) {
        int playerLeft = Integer.parseInt(parameters.get(0));
        int playerRight = Integer.parseInt(parameters.get(1));
        return isGoals(playerLeft, playerRight);
    }

    private boolean isGoals(int playerLeft,int playerRight) {
        if (playerLeft == goals || playerRight == goals ) {
            return true;
        }
        return false;
    }
}
