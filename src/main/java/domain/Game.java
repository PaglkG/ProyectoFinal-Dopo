package domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Administra el acceso a la sesión singleton y ofrece operaciones de partida
 * y vistas inmutables a las capas cliente.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Game {

    /** Rectángulo inmutable expuesto por las consultas de estado de una sesión. */
    /**
     * Rectángulo inmutable usado para dibujar límites, cancha y arcos.
     * @param x coordenada horizontal de su origen
     * @param y coordenada vertical de su origen
     * @param width ancho del rectángulo
     * @param height alto del rectángulo
     */
    public record RectangleView(double x, double y, double width, double height) { }

    /** Estado observable de un participante; su id tiene el formato {@code lado:índice}. */
    /**
     * Instantánea inmutable de la identidad, lado, posición, tamaño y velocidad de un participante.
     * @param id identificador opaco en formato lado:índice
     * @param side lado de la cancha, {@code left} o {@code right}
     * @param x coordenada horizontal de la esquina superior izquierda
     * @param y coordenada vertical de la esquina superior izquierda
     * @param width ancho de la hitbox
     * @param height alto de la hitbox
     * @param speed nivel actual de velocidad
     */
    public record ParticipantView(String id, String side, double x, double y,
            double width, double height, int speed) { }

    /** Estado observable de un balón. */
    /**
     * Instantánea inmutable de la posición y velocidad de un balón.
     * @param index índice del balón en la cancha
     * @param x coordenada horizontal del centro
     * @param y coordenada vertical del centro
     * @param radius radio del balón
     * @param velocityX velocidad horizontal
     * @param velocityY velocidad vertical
     */
    public record BallView(int index, double x, double y, double radius,
            double velocityX, double velocityY) { }

    /** Instantánea inmutable para dibujar o consultar una sesión sin exponer sus objetos internos. */
    /**
     * Instantánea inmutable del estado observable de una sesión.
     * @param court rectángulo exterior de la cancha
     * @param boundaries límites de la cancha
     * @param goals áreas de gol
     * @param participants participantes registrados
     * @param balls balones contenidos en la cancha
     * @param goalsLeft goles del lado izquierdo
     * @param goalsRight goles del lado derecho
     * @param elapsedSeconds segundos transcurridos
     * @param finished indica si la sesión terminó
     */
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

    /**
     * Limpia la referencia administrada a la sesión única. El singleton
     * continúa existiendo y puede volver a registrarse con startSession.
     */
    public void closeSession() {
        session = null;
    }

    /**
     * Reinicia la ronda de la sesión administrada sin reemplazar la instancia
     * ni modificar marcador o modalidad.
     */
    public void resetSession() {
        if (session != null) session.restartRound();
    }

    /** Mueve por identificador al participante de la sesión única. */
    public void movePlayer(String participantId, boolean direction) {
        GameSession session = requireSession();
        session.movePlayer(findParticipant(participantId), direction);
    }

    /** Ejecuta una patada por identificador y devuelve si el balón estaba al alcance. */
    public boolean kickPlayer(String participantId) {
        GameSession session = requireSession();
        return session.kickPlayer(findParticipant(participantId));
    }

    /** Devuelve una instantánea sin referencias mutables a clases internas del dominio. */
    public SessionView inspectSession() {
        GameSession session = requireSession();
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

    /** Devuelve el nombre de clase concreto de la sesión administrada. */
    public String getSessionType() {
        return requireSession().getClass().getName();
    }

    /** Coloca a un participante para una prueba manual o visual. */
    public void setParticipantPosition(String participantId, double x, double y) {
        findParticipant(participantId).resetPosition(x, y);
    }

    /** Coloca un balón para preparar un escenario reproducible. */
    public void setBallPosition(int ballIndex, double x, double y) {
        requireBall(ballIndex).reposition(x, y);
    }

    /** Aplica un cambio de velocidad al balón indicado. */
    public void applyBallImpulse(int ballIndex, double deltaX, double deltaY) {
        requireBall(ballIndex).applyImpulse(deltaX, deltaY);
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

    /** Interpreta el identificador lado:índice y busca al participante correspondiente. */
    private SessionParticipant findParticipant(String participantId) {
        if (participantId == null) throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        String[] parts = participantId.split(":", -1);
        if (parts.length != 2) throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        GameSession activeSession = requireSession();
        List<SessionParticipant> participants = switch (parts[0]) {
            case "left" -> activeSession.getParticipantsLeft();
            case "right" -> activeSession.getParticipantsRight();
            default -> throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        };
        try {
            return participants.get(Integer.parseInt(parts[1]));
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            throw new GameException(GameException.INVALID_PARTICIPANT_ID, exception);
        }
    }

    /** Copia la información de cada participante de un lado a las instantáneas de presentación. */
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

    /** Avanza un paso fijo de la sesión singleton administrada, si está iniciada. */
    public void updateGame() {
        if (session != null) session.updateGame();
    }
}
