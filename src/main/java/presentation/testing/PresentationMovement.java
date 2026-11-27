package presentation.testing;

import domain.Game;

/**
 * Recibe cambios de teclado en {@code setControl()} y guarda qué direcciones
 * están sostenidas. {@code update()} se ejecuta en el ciclo del banco y
 * convierte ese estado en llamadas a {@code Game.movePlayer()}.
 * {@code reset()} limpia las direcciones al liberar los controles del banco.
 */
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

    /** Libera las direcciones horizontales registradas.
     */
    void reset() {
        leftPlayerLeftPressed = false;
        leftPlayerRightPressed = false;
        rightPlayerLeftPressed = false;
        rightPlayerRightPressed = false;
    }

    /** Solicita a Game un paso horizontal para cada participante controlado.
     * @param game fachada de la sesión que aplica el movimiento
     * @param leftIsPlayerOne identidad del lado izquierdo
     */
    void update(Game game, boolean leftIsPlayerOne) {
        if (game.isFinished()) return;
        updatePlayer(game, leftIsPlayerOne, leftPlayerLeftPressed, leftPlayerRightPressed);
        updatePlayer(game, !leftIsPlayerOne, rightPlayerLeftPressed, rightPlayerRightPressed);
    }

    /** Convierte las direcciones sostenidas en un paso para la identidad indicada.
     * Sin dirección o con ambas teclas presionadas, se conserva la posición. */
    private void updatePlayer(Game game, boolean playerOne, boolean left, boolean right) {
        // Las dos direcciones opuestas se cancelan. Solo un sentido genera un paso en Game.
        int direction = (right ? 1 : 0) - (left ? 1 : 0);
        if (direction != 0) game.movePlayer(playerOne, direction > 0);
    }

}
