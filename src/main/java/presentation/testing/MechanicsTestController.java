package presentation.testing;
import domain.BallView;
import domain.ParticipantView;
import domain.RectangleView;
import domain.SessionView;

import java.io.File;
import java.util.List;

import domain.Game;

/** Coordina escenarios reproducibles usando únicamente la fachada {@link Game}. */
public final class MechanicsTestController {

    /** Modalidades disponibles en el formulario del banco de pruebas. */
    public enum MatchMode {
        /** Partido con límite de goles. */
        GOALS("Por goles"),
        /** Partido con límite de tiempo. */
        TIME("Por tiempo");
        private final String label;
        MatchMode(String label) { this.label = label; }
        /** Devuelve la etiqueta que se muestra en los controles de Swing. */
        @Override
        public String toString() { return label; }
    }

    /** Combinaciones de participantes disponibles para el banco de pruebas. */
    public enum SessionMode {
        /** Partida local entre dos participantes humanos. */
        PLAYER_VS_PLAYER("Dos jugadores"),
        /** Partida entre una persona y un bot. */
        PLAYER_VS_BOT("Jugador contra bot");
        private final String label;
        SessionMode(String label) { this.label = label; }
        /** Devuelve la etiqueta que se muestra en los controles de Swing. */
        @Override
        public String toString() { return label; }
    }

    /** Escenarios reproducibles que se pueden preparar desde el banco. */
    public enum Scenario {
        /** Gol en el arco izquierdo. */
        LEFT_GOAL,
        /** Gol en el arco derecho. */
        RIGHT_GOAL,
        /** Contacto inicial entre un participante y el balón. */
        PLAYER_BALL_CONTACT,
        /** Rebote del balón contra el suelo. */
        GROUND_BOUNCE,
        /** Contacto seguro del balón con una franja de cancha. */
        SAFE_FIELD_CONTACT,
        /** Rebote contra un límite lateral. */
        BALL_SIDE_REBOUND,
        /** Colisión entre dos balones. */
        BALL_BALL_COLLISION,
        /** Participante colocado fuera de la cancha. */
        PARTICIPANT_OUTSIDE,
        /** Participante en contacto con un límite lateral prohibido. */
        PARTICIPANT_TOUCHES_SIDE,
        /** Participantes colocados en superposición. */
        PARTICIPANTS_OVERLAP,
        /** Victoria después de resolver un empate en muerte súbita. */
        SUDDEN_DEATH_WIN,
        /** Distancia de patada al rival. */
        KICK_OPPONENT
    }

    private Game game;
    private SessionMode selectedSessionMode = SessionMode.PLAYER_VS_PLAYER;
    private MatchMode selectedMatchMode = MatchMode.GOALS;
    private int selectedLimit = 5;
    private MechanicsTestConfiguration configuration;
    private final PresentationMovement movement = new PresentationMovement();

    /** Inicia el banco con una sesión local de dos jugadores y límite de cinco goles. */
    public MechanicsTestController() {
        this(MechanicsTestConfiguration.defaults());
    }

    /** Inicia el controlador con una configuración extensible elegida en la pantalla inicial.
     * @param configuration configuración de sesión seleccionada
     */
    public MechanicsTestController(MechanicsTestConfiguration configuration) {
        this.configuration = configuration;
        selectedSessionMode = configuration.sessionMode();
        selectedMatchMode = configuration.matchMode();
        selectedLimit = configuration.limit();
        createConfiguredSession();
    }

    /** Guarda las opciones solicitadas y se asocia a la sesión singleton existente o inicial. */
    /** Solicita crear o reutilizar la sesión con las opciones del banco.
     * @param sessionMode modalidad de participantes
     * @param matchMode modalidad de partido
     * @param limit límite de goles o minutos
     */
    public void startNewSession(SessionMode sessionMode, MatchMode matchMode, int limit) {
        selectedSessionMode = sessionMode;
        selectedMatchMode = matchMode;
        selectedLimit = Math.max(0, limit);
        configuration = new MechanicsTestConfiguration(sessionMode, matchMode, selectedLimit,
                sessionMode == SessionMode.PLAYER_VS_BOT ? "domain.PlayerVsBot" : "domain.PlayerVsPlayer",
                matchMode == MatchMode.TIME ? "domain.ByTime" : "domain.ByGoals",
                List.of("150", "450", Integer.toString(selectedLimit)), "domain.BlueAura",
                "domain.Lightning", "Clumsy", true);
        createConfiguredSession();
    }

    /** Reinicia el balón y las posiciones en la sesión única ya creada. */
    public void restartConfiguredSession() {
        game.resetSession();
        movement.reset(game);
    }

    /** Guarda el estado actual de la sesión en el archivo indicado.
     * @param file destino del guardado
     */
    public void save(File file) {
        game.save(file);
    }

    /** Abre una sesión serializada y vuelve a asociar el movimiento visual.
     * @param file archivo con la partida guardada
     */
    public void open(File file) {
        game.open(file);
        movement.reset(game);
    }

