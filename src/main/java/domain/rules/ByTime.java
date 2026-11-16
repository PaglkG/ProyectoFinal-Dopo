package domain.rules;
import java.util.List;
import domain.exception.GameException;
/**
 * Tipo de partido con duración limitada y opción de muerte súbita.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByTime extends TypeOfMatch {

    private int minutes;
    private boolean suddenDeath;


    /**
     * Lee el primer parámetro como duración límite en minutos y rechaza valores
     * negativos.
     *
     * @param parametersRules lista cuyo primer elemento representa los minutos
     * @throws GameException si la duración es negativa
     * @throws IndexOutOfBoundsException si la lista no contiene un elemento
     * @throws NumberFormatException si el primer elemento no representa un entero
     */
    public ByTime(List<String> parametersRules) throws GameException{
        minutes = Integer.parseInt(parametersRules.get(0));
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
     * @throws IndexOutOfBoundsException si no hay tres valores
     * @throws NumberFormatException si los marcadores o el tiempo no son enteros
     */
    @Override
    public boolean check(List<String> parameters) {
        int goalLeft = Integer.parseInt(parameters.get(0));
        int goalRight = Integer.parseInt(parameters.get(1));
        int minutesInGame = Integer.parseInt(parameters.get(2));
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
