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

    /** Registra la presión o liberación de una tecla de movimiento o salto. */
    public void setControl(Control control, boolean pressed) {
        if (pressed) {
            boolean firstPress = held.add(control);
            if (firstPress && isJump(control)) jumpRequested.add(control);
        } else {
            held.remove(control);
        }
    }

    /** Limpia las teclas sostenidas y las solicitudes de salto pendientes. */
    public void clearInputs() {
        held.clear();
        jumpRequested.clear();
    }

    /** Asocia una sesión y borra los estados visuales de movimiento anteriores. */
    public void reset(Game game) {
        clearInputs();
        verticalSpeeds.clear();
        activeGame = game;
    }

    /** Reinicia el salto y la velocidad vertical después de comenzar una ronda. */
    public void resetAfterRound(Game game) {
        jumpRequested.clear();
        verticalSpeeds.clear();
        activeGame = game;
    }

    /** Actualiza participantes controlados y delega el desplazamiento horizontal a Game. */
    public void update(Game game, MechanicsTestController.SessionMode mode) {
        if (game != activeGame) {
            verticalSpeeds.clear();
            activeGame = game;
            }
        Game.SessionView state = game.inspectSession();
        if (state.finished()) return;

        Game.ParticipantView left = findBySide(state, "left");
        Game.ParticipantView right = findBySide(state, "right");
        if (mode == MechanicsTestController.SessionMode.PLAYER_VS_PLAYER && left != null) {
            updateActor(game, left, right,
                    Control.LEFT_LEFT, Control.LEFT_RIGHT, Control.LEFT_JUMP);
        }
        if (right != null) {
            Game.SessionView current = game.inspectSession();
            right = findBySide(current, "right");
            left = findBySide(current, "left");
            updateActor(game, right, left,
                    Control.RIGHT_LEFT, Control.RIGHT_RIGHT, Control.RIGHT_JUMP);
        }
        jumpRequested.clear();
    }

    /** Usa el movimiento real del dominio y limita solo la simulación visual del salto. */
    private void updateActor(Game game, Game.ParticipantView actor,
            Game.ParticipantView opponent, Control leftControl, Control rightControl, Control jumpControl) {
        String id = actor.id();
        double oldX = actor.x();
        double oldY = actor.y();
        int horizontal = (held.contains(rightControl) ? 1 : 0) - (held.contains(leftControl) ? 1 : 0);
        double nextX = oldX;
        if (horizontal != 0) {
            game.movePlayer(id, horizontal > 0);
            Game.ParticipantView moved = findById(game.inspectSession(), id);
            if (moved == null) return;
            var boundaries = game.inspectSession().boundaries();
            double minX = boundaries.get(2).x() + boundaries.get(2).width() + EDGE_GAP;
            double maxX = boundaries.get(3).x() - moved.width() - EDGE_GAP;
            nextX = clamp(moved.x(), minX, maxX);
            if (touches(nextX, oldY, moved.width(), moved.height(), opponent)) nextX = oldX;
            if (nextX != moved.x()) game.setParticipantPosition(id, nextX, oldY);
        }

        Game.SessionView currentState = game.inspectSession();
        Game.ParticipantView positioned = findById(currentState, id);
        if (positioned == null) return;
        var boundaries = currentState.boundaries();
        double floorY = boundaries.get(1).y() - positioned.height();
        double ceilingY = boundaries.get(0).y() + boundaries.get(0).height() + EDGE_GAP;
        double verticalSpeed = verticalSpeeds.getOrDefault(id, 0.0);
        boolean grounded = oldY >= floorY - EDGE_GAP
                || supportedByBall(currentState.balls(), nextX, oldY, positioned.width(), positioned.height());
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
        nextY = resolveBallVerticalContact(currentState.balls(), nextX, oldY, nextY,
                positioned.width(), positioned.height(), verticalSpeed);
        if (nextY == oldY) verticalSpeed = 0;
        game.setParticipantPosition(id, nextX, nextY);
        verticalSpeeds.put(id, verticalSpeed);
    }

    /** Detecta apoyo desde arriba para permitir que el participante vuelva a saltar. */
    private boolean supportedByBall(java.util.List<Game.BallView> balls,
            double x, double y, double width, double height) {
        for (Game.BallView ball : balls) {
            double top = ball.y() - ball.radius();
            if (Math.abs(y + height - top) <= EDGE_GAP
                    && overlapsHorizontally(x, width, ball)) return true;
        }
        return false;
    }

    /** Detiene el salto vertical en la cara del balón que encontró en su trayectoria. */
    private double resolveBallVerticalContact(java.util.List<Game.BallView> balls,
            double x, double oldY, double nextY, double width, double height, double verticalSpeed) {
        if (verticalSpeed > 0) {
            for (Game.BallView ball : balls) {
                double top = ball.y() - ball.radius();
                if (oldY + height <= top + EDGE_GAP && nextY + height >= top
                        && overlapsHorizontally(x, width, ball)) return top - height;
            }
        } else if (verticalSpeed < 0) {
            for (Game.BallView ball : balls) {
                double bottom = ball.y() + ball.radius();
                if (oldY >= bottom - EDGE_GAP && nextY <= bottom
                        && overlapsHorizontally(x, width, ball)) return bottom;
            }
        }
        return nextY;
    }

    /** Comprueba si el círculo del balón alcanza el intervalo horizontal del participante. */
    private boolean overlapsHorizontally(double x, double width, Game.BallView ball) {
        return ball.x() + ball.radius() >= x && ball.x() - ball.radius() <= x + width;
    }

    /** Busca un participante por el lado que ocupa en la instantánea. */
    private static Game.ParticipantView findBySide(Game.SessionView state, String side) {
        return state.participants().stream().filter(player -> player.side().equals(side))
                .findFirst().orElse(null);
    }

    /** Busca un participante por su identificador opaco. */
    private static Game.ParticipantView findById(Game.SessionView state, String id) {
        return state.participants().stream().filter(player -> player.id().equals(id))
                .findFirst().orElse(null);
    }

    /** Comprueba si el rectángulo propuesto toca o se superpone con otro participante. */
    private boolean touches(double x, double y, double width, double height, Game.ParticipantView other) {
        return other != null && x <= other.x() + other.width() && x + width >= other.x()
                && y <= other.y() + other.height() && y + height >= other.y();
    }

    /** Indica si el control corresponde a una tecla de salto. */
    private boolean isJump(Control control) {
        return control == Control.LEFT_JUMP || control == Control.RIGHT_JUMP;
    }

    /** Limita un valor al intervalo permitido. */
    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
