package presentation.testing;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import domain.Game;

/** Controla entradas visuales a través de la fachada {@link Game}. */
final class PresentationMovement {

    static final double STEP_SECONDS = 1.0 / 60.0;
    static final double JUMP_SPEED = 110.0;
    static final double GRAVITY = 180.0;
    private static final double EDGE_GAP = 0.02;

    enum Control {
        LEFT_LEFT, LEFT_RIGHT, LEFT_JUMP,
        RIGHT_LEFT, RIGHT_RIGHT, RIGHT_JUMP
    }

    private final EnumSet<Control> held = EnumSet.noneOf(Control.class);
    private final EnumSet<Control> jumpRequested = EnumSet.noneOf(Control.class);
    private final Map<String, Double> verticalSpeeds = Collections.synchronizedMap(new HashMap<>());
    private Game activeGame;
    private int activeSessionIndex = -1;

    public void setControl(Control control, boolean pressed) {
        if (pressed) {
            boolean firstPress = held.add(control);
            if (firstPress && isJump(control)) jumpRequested.add(control);
        } else {
            held.remove(control);
        }
    }

    public void clearInputs() {
        held.clear();
        jumpRequested.clear();
    }

    public void reset(Game game, int sessionIndex) {
        clearInputs();
        verticalSpeeds.clear();
        activeGame = game;
        activeSessionIndex = sessionIndex;
    }

    public void resetAfterRound(Game game, int sessionIndex) {
        jumpRequested.clear();
        verticalSpeeds.clear();
        activeGame = game;
        activeSessionIndex = sessionIndex;
    }

    public void update(Game game, int sessionIndex, MechanicsTestController.SessionMode mode) {
        if (game != activeGame || sessionIndex != activeSessionIndex) {
            verticalSpeeds.clear();
            activeGame = game;
            activeSessionIndex = sessionIndex;
        }
        Game.SessionView state = game.inspectSession(sessionIndex);
        if (state.finished()) return;

        Game.ParticipantView left = findBySide(state, "left");
        Game.ParticipantView right = findBySide(state, "right");
        if (mode == MechanicsTestController.SessionMode.PLAYER_VS_PLAYER && left != null) {
            updateActor(game, sessionIndex, left, right,
                    Control.LEFT_LEFT, Control.LEFT_RIGHT, Control.LEFT_JUMP);
        }
        if (right != null) {
            Game.SessionView current = game.inspectSession(sessionIndex);
            right = findBySide(current, "right");
            left = findBySide(current, "left");
            updateActor(game, sessionIndex, right, left,
                    Control.RIGHT_LEFT, Control.RIGHT_RIGHT, Control.RIGHT_JUMP);
        }
        jumpRequested.clear();
    }

    /** Usa el movimiento real del dominio y limita solo la simulación visual del salto. */
    private void updateActor(Game game, int sessionIndex, Game.ParticipantView actor,
            Game.ParticipantView opponent, Control leftControl, Control rightControl, Control jumpControl) {
        String id = actor.id();
        double oldX = actor.x();
        double oldY = actor.y();
        int horizontal = (held.contains(rightControl) ? 1 : 0) - (held.contains(leftControl) ? 1 : 0);
        double nextX = oldX;
        if (horizontal != 0) {
            game.movePlayer(sessionIndex, id, horizontal > 0);
            Game.ParticipantView moved = findById(game.inspectSession(sessionIndex), id);
            if (moved == null) return;
            var boundaries = game.inspectSession(sessionIndex).boundaries();
            double minX = boundaries.get(2).x() + boundaries.get(2).width() + EDGE_GAP;
            double maxX = boundaries.get(3).x() - moved.width() - EDGE_GAP;
            nextX = clamp(moved.x(), minX, maxX);
            if (touches(nextX, oldY, moved.width(), moved.height(), opponent)) nextX = oldX;
            if (nextX != moved.x()) game.setParticipantPosition(sessionIndex, id, nextX, oldY);
        }

        Game.ParticipantView positioned = findById(game.inspectSession(sessionIndex), id);
        if (positioned == null) return;
        var boundaries = game.inspectSession(sessionIndex).boundaries();
        double floorY = boundaries.get(1).y() - positioned.height();
        double ceilingY = boundaries.get(0).y() + boundaries.get(0).height() + EDGE_GAP;
        double verticalSpeed = verticalSpeeds.getOrDefault(id, 0.0);
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
        if (touches(nextX, nextY, positioned.width(), positioned.height(), opponent)) {
            nextY = oldY;
            verticalSpeed = 0;
        }
        game.setParticipantPosition(sessionIndex, id, nextX, nextY);
        verticalSpeeds.put(id, verticalSpeed);
    }

    private static Game.ParticipantView findBySide(Game.SessionView state, String side) {
        return state.participants().stream().filter(player -> player.side().equals(side))
                .findFirst().orElse(null);
    }

    private static Game.ParticipantView findById(Game.SessionView state, String id) {
        return state.participants().stream().filter(player -> player.id().equals(id))
                .findFirst().orElse(null);
    }

    private boolean touches(double x, double y, double width, double height, Game.ParticipantView other) {
        return other != null && x <= other.x() + other.width() && x + width >= other.x()
                && y <= other.y() + other.height() && y + height >= other.y();
    }

    private boolean isJump(Control control) {
        return control == Control.LEFT_JUMP || control == Control.RIGHT_JUMP;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
