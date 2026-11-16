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

    /** Crea el coordinador con una lista vacía de sesiones. */
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
        int indice = sessions.indexOf(closeSession);
        sessions.remove(indice);
    }

    /**
     * Punto de extensión para reiniciar la sesión; actualmente el cuerpo está
     * vacío y no modifica su estado.
     * @param session sesión que se reiniciaría
     */
    public void resetSession(GameSession session) {
    }


    /**
     * Punto de extensión para solicitar movimiento; actualmente el cuerpo no
     * delega la acción a la sesión.
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
     * Punto de extensión para solicitar un salto; actualmente el cuerpo está vacío.
     *
     * @param session sesión activa
     * @param player participante que saltará
     */
    public void jumpPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Punto de extensión para activar el poder. El cuerpo está vacío y no aplica
     * efectos; la regla de daño todavía no está confirmada en la fuente de
     * verdad del proyecto.
     *
     * @param session sesión activa
     * @param player participante que usará el poder
     */
    public void powerPlayer(GameSession session, SessionParticipant player) {
    }

    /**
     * Busca la clase por nombre, confirma que herede de {@link GameSession} y
     * llama al constructor que recibe {@code (boolean, String, List)} en ese
     * orden. Traduce los errores de carga, firma, acceso e invocación a
     * {@link GameException} con el código correspondiente.
     *
     * @param name nombre completo de la clase concreta de sesión
     * @param player indicador que se pasa al constructor de la sesión
     * @param parametersRules parámetros de la modalidad del partido
     * @param typeMatche nombre completo de la clase de modalidad del partido
     * @return instancia creada
     * @throws GameException si la clase no existe, no es una sesión, no ofrece
     *         el constructor esperado o no puede construirse
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
     * Punto de extensión para mejorar una estadística; actualmente el cuerpo
     * está vacío y no cambia el estado de la sesión.
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

    /** Punto de extensión para guardar sesiones; actualmente no escribe datos. */
    public void save() {
    }

    /**
     * Punto de extensión para abrir el estado de una partida. Actualmente no
     * lee el archivo ni modifica las sesiones.
     *
     * @param file archivo que contiene el estado guardado
     */
    public void open(File file) {
    }

    /** Delega a la sesión indicada la actualización de un paso del juego. */
    public void updateGame(GameSession game) {
        game.updateGame();
    }
}
