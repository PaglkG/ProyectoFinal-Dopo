package edu.eci.DOPOSoccer.domain.rules;
import java.util.List;
import edu.eci.DOPOSoccer.domain.exception.GameException;
/**
 * Tipo de partido con duración limitada y opción de muerte súbita.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByTime extends TypeOfMatch {

    private int minutes;


    /** Crea una modalidad por tiempo. */
    public ByTime(List<String> parametersRules) throws GameException{
        minutes = Integer.parseInt(parametersRules.get(0));
        if (minutes < 0) {
            throw new GameException(GameException.MINUTES_NOT_NEGATIVE);
        }
    }

    @Override 
    public boolean check(List<String> parameters) {
        int goalLeft = Integer.parseInt(parameters.get(0));
        int goalRight = Integer.parseInt(parameters.get(1));
        int minutesInGame = Integer.parseInt(parameters.get(2));
        if (!isTime(minutesInGame)) {
            return true;
        }
        return isSudenDeath(goalLeft, goalRight);
    }

    private boolean isSudenDeath(int goalLeft, int goalRight) {
        return goalLeft == goalRight;
    }

    private boolean isTime(int minutesInGame) {
        if (minutesInGame >= minutes) {
            return true;
        }
        return false;
    }

}
