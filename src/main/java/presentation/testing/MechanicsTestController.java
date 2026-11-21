package presentation.testing;
import domain.BallView;
import domain.ParticipantView;
import domain.RectangleView;
import domain.SessionView;

import java.util.List;

import domain.Game;

/** Coordina escenarios reproducibles usando únicamente la fachada {@link Game}. */
public final class MechanicsTestController {

    public enum MatchMode {
        GOALS("Por goles"), TIME("Por tiempo");
        private final String label;
        MatchMode(String label) { this.label = label; }
        /** Devuelve la etiqueta que se muestra en los controles de Swing. */
        @Override
        public String toString() { return label; }
    }

    public enum SessionMode {
        PLAYER_VS_PLAYER("Dos jugadores"), PLAYER_VS_BOT("Jugador contra bot");
        private final String label;
        SessionMode(String label) { this.label = label; }
        /** Devuelve la etiqueta que se muestra en los controles de Swing. */
        @Override
        public String toString() { return label; }
    }

    public enum Scenario {
        LEFT_GOAL, RIGHT_GOAL, PLAYER_BALL_CONTACT, GROUND_BOUNCE,
        SAFE_FIELD_CONTACT, BALL_SIDE_REBOUND, BALL_BALL_COLLISION, PARTICIPANT_OUTSIDE,
        PARTICIPANT_TOUCHES_SIDE, PARTICIPANTS_OVERLAP, SUDDEN_DEATH_WIN,
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

    /** Inicia el controlador con una configuración extensible elegida en la pantalla inicial. */
    public MechanicsTestController(MechanicsTestConfiguration configuration) {
        this.configuration = configuration;
        selectedSessionMode = configuration.sessionMode();
        selectedMatchMode = configuration.matchMode();
        selectedLimit = configuration.limit();
        createConfiguredSession();
    }

    /** Guarda las opciones solicitadas y se asocia a la sesión singleton existente o inicial. */
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

    /** @param control tecla lógica que cambia de estado @param pressed indica si permanece presionada */
    public void setMovementControl(PresentationMovement.Control control, boolean pressed) {
        movement.setControl(control, pressed);
    }

    /** @return {@code true} si la patada del jugador izquierdo alcanzó el balón */
    public boolean kickLeft() {
        return configuration.sessionMode() == SessionMode.PLAYER_VS_PLAYER
                && game.kickPlayer(false);
    }

    /** @return {@code true} si la patada del jugador derecho alcanzó el balón */
    public boolean kickRight() {
        return game.kickPlayer(configuration.sessionMode() == SessionMode.PLAYER_VS_BOT
                ? configuration.playerOne() : true);
    }

    /** Prepara el escenario solicitado a través de las operaciones de {@link Game}. */
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

    /** Devuelve la instantánea actual de la sesión para presentación. */
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

    /** Builds the presentation snapshot exclusively from primitive Game queries. */
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
