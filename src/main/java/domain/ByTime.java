package domain;
import java.util.List;
import domain.GameException;
/**
 * Tipo de partido con duración limitada y opción de muerte súbita.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByTime extends TypeOfMatch {

    /** Duración reglamentaria del partido en minutos. */
    private int minutes;
    /** Indica si el empate mantiene activa la partida después del tiempo reglamentario. */
    private boolean suddenDeath;

    /** Indica si el partido se encuentra en muerte súbita.
     * @return {@code true} si el tiempo terminó con empate y el partido continúa
     */
    public boolean isSuddenDeath() { return suddenDeath; }

    /** Limpia la muerte súbita al reiniciar una partida. */
    void reset() { suddenDeath = false; }


    /**
     * Lee el primer parámetro como duración límite en minutos y rechaza valores
     * negativos.
     *
     * @param parametersRules lista cuyo primer elemento representa los minutos
     * @throws GameException si la duración es negativa
     * @throws GameException si los parámetros no son válidos o la duración es negativa
     */
    public ByTime(List<String> parametersRules) throws GameException{
        minutes = parseInteger(parametersRules, 0, GameException.INVALID_RULE_PARAMETERS);
        if (minutes < 0) {
            throw new GameException(GameException.MINUTES_NOT_NEGATIVE);
        }
    }

    /**
     * Evalúa si el partido continúa según el tiempo transcurrido y el marcador.
     *
     * @param parameters marcador izquierdo, marcador derecho y minutos
     *        transcurridos, en ese orden
     * Si el tiempo no se ha agotado, deja el estado de muerte súbita como está.
     * Cuando ya se agotó, activa ese estado si hay empate; de lo contrario, lo
     * desactiva.
     *
     * @return {@code true} si la partida continúa; {@code false} si el tiempo
     *         se agotó y el marcador es desigual
     * @throws GameException si faltan valores o algún parámetro no es entero
     */
    @Override
    public boolean check(List<String> parameters) {
        int goalLeft = parseInteger(parameters, 0, GameException.INVALID_SCORE_PARAMETERS);
        int goalRight = parseInteger(parameters, 1, GameException.INVALID_SCORE_PARAMETERS);
        int minutesInGame = parseInteger(parameters, 2, GameException.INVALID_SCORE_PARAMETERS);
        if (!isTime(minutesInGame)) {
            return true;
        }
        if (isSuddenDeath(goalLeft, goalRight)) {
            suddenDeath = true;
            return true;
        }
        suddenDeath = false;
        return false;
    }

    /** Convierte a entero el parámetro indicado o lanza la excepción de dominio correspondiente. */
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
     * Comprueba si el marcador está empatado para mantener la muerte súbita.
     *
     * @param goalLeft marcador del lado izquierdo
     * @param goalRight marcador del lado derecho
     * @return {@code true} si ambos marcadores son iguales
     */
    private boolean isSuddenDeath(int goalLeft, int goalRight) {
        return goalLeft == goalRight;
    }

    /**
     * Comprueba si se alcanzó o superó el límite de tiempo configurado.
     *
     * @param minutesInGame minutos transcurridos en el partido
     * @return {@code true} si los minutos transcurridos son iguales o mayores
     *         que el límite
     */
    private boolean isTime(int minutesInGame) {
        if (minutesInGame >= minutes) {
            return true;
        }
        return false;
    }

}