    /** Obtiene la sesión singleton a través de Game y alinea los controles con su modalidad real. */
    private void createConfiguredSession() {
        game = new Game();
        List<String> powers = configuration.sessionMode() == SessionMode.PLAYER_VS_BOT
                ? List.of(configuration.rightPowerClass(), configuration.leftPowerClass(), configuration.botProfile())
                : List.of(configuration.rightPowerClass(), configuration.leftPowerClass());
        game.startSession(configuration.ruleParameters(), powers,
                configuration.sessionClass(), configuration.matchClass(), configuration.playerOne());
        String actualSessionClass = game.getSessionType();
        selectedSessionMode = actualSessionClass.equals("domain.PlayerVsBot")
                ? SessionMode.PLAYER_VS_BOT : SessionMode.PLAYER_VS_PLAYER;
        if (!actualSessionClass.equals(configuration.sessionClass())) {
            configuration = new MechanicsTestConfiguration(selectedSessionMode, selectedMatchMode,
                    selectedLimit, actualSessionClass, configuration.matchClass(),
                    configuration.ruleParameters(), configuration.rightPowerClass(),
                    configuration.leftPowerClass(), configuration.botProfile(), configuration.playerOne());
        }
        movement.reset(game);
    }

    /** Ejecuta un paso del movimiento visual y uno de la lógica del dominio. */
    public void step() {
        SessionView before = state();
        if (before.finished()) return;
        movement.update(game, selectedSessionMode,
                selectedSessionMode == SessionMode.PLAYER_VS_BOT ? configuration.playerOne() : true);
        advanceDomainOnly(before);
    }

    /** Avanza la lógica del dominio y reinicia el movimiento visual cuando cambió el marcador. */
    private void advanceDomainOnly(SessionView before) {
        game.updateGame();
        SessionView after = state();
        if (after.goalsLeft() != before.goalsLeft() || after.goalsRight() != before.goalsRight()) {
            movement.resetAfterRound(game);
        }
    }

    /** Actualiza el estado de una tecla de movimiento o salto.
     * @param control control lógico que cambia de estado
     * @param pressed {@code true} si la tecla permanece presionada
     */
    public void setMovementControl(PresentationMovement.Control control, boolean pressed) {
        movement.setControl(control, pressed);
    }

    /** Intenta patear con el lado izquierdo en una sesión entre dos personas.
     * @return {@code true} si la patada alcanzó un objetivo
     */
    public boolean kickLeft() {
        return configuration.sessionMode() == SessionMode.PLAYER_VS_PLAYER
                && game.kickPlayer(false);
    }

    /** Intenta patear con el participante del lado derecho.
     * @return {@code true} si la patada alcanzó un objetivo
     */
    public boolean kickRight() {
        return game.kickPlayer(configuration.sessionMode() == SessionMode.PLAYER_VS_BOT
                ? configuration.playerOne() : true);
    }

    /** Prepara el escenario solicitado a través de las operaciones de {@link Game}.
     * @param scenario escenario reproducible que se preparará
     */
    public void runScenario(Scenario scenario) {
        createConfiguredSession();
        neutralizeBall();
        SessionView state = state();
        BallView ball = state.balls().get(0);
        List<RectangleView> walls = state.boundaries();

        switch (scenario) {
            case LEFT_GOAL -> placeInGoal(0);
            case RIGHT_GOAL -> placeInGoal(1);
            case PLAYER_BALL_CONTACT -> {
                ParticipantView player = participant(state, "left");
                if (player == null) return;
                game.setBallPosition(ball.index(),
                        player.x() + player.width() / 2, player.y() + player.height() / 2);
            }
            case GROUND_BOUNCE -> {
                game.setBallPosition(ball.index(),
                        walls.get(0).x() + walls.get(0).width() / 2,
                        walls.get(1).y() - ball.radius());
                game.applyBallImpulse(ball.index(), 0, 8);
            }
            case SAFE_FIELD_CONTACT -> game.setBallPosition(ball.index(),
                    walls.get(2).x() + ball.radius(), walls.get(0).y() + 15);
            case BALL_SIDE_REBOUND -> {
                game.setBallPosition(ball.index(),
                        walls.get(2).x() - 3, walls.get(0).y() + walls.get(0).height() + 30);
                game.applyBallImpulse(ball.index(), -10, 0);
            }
            case BALL_BALL_COLLISION -> {
                if (state.balls().size() < 2) {
                    return;
                }
                BallView first = state.balls().get(0);
                BallView second = state.balls().get(1);
                double contactDistance = first.radius() + second.radius();
                game.setBallPosition(first.index(), 225 - contactDistance / 2, 70);
                game.setBallPosition(second.index(), 225 + contactDistance / 2, 70);
                game.applyBallImpulse(first.index(), 15, 0);
                game.applyBallImpulse(second.index(), -15, 0);
            }
            case PARTICIPANT_OUTSIDE -> {
                ParticipantView player = participant(state, "left");
                if (player == null) return;
                game.setPlayerPosition(playerOneForSide("left"),
                        walls.get(2).x() - player.width() - 1, player.y());
            }
            case PARTICIPANT_TOUCHES_SIDE -> {
                ParticipantView player = participant(state, "left");
                if (player == null) return;
                game.setPlayerPosition(playerOneForSide("left"),
                        walls.get(2).x() + walls.get(2).width(), player.y());
            }
            case PARTICIPANTS_OVERLAP -> {
                ParticipantView left = participant(state, "left");
                ParticipantView right = participant(state, "right");
                if (left == null || right == null) return;
                game.setPlayerPosition(playerOneForSide("right"), left.x(), left.y());
            }
            case KICK_OPPONENT -> {
                ParticipantView left = participant(state, "left");
                ParticipantView right = participant(state, "right");
                if (left == null || right == null) return;
                double center = state.court().x() + state.court().width() / 2;
                double gap = 8;
                double leftX = center - left.width() - gap / 2;
                double rightX = center + gap / 2;
                game.setPlayerPosition(playerOneForSide("left"), leftX, left.y());
                game.setPlayerPosition(playerOneForSide("right"), rightX, right.y());
            }
            case SUDDEN_DEATH_WIN -> {
                configuration = new MechanicsTestConfiguration(selectedSessionMode, MatchMode.TIME, 0,
                        configuration.sessionClass(), "domain.ByTime", List.of("150", "450", "0"),
                        configuration.rightPowerClass(), configuration.leftPowerClass(),
                        configuration.botProfile(), configuration.playerOne());
                createConfiguredSession();
                step();
                placeInGoal(0);
            }
        }
        advanceDomainOnly(state());
    }

