package edu.eci.DOPOSoccer.domain;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import edu.eci.DOPOSoccer.domain.exception.GameException;
import edu.eci.DOPOSoccer.domain.rules.Rule;
import edu.eci.DOPOSoccer.domain.rules.TypeOfMatche;

/**
 * Mantiene el estado de una sesión y sus dos lados de juego.
 * El balón está sometido a gravedad y rebota en el suelo, los arcos y los bordes de la cancha.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GameSession {

    private double timeLeft;
    private int yBaseRight;
    private int xBaseRight;
    private int yBaseLeft;
    private int xBaseLeft;
    private static double GRAVITY;
    private SoccerField field;
    private Scoreboard scoreboard;
    private List<SessionParticipant> participantsRight;
    private List<SessionParticipant> participantsLeft;
    private TypeOfMatche typeOfMatche;

    /** Crea una sesión de juego. */
    public GameSession() {
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
     * Asigna las reglas del tipo de partido indicado.
     * @param typeMatche identificador del tipo de partido
     */
    public void assignRules(String typeMatche) throws GameException {
    }

    /**
     * Agrega una regla a la sesión.
     * @param rule regla que se agregará
     */
    public void addRule(Rule rule) {
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
     * Busca las reglas asociadas al tipo de juego indicado.
     * @param typeGame identificador del tipo de juego
     * @return tipo de partido correspondiente
     */
    private Object whatsRules(String typeGame) throws GameException{
        try {
            Class<?> targetClass = Class.forName(typeGame);
            Object newInstance = targetClass.getDeclaredConstructor().newInstance();
            return newInstance;
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
