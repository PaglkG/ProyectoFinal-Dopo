package presentation.testing;

import domain.Game;

/** Repite el movimiento horizontal mientras una tecla permanece presionada. */
final class PresentationMovement {
    private boolean leftPlayerLeftPressed;
    private boolean leftPlayerRightPressed;
    private boolean rightPlayerLeftPressed;
    private boolean rightPlayerRightPressed;

    /** Registra la presión o liberación de una dirección de un participante.
     * @param rightPlayer {@code true} para el participante derecho
     * @param rightDirection {@code true} para la dirección derecha
     * @param pressed {@code true} mientras la tecla está presionada
     */
    void setControl(boolean rightPlayer, boolean rightDirection, boolean pressed) {
        if (rightPlayer && rightDirection) rightPlayerRightPressed = pressed;
        else if (rightPlayer) rightPlayerLeftPressed = pressed;
        else if (rightDirection) leftPlayerRightPressed = pressed;
        else leftPlayerLeftPressed = pressed;
    }

    /** Libera todas las teclas registradas.
     */
    void reset() {
        leftPlayerLeftPressed = false;
        leftPlayerRightPressed = false;
        rightPlayerLeftPressed = false;
        rightPlayerRightPressed = false;
    }

    /** Solicita a Game un paso horizontal para cada participante controlado.
     * @param game fachada de la sesión que aplica el movimiento
     */
    void update(Game game) {
        if (game.isFinished()) return;
        updatePlayer(game, false, leftPlayerLeftPressed, leftPlayerRightPressed);
        updatePlayer(game, true, rightPlayerLeftPressed, rightPlayerRightPressed);
    }

    private void updatePlayer(Game game, boolean playerOne, boolean left, boolean right) {
        // Convierte las teclas sostenidas en un paso de movimiento para Game.
        int direction = (right ? 1 : 0) - (left ? 1 : 0);
        if (direction != 0) game.movePlayer(playerOne, direction > 0);
    }
}
