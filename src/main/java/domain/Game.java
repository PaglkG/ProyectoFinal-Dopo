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

    /** Mueve por identidad al jugador seleccionado: true jugador 1, false jugador 2. */
    public void movePlayer(boolean playerOne, boolean direction) {
        GameSession session = requireSession();
        session.movePlayer(findPlayer(playerOne), direction);
    }

    /** Ejecuta una patada por identificador y devuelve si el balón estaba al alcance. */
    public boolean kickPlayer(boolean playerOne) {
        GameSession session = requireSession();
        return session.kickPlayer(findPlayer(playerOne));
    }

    public double getPlayerX(boolean playerOne) { return findPlayer(playerOne).getHitbox().getX(); }
    public double getPlayerY(boolean playerOne) { return findPlayer(playerOne).getHitbox().getY(); }
    public double getPlayerWidth(boolean playerOne) { return findPlayer(playerOne).getHitbox().getWidth(); }
    public double getPlayerHeight(boolean playerOne) { return findPlayer(playerOne).getHitbox().getHeight(); }
    public int getPlayerSpeed(boolean playerOne) { return findPlayer(playerOne).getStats().getSpeed(); }
    public double getBallX(int index) { return requireBall(index).getX(); }
    public double getBallY(int index) { return requireBall(index).getY(); }
    public double getBallRadius(int index) { return requireBall(index).getRadius(); }
    public double getBallVelocityX(int index) { return requireBall(index).getVelocityX(); }
    public double getBallVelocityY(int index) { return requireBall(index).getVelocityY(); }
    public int getBallCount() { return requireSession().getField().getBalls().length; }
    public double getGoalX(int index) { return requireGoal(index).getX(); }
    public double getGoalY(int index) { return requireGoal(index).getY(); }
    public double getGoalWidth(int index) { return requireGoal(index).getWidth(); }
    public double getGoalHeight(int index) { return requireGoal(index).getHeight(); }
    public double getCourtX() { return requireSession().getField().courtX(); }
    public double getCourtY() { return requireSession().getField().courtY(); }
    public double getCourtWidth() { return requireSession().getField().courtWidth(); }
    public double getCourtHeight() { return requireSession().getField().courtHeight(); }
    public double getBoundaryX(int index) { return requireBoundary(index).getX(); }
    public double getBoundaryY(int index) { return requireBoundary(index).getY(); }
    public double getBoundaryWidth(int index) { return requireBoundary(index).getWidth(); }
    public double getBoundaryHeight(int index) { return requireBoundary(index).getHeight(); }
    public int getGoalsLeft() { return requireSession().getGoalsLeft(); }
    public int getGoalsRight() { return requireSession().getGoalsRight(); }
    public double getElapsedSeconds() { return requireSession().getElapsedSeconds(); }
    public boolean isFinished() { return requireSession().isFinished(); }

    /** Estado compatible con el tipo de partida activo; ByTime añade suddenDeath al final. */
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

    private RectangularHitbox requireBoundary(int index) {
        RectangularHitbox[] boundaries = requireSession().getField().getHitboxField();
        if (index < 0 || index >= boundaries.length) throw new GameException(GameException.INVALID_BALL_INDEX);
        return boundaries[index];
    }

    private RectangularHitbox requireGoal(int index) {
        RectangularHitbox[] goals = requireSession().getField().getGoalHitboxes();
        if (index < 0 || index >= goals.length) throw new GameException(GameException.INVALID_BALL_INDEX);
        return goals[index];
    }

    private SessionParticipant findPlayer(boolean playerOne) {
        GameSession active = requireSession();
        int expectedId = playerOne ? 1 : 2;
        for (SessionParticipant participant : active.getParticipantsLeft()) {
            if (participant instanceof Player player && player.getUserId() == expectedId) return participant;
        }
        for (SessionParticipant participant : active.getParticipantsRight()) {
            if (participant instanceof Player player && player.getUserId() == expectedId) return participant;
        }
        // In player-vs-bot, the bot occupies the identity not selected for the human.
        if (active instanceof PlayerVsBot) {
            SessionParticipant bot = active.getParticipantsLeft().get(0);
            SessionParticipant human = active.getParticipantsRight().get(0);
            boolean humanIsPlayerOne = human instanceof Player p && p.getUserId() == 1;
            if (playerOne != humanIsPlayerOne) return bot;
        }
        throw new GameException(GameException.INVALID_PARTICIPANT_ID);
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
    public void setPlayerPosition(boolean playerOne, double x, double y) {
        findPlayer(playerOne).resetPosition(x, y);
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
    /** Copia la información de cada participante de un lado a las instantáneas de presentación. */
    private void addParticipantViews(List<ParticipantView> target, String side,
            List<SessionParticipant> participants) {
        for (int index = 0; index < participants.size(); index++) {
            var participant = participants.get(index);
            var box = participant.getHitbox();
            target.add(new ParticipantView(side,
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
