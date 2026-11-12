package edu.eci.DOPOSoccer.domain;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import edu.eci.DOPOSoccer.domain.rules.*;
import edu.eci.DOPOSoccer.domain.rules.TypeOfMatch;
import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.rules.Rule;
import edu.eci.DOPOSoccer.domain.rules.TypeOfMatch;

/**
 * Mantiene el estado de una sesión, sus participantes, la cancha, el marcador
 * y la modalidad que determina cuándo termina el partido.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class GameSession {

    protected double timeLeft;
    protected int yBaseRight;
    protected int xBaseRight;
    protected int yBaseLeft;
    protected int xBaseLeft;
    protected static double GRAVITY;
    protected TypeOfMatch typeOfMatch;
    protected SoccerField field;
    protected Scoreboard scoreboard;
    protected List<SessionParticipant> participantsRight;
    protected List<SessionParticipant> participantsLeft;
    protected TypeOfMatch TypeOfMatch;
    

    /**
     * Crea una sesión e instancia reflectivamente su modalidad de partido.
     *
     * @param player indicador que la modalidad concreta puede usar para elegir
     *        el jugador humano; no determina el lado de la cancha
     * @param typeMatche nombre completo de la clase de modalidad, como
     *        {@code ByGoals} o {@code ByTime}
     * @param parametersRules parámetros requeridos por el constructor de esa modalidad
     * @throws GameException si la clase no existe o no se puede instanciar
     */
    public GameSession(boolean player,String typeMatche, List<String> parametersRules) throws GameException {
        participantsRight = new ArrayList<>();
        participantsLeft = new ArrayList<>();
        Object typeMatcheObject = whatsRules(typeMatche, parametersRules);
        TypeOfMatch typeMatcheFinal = (TypeOfMatch) typeMatcheObject;
        scoreboard = new Scoreboard();
        field = new SoccerField();
        timeLeft = 0;
        GRAVITY = 3.14;
        typeOfMatch = typeMatcheFinal;
    }

    /**
     * Retira al participante de la sesión.
     * @param participant participante que se retirará
     */
    public void removeParticipant(SessionParticipant participant) {
    }

    /**
     * Solicita el movimiento del participante.
     * @param user participante
     * @param direction dirección
     */
    public void movePlayer(SessionParticipant user, char direction) {
    }

    /** Reinicia la ronda en el centro de la cancha después de un gol. */
    public void restartRound() {
    }

    /**
     * Comprueba si se cumple la cantidad mínima de participantes.
     * @return sesión con participantes suficientes o estado pendiente
     */
    public GameSession minParticipants() {
        return null;
    }


    

    /**
     * Registra un gol para el lado indicado.
     * @param side lado que anotó
     */
    public void goal(String side) {
    }

    /**
     * Solicita un salto al participante.
     * @param player participante que saltará
     */
    public void jumpPlayer(SessionParticipant player) {
    }

    /**
     * Solicita la activación del poder del participante.
     * @param player participante que usará el poder
     */
    public void powerPlayer(SessionParticipant player) {
    }

    /**
     * Crea la modalidad de partido indicada usando su constructor que recibe
     * la lista de parámetros.
     * @param typeGame nombre completo de la clase de modalidad
     * @param parametersRules argumentos de configuración de la modalidad
     * @return instancia de la modalidad creada
     * @throws GameException si la clase no existe o no se puede instanciar
     */
    private Object whatsRules(String typeGame, List<String> parametersRules) throws GameException{
        try {
            Class<?> targetClass = Class.forName(typeGame);
            if (!TypeOfMatch.class.isAssignableFrom(targetClass)) {
                throw new GameException(GameException.NO_FOUND_TYPE_GAME);
            }
            return TypeOfMatch.class.cast(
                    targetClass.getDeclaredConstructor(List.class).newInstance(parametersRules));
        }
        catch (ClassNotFoundException e) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        catch (NoSuchMethodException  e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        }
        catch (InstantiationException  e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
        catch (IllegalAccessException  e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        }
        catch (InvocationTargetException  e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
    }

    /** Comprueba el estado del tipo de partido asociado a la sesión. */
    public void checkTypeOfMatch() {
    }
}