    /** Coloca el balón en el centro del arco indicado para preparar un gol. */
    private void placeInGoal(int goalIndex) {
        SessionView state = state();
        RectangleView goal = state.goals().get(goalIndex);
        game.setBallPosition(0,
                goal.x() + goal.width() / 2, goal.y() + goal.height() / 2);
    }

    /** Cancela la velocidad actual del balón antes de ubicar un escenario. */
    private void neutralizeBall() {
        BallView ball = state().balls().get(0);
        game.applyBallImpulse(ball.index(), -ball.velocityX(), -ball.velocityY());
    }

    /** Busca la instantánea del primer participante del lado indicado. */
    private static ParticipantView participant(SessionView state, String side) {
        return state.participants().stream()
                .filter(player -> player.side().equals(side))
                .findFirst()
                .orElse(null);
    }

    /** Devuelve la instantánea actual de la sesión para presentación.
     * @return estado inmutable de la sesión
     */
    public SessionView getSessionView() {
        return state();
    }

    private boolean playerOneForSide(String side) {
        if (selectedSessionMode == SessionMode.PLAYER_VS_BOT) {
            return side.equals("right") ? configuration.playerOne() : !configuration.playerOne();
        }
        return side.equals("right");
    }

    private SessionView state() {
        return state(game, selectedSessionMode == SessionMode.PLAYER_VS_BOT ? configuration.playerOne() : true);
    }

    /** Construye la instantánea de presentación usando solo consultas de la fachada.
     * @param game fachada que entrega los datos actuales
     * @param playerOneOnRight indica si el jugador 1 ocupa el lado derecho
     * @return estado inmutable preparado para presentar
     */
    public static SessionView state(Game game, boolean playerOneOnRight) {
        List<RectangleView> boundaries = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) boundaries.add(new RectangleView(game.getBoundaryX(i),
                game.getBoundaryY(i), game.getBoundaryWidth(i), game.getBoundaryHeight(i)));
        List<RectangleView> goals = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) goals.add(new RectangleView(game.getGoalX(i), game.getGoalY(i),
                game.getGoalWidth(i), game.getGoalHeight(i)));
        List<ParticipantView> players = List.of(
                new ParticipantView("left", game.getPlayerX(!playerOneOnRight), game.getPlayerY(!playerOneOnRight),
                        game.getPlayerWidth(!playerOneOnRight), game.getPlayerHeight(!playerOneOnRight), game.getPlayerSpeed(!playerOneOnRight)),
                new ParticipantView("right", game.getPlayerX(playerOneOnRight), game.getPlayerY(playerOneOnRight),
                        game.getPlayerWidth(playerOneOnRight), game.getPlayerHeight(playerOneOnRight), game.getPlayerSpeed(playerOneOnRight)));
        List<BallView> balls = new java.util.ArrayList<>();
        for (int i = 0; i < game.getBallCount(); i++) balls.add(new BallView(i, game.getBallX(i), game.getBallY(i),
                game.getBallRadius(i), game.getBallVelocityX(i), game.getBallVelocityY(i)));
        RectangleView court = new RectangleView(game.getCourtX(), game.getCourtY(), game.getCourtWidth(), game.getCourtHeight());
        List<String> match = game.getMatchState();
        return new SessionView(court, boundaries, goals, players, balls, Integer.parseInt(match.get(0)),
                Integer.parseInt(match.get(1)), game.getElapsedSeconds(), game.isFinished());
    }
}
