package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Administra el acceso a la sesión singleton y ofrece operaciones de partida
 * y consultas escalares a las capas cliente.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Game {

    /** Nombre y extensión iniciales propuestos por el selector de guardado. */
    public static String DEFAULT_SAVE_FILE = "partida.dat";

    /** Crea la fachada antes de iniciar o cargar una sesión. */
    public Game() {

    }

    private GameSession session;
    /**
     * Obtiene la sesión singleton. La primera llamada elige su clase concreta;
     * las siguientes reutilizan esa instancia. Game conserva una única
     * referencia a la instancia recibida.
     *
     * @param player selecciona el jugador humano cuando la modalidad lo requiere
     *        ({@code true}: jugador 1; {@code false}: jugador 2); no selecciona el lado
     * @param parametersRules parámetros que recibe la modalidad del partido
     * @param parametersPowers poderes en orden de participantes; en PVB incluye
     *        además el perfil del bot como tercer valor
     * @param typeGame nombre completo de la clase concreta de sesión, como
     *        {@link PlayerVsBot} o {@link PlayerVsPlayer}
     * @param typeMatche nombre completo de la clase que define la modalidad
     *        del partido, como {@code ByGoals} o {@code ByTime}
     * @return sesión singleton administrada por esta fachada
     * @throws GameException si no se encuentra o no se puede construir la clase de sesión
     */
    public GameSession startSession(
            List<String> parametersRules,
            List<String> parametersPowers,
            String typeGame,
            String typeMatche,
            boolean player) throws GameException {
        session = GameSession.getInstance(
                typeGame, player, typeMatche, parametersRules, parametersPowers);
        return session;
    }

    /** Reinicia la ronda sin reemplazar la sesión administrada ni su marcador.
     */
    public void resetSession() {
        if (session != null) session.restartRound();
    }

    /** Mueve al participante seleccionado por identidad.
     * @param player {@code true} para jugador 1; {@code false} para jugador 2
     * @param direction {@code true} para derecha; {@code false} para izquierda
     */
    public void movePlayer(boolean player, boolean direction) {
        GameSession session = requireSession();
        session.movePlayer(findPlayer(player), direction);
    }

    /** Solicita un salto para el participante seleccionado.
     * @param player {@code true} para jugador 1; {@code false} para jugador 2
     * @throws GameException si no hay sesión activa o no se encuentra el participante
     */
    public void jumpPlayer(boolean player) {
        GameSession session = requireSession();
        session.jumpPlayer(findPlayer(player));
    }

    /** Ejecuta una patada para el participante seleccionado.
     * @param player {@code true} para jugador 1; {@code false} para jugador 2
     * @return {@code true} si la patada alcanzó al rival o al balón
     */
    public boolean kickPlayer(boolean player) {
        GameSession session = requireSession();
        return session.kickPlayer(findPlayer(player));
    }

    /** Devuelve la coordenada horizontal del participante seleccionado.
     * @param player identidad del jugador que se consulta
     * @return coordenada horizontal
     */
    public double getPlayerX(boolean player) {
        return findPlayer(player).getHitbox().getX();
    }
    /** Devuelve la coordenada vertical del participante seleccionado.
     * @param player identidad del jugador que se consulta
     * @return coordenada vertical
     */
    public double getPlayerY(boolean player) { 
        return findPlayer(player).getHitbox().getY(); 
    }
    /** Devuelve el ancho de la hitbox del participante seleccionado.
     * @param player identidad del jugador que se consulta
     * @return ancho de la hitbox
     */
    public double getPlayerWidth(boolean player) { 
        return findPlayer(player).getHitbox().getWidth(); 
    }
    /** Devuelve el alto de la hitbox del participante seleccionado.
     * @param player identidad del jugador que se consulta
     * @return alto de la hitbox
     */
    public double getPlayerHeight(boolean player) { 
        return findPlayer(player).getHitbox().getHeight(); 
    }
    /** Devuelve el nivel de velocidad del participante seleccionado.
     * @param player identidad del jugador que se consulta
     * @return nivel actual de velocidad
     */
    public int getPlayerSpeed(boolean player) { 
        return findPlayer(player).getStats().getSpeed(); 
    }
    /** Devuelve los puntos disponibles del participante seleccionado.
     * @param player {@code true} para el jugador 1; {@code false} para el jugador 2
     * @return puntos disponibles para mejoras
     * @throws GameException si no hay sesión o no existe el participante
     */
    public int getPlayerPoints(boolean player) {
        return findPlayer(player).getStats().getPoints();
    }

    /** Devuelve la vida actual del participante seleccionado.
     * @param player {@code true} para el jugador 1; {@code false} para el jugador 2
     * @return vida actual del participante
     * @throws GameException si no hay sesión o no existe el participante
     */
    public int getPlayerLife(boolean player) {
        return findPlayer(player).getLife();
    }

    /** Devuelve la vida máxima del participante según sus estadísticas.
     * @param player {@code true} para el jugador 1; {@code false} para el jugador 2
     * @return vida máxima del participante
     * @throws GameException si no hay sesión o no existe el participante
     */
    public int getPlayerMaxLife(boolean player) {
        return findPlayer(player).getMaxLife();
    }

    /** Devuelve los segundos restantes para completar la recarga del poder asignado.
     * @param player {@code true} para el jugador 1; {@code false} para el jugador 2
     * @return segundos de recarga pendientes
     * @throws GameException si no hay sesión o no existe el participante
     */
    public double getPlayerPowerRemainingSeconds(boolean player) {
        return findPlayer(player).getSpecialPower().getRemainingSeconds();
    }

    /** Devuelve la coordenada horizontal del centro del balón indicado.
     * @param index índice del balón
     * @return coordenada horizontal del centro
     */
    public double getBallX(int index) { 
        return requireBall(index).getX(); 
    }
    /** Devuelve la coordenada vertical del centro del balón indicado.
     * @param index índice del balón
     * @return coordenada vertical del centro
     */
    public double getBallY(int index) { 
        return requireBall(index).getY(); 
    }
    /** Devuelve el radio del balón indicado.
     * @param index índice del balón
     * @return radio del balón
     */
    public double getBallRadius(int index) { 
        return requireBall(index).getRadius(); 
    }
    /** Devuelve la velocidad horizontal del balón indicado.
     * @param index índice del balón
     * @return velocidad horizontal
     */
    public double getBallVelocityX(int index) { 
        return requireBall(index).getVelocityX(); 
    }
    /** Devuelve la velocidad vertical del balón indicado.
     * @param index índice del balón
     * @return velocidad vertical
     */
    public double getBallVelocityY(int index) { 
        return requireBall(index).getVelocityY(); 
    }
    /** Devuelve la cantidad de balones de la sesión.
     * @return número de balones registrados
     */
    public int getBallCount() { 
        return requireSession().getField().getBalls().length; 
    }
    /** Devuelve la coordenada horizontal del arco indicado.
     * @param index índice del arco
     * @return coordenada horizontal
     */
    public double getGoalX(int index) { 
        return requireGoal(index).getX(); 
    }
    /** Devuelve la coordenada vertical del arco indicado.
     * @param index índice del arco
     * @return coordenada vertical
     */
    public double getGoalY(int index) { 
        return requireGoal(index).getY(); 
    }
    /** Devuelve el ancho del arco indicado.
     * @param index índice del arco
     * @return ancho del arco
     */
    public double getGoalWidth(int index) { 
        return requireGoal(index).getWidth(); 
    }
    /** Devuelve el alto del arco indicado.
     * @param index índice del arco
     * @return alto del arco
     */
    public double getGoalHeight(int index) { 
        return requireGoal(index).getHeight(); 
    }
    /** Devuelve la coordenada horizontal del borde izquierdo de la cancha.
     * @return coordenada horizontal
     */
    public double getCourtX() { 
        return requireSession().getField().courtX(); 
    }
    /** Devuelve la coordenada vertical del borde superior de la cancha.
     * @return coordenada vertical
     */
    public double getCourtY() { 
        return requireSession().getField().courtY(); 
    }
    /** Devuelve el ancho exterior de la cancha.
     * @return ancho de la cancha
     */
    public double getCourtWidth() { 
        return requireSession().getField().courtWidth(); 
    }
    /** Devuelve el alto exterior de la cancha.
     * @return alto de la cancha
     */
    public double getCourtHeight() { 
        return requireSession().getField().courtHeight(); 
    }
    /** Devuelve los goles anotados por el lado izquierdo.
     * @return goles del lado izquierdo
     */
    public int getGoalsLeft() { 
        return requireSession().getGoalsLeft(); 
    }
    /** Devuelve los goles anotados por el lado derecho.
     * @return goles del lado derecho
     */
    public int getGoalsRight() { 
        return requireSession().getGoalsRight(); 
    }
    /** Devuelve el tiempo transcurrido de la sesión.
     * @return tiempo transcurrido, en segundos
     */
    public double getElapsedSeconds() { 
        return requireSession().getElapsedSeconds(); 
    }
    /** Indica si la regla de partido terminó la sesión.
     * @return {@code true} si la sesión terminó
     */
    public boolean isFinished() { 
        return requireSession().isFinished(); 
    }

    /** Devuelve marcador y datos de tiempo en el formato de la regla activa.
     * @return goles izquierdo y derecho; {@code ByTime} agrega minutos y muerte súbita
     */
    public List<String> getMatchState() {
        GameSession active = requireSession();
        List<String> state = new ArrayList<>(List.of(
                Integer.toString(active.getGoalsLeft()), Integer.toString(active.getGoalsRight())));
        if (active.getTypeOfMatch() instanceof ByTime timeRule) {
            state.add(Integer.toString((int) Math.floor((active.getElapsedSeconds() + 1e-9) / 60)));
            state.add(Boolean.toString(timeRule.isSuddenDeath()));
        }
        return List.copyOf(state);
    }

    private RectangularHitbox requireGoal(int index) {
        // Obtiene el arco indicado y rechaza índices fuera de la lista.
        RectangularHitbox[] goals = requireSession().getField().getGoalHitboxes();
        if (index < 0 || index >= goals.length) throw new GameException(GameException.INVALID_BALL_INDEX);
        return goals[index];
    }

    private SessionParticipant findPlayer(boolean player) {
        // Resuelve el jugador solicitado por identidad, incluyendo la posición del bot.
        GameSession active = requireSession();
        int expectedId = player ? 1 : 2;
        for (SessionParticipant participant : active.getParticipantsLeft()) {
            if (participant instanceof Player actualPlayer && actualPlayer.getUserId() == expectedId) return participant;
        }
        for (SessionParticipant participant : active.getParticipantsRight()) {
            if (participant instanceof Player actualPlayer && actualPlayer.getUserId() == expectedId) return participant;
        }
        // In player-vs-bot, the bot occupies the identity not selected for the human.
        if (active instanceof PlayerVsBot) {
            SessionParticipant bot = active.getParticipantsRight().get(0);
            SessionParticipant human = active.getParticipantsLeft().get(0);
            boolean humanIsPlayer = human instanceof Player actualPlayer && actualPlayer.getUserId() == 1;
            if (player != humanIsPlayer) return bot;
        }
        throw new GameException(GameException.INVALID_PARTICIPANT_ID);
    }

    /** Devuelve el nombre simple de la clase concreta de la sesión administrada.
     * @return nombre de clase de la modalidad de sesión activa
     */
    public String getSessionType() {
        return requireSession().getClass().getSimpleName();
    }

    /** Devuelve los nombres simples de las implementaciones de {@link GameSession}.
     * @return nombres disponibles de los tipos de sesión
     */
    public List<String> getSessionTypes() {
        return GameCatalog.discover(GameSession.class, false, boolean.class, String.class, List.class, List.class);
    }

    /** Devuelve los nombres simples de los tipos de partido disponibles.
     * @return nombres disponibles de las reglas de partido
     */
    public List<String> getMatchTypes() {
        return GameCatalog.load("match-types.txt", TypeOfMatch.class, List.class)
                .stream().map(Class::getSimpleName).toList();
    }

    /** Devuelve los poderes registrados que pueden asignarse al crear una partida.
     * @return nombres completos de clase, en el orden del catálogo
     */
    public List<String> getPowerTypes() {
        return GameCatalog.load("powers.txt", Power.class).stream().map(Class::getName).toList();
    }

    /** Devuelve los nombres simples de los perfiles de bot disponibles.
     * @return nombres disponibles de perfiles de bot
     */
    public List<String> getBotTypes() {
        return GameCatalog.discover(BotPlayer.class, true);
    }

    /** Devuelve la sesión única administrada o falla si todavía no se ha iniciado. */
    private GameSession requireSession() {
        if (session == null) throw new GameException(GameException.NO_FOUND_SESSION);
        return session;
    }

    /** Devuelve el balón solicitado o lanza GameException si la sesión no está iniciada o el índice es inválido. */
    private Ball requireBall(int ballIndex) {
        Ball[] balls = requireSession().getField().getBalls();
        if (ballIndex < 0 || ballIndex >= balls.length) {
            throw new GameException(GameException.INVALID_BALL_INDEX);
        }
        return balls[ballIndex];
    }

    /**
     * Punto de extensión para activar el poder. El cuerpo está vacío y no aplica
     * efectos; el comportamiento del poder aún no está implementado.
     *
     * @param player participante que usará el poder
     */
    public void powerPlayer(SessionParticipant player) {
    }

    /**
     * Mejora una habilidad del participante seleccionado dentro de la sesión.
     *
     * @param player indicador del jugador al que se mejorará
     * @param stats estadística que se mejorará
     */
    public void improveStatistics(boolean player, String stats) {
        if (session != null) session.improveStatistics(player, stats);
    }

    /**
     * Guarda la sesión administrada como objeto serializado.
     *
     * @param file archivo que contiene el estado guardado
     * @throws GameException si no hay sesión, el archivo es nulo o no puede escribirse
     */
    public void save(File file) {
        GameSession active = requireSession();
        if (file == null) throw new GameException(GameException.INVALID_SAVE_FILE);
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(active);
        } catch (IOException exception) {
            throw new GameException(GameException.SAVE_FAILED, exception);
        }
    }

    /**
     * Abre una partida guardada y reemplaza la sesión única administrada.
     *
     * @param file archivo que contiene el estado serializado
     * @throws GameException si el archivo no puede validarse o deserializarse
     */
    public void open(File file) {
        if (file == null || !file.isFile()) throw new GameException(GameException.INVALID_SAVE_FILE);
        try (FileInputStream fileInput = new FileInputStream(file);
                ObjectInputStream input = new ObjectInputStream(fileInput)) {
            Object restored = input.readObject();
            if (!(restored instanceof GameSession restoredSession)) {
                throw new GameException(GameException.INVALID_SAVE_FILE);
            }
            GameSession.restoreInstance(restoredSession);
            session = restoredSession;
        } catch (IOException | ClassNotFoundException exception) {
            throw new GameException(GameException.OPEN_FAILED, exception);
        }
    }

    /** Avanza un paso fijo de la sesión singleton administrada, si está iniciada. */
    public void updateGame() {
        if (session != null) session.updateGame();
    }
}
