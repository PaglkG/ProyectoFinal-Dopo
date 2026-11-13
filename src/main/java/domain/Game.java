package domain;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;

import domain.exception.GameException;

/**
 * Coordina la creación y administración de sesiones, participantes,
 * estadísticas y persistencia de las partidas.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Game {

    private List<GameSession> sessions = new ArrayList<>();

    /** Crea el coordinador de las partidas. */
    public Game() {
        sessions = new ArrayList<>();
    }

    /**
     * Crea una sesión de la clase indicada, le pasa la modalidad del partido
     * y sus parámetros, y la agrega a la lista de sesiones administradas.
     *
     * @param player selecciona el jugador humano cuando la modalidad lo requiere
     *        ({@code true}: jugador 1; {@code false}: jugador 2); no selecciona el lado
     * @param parametersRules parámetros que recibe la modalidad del partido
     * @param typeGame nombre completo de la clase concreta de sesión, como
     *        {@link PlayerVsBot} o {@link PlayerVsPlayer}
     * @param typeMatche nombre completo de la clase que define la modalidad
     *        del partido, como {@code ByGoals} o {@code ByTime}
     * @throws GameException si no se encuentra o no se puede construir la clase de sesión
     */
    public GameSession startSession(
            List<String> parametersRules,
            String typeGame,
            String typeMatche,
            boolean player) throws GameException {
        GameSession session = whatSession(typeGame, player, parametersRules, typeMatche);

        sessions.add(session);
        return session;
    }

    /**
     * Cierra la sesión indicada.
     *
     * @param closeSession sesión que se cerrará
     */
    public void closeSession(GameSession closeSession) {
        sessions.remove(0);
    }

    /**
     * Reinicia la sesión indicada.
     *
     * @param session sesión que se reiniciará
     */
    public void resetSession(GameSession session) {
    }


    /**
     * Solicita el movimiento del participante en la dirección indicada.
     *
     * @param session sesión activa
     * @param player participante que se moverá
     * @param direction dirección del movimiento
     */
    public void movePlayer(
            GameSession session,
            SessionParticipant player,
            boolean direction) {
    }

    /**
     * Solicita un salto del participante.
     *
     * @param session sesión activa
     * @param player participante que saltará
     */
    public void jumpPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Solicita la activación del poder del participante. La guía define el daño
     * como dañoBasePoder × multiplicadorKick, con
     * multiplicadorKick = 1 + 0,10 × (kick - 1). La vida resultante no puede
     * ser negativa: max(0, vidaActual - daño). Si llega a cero, el participante
     * queda aturdido durante 3 segundos.
     *
     * @param session sesión activa
     * @param player participante que usará el poder
     */
    public void powerPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Construye reflectivamente una sesión de la clase indicada.
     *
     * @param name nombre completo de la clase concreta de sesión
     * @param player indicador que se pasa al constructor de la sesión
     * @param parametersRules parámetros de la modalidad del partido
     * @param typeMatche nombre completo de la clase de modalidad del partido
     * @return instancia creada
     * @throws GameException si la clase no existe o no se puede instanciar
     */
    private GameSession whatSession(String name, boolean player, List<String> parametersRules, String typeMatche) throws GameException {
        try {
            Class<?> targetClass = Class.forName(name);
            if (!GameSession.class.isAssignableFrom(targetClass)) {
                throw new GameException(GameException.NO_FOUND_SESSION);
            }
            return GameSession.class.cast(
                    targetClass.getDeclaredConstructor(boolean.class, String.class, List.class)
                            .newInstance(player, typeMatche, parametersRules));
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

    /**
     * Solicita la mejora de una estadística para el jugador seleccionado.
     *
     * @param session sesión del jugador
     * @param player indicador del jugador al que se mejorará
     * @param stats estadística que se mejorará
     */
    public void ImproveStatistics(
            GameSession session,
            boolean player,
            String stats) {
    }

    /** Guarda el estado de la partida. */
    public void save() {
    }

    /**
     * Abre el estado de una partida desde un archivo.
     *
     * @param file archivo que contiene el estado guardado
     */
    public void open(File file) {
    }

    /** Actualiza la sesión indicada. */
    public void updateGame(GameSession game) {
    }
}
