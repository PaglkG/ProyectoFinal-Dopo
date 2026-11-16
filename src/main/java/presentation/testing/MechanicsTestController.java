package presentation.testing;

import java.util.List;

import domain.Ball;
import domain.Game;
import domain.GameSession;
import domain.PlayerVsBot;
import domain.PlayerVsPlayer;
import domain.SessionParticipant;
import domain.SoccerField;
import domain.exception.GameException;
import domain.rules.ByGoals;
import domain.rules.ByTime;

/** Coordina escenarios reproducibles con las clases reales del dominio. */
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
        SAFE_FIELD_CONTACT, BALL_OUTSIDE, PARTICIPANT_OUTSIDE,
        PARTICIPANT_TOUCHES_SIDE, PARTICIPANTS_OVERLAP, SUDDEN_DEATH_WIN
    }

    private Game game;
    private GameSession session;
    private SessionMode selectedSessionMode = SessionMode.PLAYER_VS_PLAYER;
    private MatchMode selectedMatchMode = MatchMode.GOALS;
    private int selectedLimit = 5;
    private final PresentationMovement movement = new PresentationMovement();

    public MechanicsTestController() {
        startNewSession(SessionMode.PLAYER_VS_PLAYER, MatchMode.GOALS, 5);
    }

    public void startNewSession(SessionMode sessionMode, MatchMode matchMode, int limit) {
        selectedSessionMode = sessionMode;
        selectedMatchMode = matchMode;
        selectedLimit = Math.max(0, limit);
        createSession(sessionMode, matchMode, selectedLimit);
    }

    private void createSession(SessionMode sessionMode, MatchMode matchMode, int limit) {
        game = new Game();
        String sessionClass = sessionMode == SessionMode.PLAYER_VS_BOT
                ? PlayerVsBot.class.getName() : PlayerVsPlayer.class.getName();
        String ruleClass = matchMode == MatchMode.TIME ? ByTime.class.getName() : ByGoals.class.getName();
        session = game.startSession(List.of(Integer.toString(limit)), sessionClass, ruleClass, true);
        movement.reset(session);
    }

    public void step() {
        if (session.isFinished()) return;
        int previousLeftGoals = session.getGoalsLeft();
        int previousRightGoals = session.getGoalsRight();
        movement.update(session, selectedSessionMode);
        game.updateGame(session);
        if (session.getGoalsLeft() != previousLeftGoals || session.getGoalsRight() != previousRightGoals) {
            movement.resetAfterRound(session);
        }
    }

    void setMovementControl(PresentationMovement.Control control, boolean pressed) {
        movement.setControl(control, pressed);
    }

    /** Patea hacia el arco derecho si el balón está al alcance del jugador izquierdo. */
    boolean kickLeft() {
        if (selectedSessionMode != SessionMode.PLAYER_VS_PLAYER) return false;
        return kick(session.getParticipantsLeft());
    }

    /** Patea hacia el arco izquierdo si el balón está al alcance del jugador derecho. */
    boolean kickRight() {
        return kick(session.getParticipantsRight());
    }

    private boolean kick(List<SessionParticipant> participants) {
        if (participants.isEmpty()) return false;
        return session.kickPlayer(participants.get(0));
    }

    /** Prepara y avanza exactamente un escenario usando la actualización real de la sesión. */
    public void runScenario(Scenario scenario) {
        // Cada botón comienza desde un estado limpio para que el escenario sea repetible.
        createSession(selectedSessionMode, selectedMatchMode, selectedLimit);
        SoccerField field = session.getField();
        Ball ball = field.getBalls()[0];
        neutralizeVelocity(ball);

        switch (scenario) {
            case LEFT_GOAL -> placeInGoal(field, ball, 0);
            case RIGHT_GOAL -> placeInGoal(field, ball, 1);
            case PLAYER_BALL_CONTACT -> {
                SessionParticipant participant = session.getParticipantsLeft().get(0);
                ball.reposition(participant.getHitbox().getX() + participant.getHitbox().getWidth() / 2,
                        participant.getHitbox().getY() + participant.getHitbox().getHeight() / 2);
            }
            case GROUND_BOUNCE -> {
                ball.reposition(field.getHitboxField()[0].getX() + field.getHitboxField()[0].getWidth() / 2,
                        field.getHitboxField()[1].getY() - ball.getRadius());
                ball.applyImpulse(0, 8);
            }
            case SAFE_FIELD_CONTACT -> {
                double left = field.getHitboxField()[2].getX();
                ball.reposition(left + ball.getRadius(), field.getHitboxField()[0].getY() + 15);
            }
            case BALL_OUTSIDE -> ball.reposition(
                    field.getHitboxField()[2].getX() - ball.getRadius() - 1,
                    field.getHitboxField()[0].getY() + field.getHitboxField()[0].getHeight() + 20);
            case PARTICIPANT_OUTSIDE -> {
                SessionParticipant participant = session.getParticipantsLeft().get(0);
                participant.resetPosition(field.getHitboxField()[2].getX() - participant.getHitbox().getWidth() - 1,
                        field.getHitboxField()[0].getY() + 30);
            }
            case PARTICIPANT_TOUCHES_SIDE -> {
                SessionParticipant participant = session.getParticipantsLeft().get(0);
                participant.resetPosition(field.getHitboxField()[2].getX() + field.getHitboxField()[2].getWidth(),
                        participant.getHitbox().getY());
            }
            case PARTICIPANTS_OVERLAP -> {
                SessionParticipant left = session.getParticipantsLeft().get(0);
                SessionParticipant right = session.getParticipantsRight().get(0);
                right.resetPosition(left.getHitbox().getX(), left.getHitbox().getY());
            }
            case SUDDEN_DEATH_WIN -> {
                createSession(selectedSessionMode, MatchMode.TIME, 0);
                step(); // Empate con tiempo agotado: continúa en muerte súbita.
                placeInGoal(session.getField(), session.getField().getBalls()[0], 0);
            }
        }
        step();
    }

    private static void placeInGoal(SoccerField field, Ball ball, int goalIndex) {
        var goal = field.getGoalHitboxes()[goalIndex];
        ball.reposition(goal.getX() + goal.getWidth() / 2, goal.getY() + goal.getHeight() / 2);
    }

    private static void neutralizeVelocity(Ball ball) {
        ball.applyImpulse(-ball.getVelocityX(), -ball.getVelocityY());
    }

    public GameSession getSession() { return session; }
}
