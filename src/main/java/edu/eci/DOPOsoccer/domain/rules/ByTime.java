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
        int minutesInGame = Integer.parseInt(parameters.get(3));
        if (isTime(minutesInGame)) {
            if (isSudenDeath(minutesInGame, minutesInGame)) {
                return false;
            }
            return true;
        }
        return true;
    }

    private boolean isSudenDeath(int goalLeft, int goalRight) {
        if (goalLeft == goalRight ) {
            return false;
        }
        return true;
    }

    private boolean isTime(int minutesInGame) {
        return false;
    }

}
