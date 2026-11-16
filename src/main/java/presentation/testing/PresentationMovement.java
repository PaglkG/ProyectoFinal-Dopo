package presentation.testing;

import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import domain.GameSession;
import domain.SessionParticipant;
import domain.SoccerField;

/** Movimiento arcade exclusivo del banco visual; no escribe reglas de movimiento en el dominio. */
final class PresentationMovement {

    static final double STEP_SECONDS = 1.0 / 60.0;
    static final double HORIZONTAL_SPEED = 150.0;
    static final double JUMP_SPEED = 110.0;
    static final double GRAVITY = 180.0;
    private static final double EDGE_GAP = 0.02;

    enum Control {
        LEFT_LEFT, LEFT_RIGHT, LEFT_JUMP,
        RIGHT_LEFT, RIGHT_RIGHT, RIGHT_JUMP
    }

    private final EnumSet<Control> held = EnumSet.noneOf(Control.class);
    private final EnumSet<Control> jumpRequested = EnumSet.noneOf(Control.class);
    private final Map<SessionParticipant, Double> verticalSpeeds =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private GameSession activeSession;

    void setControl(Control control, boolean pressed) {
        if (pressed) {
            boolean firstPress = held.add(control);
            if (firstPress && isJump(control)) jumpRequested.add(control);
        } else {
            held.remove(control);
        }
    }

    void clearInputs() {
        held.clear();
        jumpRequested.clear();
    }

    void reset(GameSession session) {
        clearInputs();
        verticalSpeeds.clear();
        activeSession = session;
    }

    void resetAfterRound(GameSession session) {
        jumpRequested.clear();
        verticalSpeeds.clear();
        activeSession = session;
    }

    void update(GameSession session, MechanicsTestController.SessionMode mode) {
        if (session != activeSession) {
            verticalSpeeds.clear();
            activeSession = session;
        }
        if (session.isFinished()) return;
        SoccerField field = session.getField();
        List<SessionParticipant> left = session.getParticipantsLeft();
        List<SessionParticipant> right = session.getParticipantsRight();
        SessionParticipant leftActor = left.isEmpty() ? null : left.get(0);
        SessionParticipant rightActor = right.isEmpty() ? null : right.get(0);

        // En jugador contra bot, solo se controla al humano situado a la derecha.
        if (mode == MechanicsTestController.SessionMode.PLAYER_VS_PLAYER && leftActor != null) {
            updateActor(leftActor, rightActor, field, Control.LEFT_LEFT, Control.LEFT_RIGHT, Control.LEFT_JUMP);
        }
        if (rightActor != null) {
            updateActor(rightActor, leftActor, field, Control.RIGHT_LEFT, Control.RIGHT_RIGHT, Control.RIGHT_JUMP);
        }
        jumpRequested.clear();
    }

    private void updateActor(SessionParticipant actor, SessionParticipant opponent, SoccerField field,
            Control leftControl, Control rightControl, Control jumpControl) {
        var box = actor.getHitbox();
        double oldX = box.getX();
        double oldY = box.getY();
        int horizontal = (held.contains(rightControl) ? 1 : 0) - (held.contains(leftControl) ? 1 : 0);
        double minX = field.getHitboxField()[2].getX() + field.getHitboxField()[2].getWidth() + EDGE_GAP;
        double maxX = field.getHitboxField()[3].getX() - box.getWidth() - EDGE_GAP;
        double nextX = clamp(oldX + horizontal * HORIZONTAL_SPEED * STEP_SECONDS, minX, maxX);

        actor.resetPosition(nextX, oldY);
        if (touches(opponent, actor)) actor.resetPosition(oldX, oldY);

        double floorY = field.getHitboxField()[1].getY() - box.getHeight();
        double ceilingY = field.getHitboxField()[0].getY() + field.getHitboxField()[0].getHeight() + EDGE_GAP;
        double verticalSpeed = verticalSpeeds.getOrDefault(actor, 0.0);
        boolean grounded = oldY >= floorY - EDGE_GAP;
        if (jumpRequested.contains(jumpControl) && grounded) verticalSpeed = -JUMP_SPEED;
        verticalSpeed += GRAVITY * STEP_SECONDS;
        double nextY = oldY + verticalSpeed * STEP_SECONDS;
        if (nextY >= floorY) {
            nextY = floorY;
            verticalSpeed = 0;
        } else if (nextY <= ceilingY) {
            nextY = ceilingY;
            if (verticalSpeed < 0) verticalSpeed = 0;
        }

        double acceptedX = actor.getHitbox().getX();
        actor.resetPosition(acceptedX, nextY);
        if (touches(opponent, actor)) {
            actor.resetPosition(acceptedX, oldY);
            verticalSpeed = 0;
        }
        verticalSpeeds.put(actor, verticalSpeed);
    }

    private boolean touches(SessionParticipant first, SessionParticipant second) {
        return first != null && first.intersects(second).isContact();
    }

    private boolean isJump(Control control) {
        return control == Control.LEFT_JUMP || control == Control.RIGHT_JUMP;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
