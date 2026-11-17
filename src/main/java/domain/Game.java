package domain;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import domain.exception.GameException;

/**
 * Coordina la creación y administración de sesiones, participantes,
 * estadísticas y persistencia de las partidas.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Game {

    /** Rectángulo inmutable expuesto por las consultas de estado de una sesión. */
    public record RectangleView(double x, double y, double width, double height) { }

    /** Estado observable de un participante; su id tiene el formato {@code lado:índice}. */
    public record ParticipantView(String id, String side, double x, double y,
            double width, double height, int speed) { }

    /** Estado observable de un balón. */
    public record BallView(int index, double x, double y, double radius,
            double velocityX, double velocityY) { }

    /** Instantánea inmutable para dibujar o consultar una sesión sin exponer sus objetos internos. */
    public record SessionView(RectangleView court, List<RectangleView> boundaries,
            List<RectangleView> goals, List<ParticipantView> participants, List<BallView> balls,
            int goalsLeft, int goalsRight, double elapsedSeconds, boolean finished) {
        public SessionView {
            boundaries = List.copyOf(boundaries);
            goals = List.copyOf(goals);
            participants = List.copyOf(participants);
            balls = List.copyOf(balls);
        }
    }

    private List<GameSession> sessions = new ArrayList<>();
    private final Map<Integer, Stats> playerStats = new HashMap<>();

    /** Crea el coordinador con una lista vacía de sesiones. */
    public Game() {
        sessions = new ArrayList<>();
    }

    /**
     * Crea una sesión de la clase indicada, le pasa la modalidad del partido
     * y sus parámetros, restaura estadísticas humanas por ID y la agrega a la
     * lista de sesiones administradas.
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
     * @throws GameException si no se encuentra o no se puede construir la clase de sesión
     */
    public GameSession startSession(
            List<String> parametersRules,
            List<String> parametersPowers,
            String typeGame,
            String typeMatche,
            boolean player) throws GameException {
        GameSession session = whatSession(typeGame, player, parametersRules, typeMatche, parametersPowers);
        restorePlayerStats(session);
        sessions.add(session);
        return session;
    }

    private void restorePlayerStats(GameSession session) {
        for (SessionParticipant participant : session.getParticipantsLeft()) {
            restorePlayerStats(participant);
        }
        for (SessionParticipant participant : session.getParticipantsRight()) {
            restorePlayerStats(participant);
        }
    }

    private void restorePlayerStats(SessionParticipant participant) {
        if (participant instanceof Player player) {
            Stats savedStats = playerStats.computeIfAbsent(player.getUserId(), ignored -> participant.getStats());
            participant.setStats(savedStats);
        }
    }

    /** Crea una sesión y devuelve su índice opaco para consultar por esta fachada. */
    public int startSessionForView(List<String> parametersRules, List<String> parametersPowers,
            String typeGame, String typeMatche, boolean player) throws GameException {
        startSession(parametersRules, parametersPowers, typeGame, typeMatche, player);
        return sessions.size() - 1;
    }

    /**
     * Cierra la sesión indicada.
     *
     * @param closeSession sesión que se cerrará
     */
    public void closeSession(GameSession closeSession) {
        sessions.remove(closeSession);
    }

    /**
     * Punto de extensión para reiniciar la sesión; actualmente el cuerpo está
     * vacío y no modifica su estado.
     * @param session sesión que se reiniciaría
     */
    public void resetSession(GameSession session) {
    }


    /**
     * Solicita a la sesión que mueva al participante indicado.
     *
     * @param session sesión activa
     * @param player participante que se moverá
     * @param direction dirección del movimiento
     */
    public void movePlayer(
            GameSession session,
            SessionParticipant player,
            boolean direction) {
        if (session != null) {
            session.movePlayer(player, direction);
        }
    }

    /** Mueve por identificador al participante de una sesión administrada. */
    public void movePlayer(int sessionIndex, String participantId, boolean direction) {
        GameSession session = requireSession(sessionIndex);
        movePlayer(session, findParticipant(session, participantId), direction);
    }

    /** Ejecuta una patada por identificador y devuelve si el balón estaba al alcance. */
    public boolean kickPlayer(int sessionIndex, String participantId) {
        GameSession session = requireSession(sessionIndex);
        return session.kickPlayer(findParticipant(session, participantId));
    }

    /** Avanza una sesión administrada un paso fijo. */
    public void updateGame(int sessionIndex) {
        updateGame(requireSession(sessionIndex));
    }

    /** Devuelve una instantánea sin referencias mutables a clases internas del dominio. */
    public SessionView inspectSession(int sessionIndex) {
        GameSession session = requireSession(sessionIndex);
        SoccerField field = session.getField();
        var boundaries = java.util.Arrays.stream(field.getHitboxField())
                .map(box -> new RectangleView(box.getX(), box.getY(), box.getWidth(), box.getHeight()))
                .toList();
        var goals = java.util.Arrays.stream(field.getGoalHitboxes())
                .map(box -> new RectangleView(box.getX(), box.getY(), box.getWidth(), box.getHeight()))
                .toList();
        var participants = new ArrayList<ParticipantView>();
        addParticipantViews(participants, "left", session.getParticipantsLeft());
        addParticipantViews(participants, "right", session.getParticipantsRight());
        var balls = new ArrayList<BallView>();
        Ball[] sessionBalls = field.getBalls();
        for (int index = 0; index < sessionBalls.length; index++) {
            Ball ball = sessionBalls[index];
            balls.add(new BallView(index, ball.getX(), ball.getY(), ball.getRadius(),
                    ball.getVelocityX(), ball.getVelocityY()));
        }
        RectangleView top = boundaries.get(0);
        RectangleView bottom = boundaries.get(1);
        RectangleView left = boundaries.get(2);
        RectangleView right = boundaries.get(3);
        RectangleView court = new RectangleView(top.x(), top.y(),
                right.x() + right.width() - left.x(),
                bottom.y() + bottom.height() - top.y());
        return new SessionView(court, boundaries, goals, participants, balls,
                session.getGoalsLeft(), session.getGoalsRight(), session.getElapsedSeconds(), session.isFinished());
    }

    /** Coloca a un participante para una prueba manual o visual. */
    public void setParticipantPosition(int sessionIndex, String participantId, double x, double y) {
        GameSession session = requireSession(sessionIndex);
        findParticipant(session, participantId).resetPosition(x, y);
    }

    /** Coloca un balón para preparar un escenario reproducible. */
    public void setBallPosition(int sessionIndex, int ballIndex, double x, double y) {
        requireBall(sessionIndex, ballIndex).reposition(x, y);
    }

    /** Aplica un cambio de velocidad al balón indicado. */
    public void applyBallImpulse(int sessionIndex, int ballIndex, double deltaX, double deltaY) {
        requireBall(sessionIndex, ballIndex).applyImpulse(deltaX, deltaY);
    }

    private GameSession requireSession(int sessionIndex) {
        if (sessionIndex < 0 || sessionIndex >= sessions.size()) {
            throw new GameException(GameException.INVALID_SESSION_INDEX);
        }
        return sessions.get(sessionIndex);
    }

    private Ball requireBall(int sessionIndex, int ballIndex) {
        Ball[] balls = requireSession(sessionIndex).getField().getBalls();
        if (ballIndex < 0 || ballIndex >= balls.length) {
            throw new GameException(GameException.INVALID_BALL_INDEX);
        }
        return balls[ballIndex];
    }

    private SessionParticipant findParticipant(GameSession session, String participantId) {
        if (participantId == null) throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        String[] parts = participantId.split(":", -1);
        if (parts.length != 2) throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        List<SessionParticipant> participants = switch (parts[0]) {
            case "left" -> session.getParticipantsLeft();
            case "right" -> session.getParticipantsRight();
            default -> throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        };
        try {
            return participants.get(Integer.parseInt(parts[1]));
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            throw new GameException(GameException.INVALID_PARTICIPANT_ID, exception);
        }
    }

    private void addParticipantViews(List<ParticipantView> target, String side,
            List<SessionParticipant> participants) {
        for (int index = 0; index < participants.size(); index++) {
            var participant = participants.get(index);
            var box = participant.getHitbox();
            target.add(new ParticipantView(side + ":" + index, side,
                    box.getX(), box.getY(), box.getWidth(), box.getHeight(), participant.getStats().getSpeed()));
        }
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
    private GameSession whatSession(String name, boolean player, List<String> parametersRules,
            String typeMatche, List<String> parametersPowers) throws GameException {
        try {
            Class<?> targetClass = Class.forName(name);
            if (!GameSession.class.isAssignableFrom(targetClass)) {
                throw new GameException(GameException.NO_FOUND_SESSION);
            }
            return GameSession.class.cast(
                    targetClass.getDeclaredConstructor(boolean.class, String.class, List.class, List.class)
                            .newInstance(player, typeMatche, parametersRules, parametersPowers));
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
     * Mejora una habilidad del participante seleccionado dentro de la sesión.
     *
     * @param session sesión del jugador
     * @param player indicador del jugador al que se mejorará
     * @param stats estadística que se mejorará
     */
    public void ImproveStatistics(
            GameSession session,
            boolean player,
            String stats) {
        if (session != null) {
            session.ImproveStatistics(player, stats);
        }
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
