package presentation.testing;

import java.util.List;

import domain.Game;

/** Coordina escenarios reproducibles usando únicamente la fachada {@link Game}. */
public final class MechanicsTestController {

    public enum MatchMode {
        GOALS("Por goles"), TIME("Por tiempo");
        private final String label;
        MatchMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum SessionMode {
        PLAYER_VS_PLAYER("Dos jugadores"), PLAYER_VS_BOT("Jugador contra bot");
        private final String label;
        SessionMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum Scenario {
        LEFT_GOAL, RIGHT_GOAL, PLAYER_BALL_CONTACT, GROUND_BOUNCE,
        SAFE_FIELD_CONTACT, BALL_SIDE_REBOUND, PARTICIPANT_OUTSIDE,
        PARTICIPANT_TOUCHES_SIDE, PARTICIPANTS_OVERLAP, SUDDEN_DEATH_WIN,
        KICK_OPPONENT
    }

    private Game game;
    private SessionMode selectedSessionMode = SessionMode.PLAYER_VS_PLAYER;
    private MatchMode selectedMatchMode = MatchMode.GOALS;
    private int selectedLimit = 5;
    private int activeSessionIndex;
    private final PresentationMovement movement = new PresentationMovement();

    /** Inicia el banco con una sesión local de dos jugadores y límite de cinco goles. */
    public MechanicsTestController() {
        startNewSession(SessionMode.PLAYER_VS_PLAYER, MatchMode.GOALS, 5);
    }

    /** Guarda las opciones y crea una sesión nueva con esos valores. */
    public void startNewSession(SessionMode sessionMode, MatchMode matchMode, int limit) {
        selectedSessionMode = sessionMode;
        selectedMatchMode = matchMode;
        selectedLimit = Math.max(0, limit);
        createSession(sessionMode, matchMode, selectedLimit);
    }

    private void createSession(SessionMode sessionMode, MatchMode matchMode, int limit) {
        game = new Game();
        String sessionClass = sessionMode == SessionMode.PLAYER_VS_BOT
                ? "domain.PlayerVsBot" : "domain.PlayerVsPlayer";
        String ruleClass = matchMode == MatchMode.TIME ? "domain.rules.ByTime" : "domain.rules.ByGoals";
        List<String> selectedPowers = sessionMode == SessionMode.PLAYER_VS_BOT
                ? List.of("domain.powers.AuraAzul", "domain.powers.Rayo", "Clumsy")
                : List.of("domain.powers.AuraAzul", "domain.powers.Rayo");
        activeSessionIndex = game.startSessionForView(List.of(Integer.toString(limit)), selectedPowers,
                sessionClass, ruleClass, true);
        movement.reset(game, activeSessionIndex);
    }

    /** Ejecuta un paso del movimiento visual y uno de la lógica del dominio. */
    public void step() {
        Game.SessionView before = game.inspectSession(activeSessionIndex);
        if (before.finished()) return;
        movement.update(game, activeSessionIndex, selectedSessionMode);
        advanceDomainOnly(before);
    }

    private void advanceDomainOnly(Game.SessionView before) {
        game.updateGame(activeSessionIndex);
        Game.SessionView after = game.inspectSession(activeSessionIndex);
        if (after.goalsLeft() != before.goalsLeft() || after.goalsRight() != before.goalsRight()) {
            movement.resetAfterRound(game, activeSessionIndex);
        }
    }

    /** @param control tecla lógica que cambia de estado @param pressed indica si permanece presionada */
    public void setMovementControl(PresentationMovement.Control control, boolean pressed) {
        movement.setControl(control, pressed);
    }

    /** @return {@code true} si la patada del jugador izquierdo alcanzó el balón */
    public boolean kickLeft() {
        return selectedSessionMode == SessionMode.PLAYER_VS_PLAYER
                && game.kickPlayer(activeSessionIndex, "left:0");
    }

    /** @return {@code true} si la patada del jugador derecho alcanzó el balón */
    public boolean kickRight() {
        return game.kickPlayer(activeSessionIndex, "right:0");
    }

    /** Prepara el escenario solicitado a través de las operaciones de {@link Game}. */
    public void runScenario(Scenario scenario) {
        createSession(selectedSessionMode, selectedMatchMode, selectedLimit);
        neutralizeBall();
        Game.SessionView state = game.inspectSession(activeSessionIndex);
        Game.BallView ball = state.balls().get(0);
        List<Game.RectangleView> walls = state.boundaries();

        switch (scenario) {
            case LEFT_GOAL -> placeInGoal(0);
            case RIGHT_GOAL -> placeInGoal(1);
            case PLAYER_BALL_CONTACT -> {
                Game.ParticipantView player = participant(state, "left");
                if (player == null) return;
                game.setBallPosition(activeSessionIndex, ball.index(),
                        player.x() + player.width() / 2, player.y() + player.height() / 2);
            }
            case GROUND_BOUNCE -> {
                game.setBallPosition(activeSessionIndex, ball.index(),
                        walls.get(0).x() + walls.get(0).width() / 2,
                        walls.get(1).y() - ball.radius());
                game.applyBallImpulse(activeSessionIndex, ball.index(), 0, 8);
            }
            case SAFE_FIELD_CONTACT -> game.setBallPosition(activeSessionIndex, ball.index(),
                    walls.get(2).x() + ball.radius(), walls.get(0).y() + 15);
            case BALL_SIDE_REBOUND -> {
                game.setBallPosition(activeSessionIndex, ball.index(),
                        walls.get(2).x() - 3, walls.get(0).y() + walls.get(0).height() + 30);
                game.applyBallImpulse(activeSessionIndex, ball.index(), -10, 0);
            }
            case PARTICIPANT_OUTSIDE -> {
                Game.ParticipantView player = participant(state, "left");
                if (player == null) return;
                game.setParticipantPosition(activeSessionIndex, player.id(),
                        walls.get(2).x() - player.width() - 1, player.y());
            }
            case PARTICIPANT_TOUCHES_SIDE -> {
                Game.ParticipantView player = participant(state, "left");
                if (player == null) return;
                game.setParticipantPosition(activeSessionIndex, player.id(),
                        walls.get(2).x() + walls.get(2).width(), player.y());
            }
            case PARTICIPANTS_OVERLAP -> {
                Game.ParticipantView left = participant(state, "left");
                Game.ParticipantView right = participant(state, "right");
                if (left == null || right == null) return;
                game.setParticipantPosition(activeSessionIndex, right.id(), left.x(), left.y());
            }
            case KICK_OPPONENT -> {
                Game.ParticipantView left = participant(state, "left");
                Game.ParticipantView right = participant(state, "right");
                if (left == null || right == null) return;
                double center = state.court().x() + state.court().width() / 2;
                double gap = 8;
                double leftX = center - left.width() - gap / 2;
                double rightX = center + gap / 2;
                game.setParticipantPosition(activeSessionIndex, left.id(), leftX, left.y());
                game.setParticipantPosition(activeSessionIndex, right.id(), rightX, right.y());
            }
            case SUDDEN_DEATH_WIN -> {
                createSession(selectedSessionMode, MatchMode.TIME, 0);
                step();
                placeInGoal(0);
            }
        }
        advanceDomainOnly(game.inspectSession(activeSessionIndex));
    }

    private void placeInGoal(int goalIndex) {
        Game.SessionView state = game.inspectSession(activeSessionIndex);
        Game.RectangleView goal = state.goals().get(goalIndex);
        game.setBallPosition(activeSessionIndex, 0,
                goal.x() + goal.width() / 2, goal.y() + goal.height() / 2);
    }

    private void neutralizeBall() {
        Game.BallView ball = game.inspectSession(activeSessionIndex).balls().get(0);
        game.applyBallImpulse(activeSessionIndex, ball.index(), -ball.velocityX(), -ball.velocityY());
    }

    private static Game.ParticipantView participant(Game.SessionView state, String side) {
        return state.participants().stream()
                .filter(player -> player.side().equals(side))
                .findFirst()
                .orElse(null);
    }

    /** Devuelve la instantánea actual de la sesión para presentación. */
    public Game.SessionView getSessionView() {
        return game.inspectSession(activeSessionIndex);
    }
}
