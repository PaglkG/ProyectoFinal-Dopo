package presentation.testing;

import domain.Game;

/** Adapta las acciones manuales del banco a las operaciones públicas de {@link Game}. */
final class MechanicsTestController {
    private static final String SESSION_TYPE = "domain.PlayerVsPlayer";
    private static final String MATCH_TYPE = "domain.ByGoals";
    private final Game game = new Game();
    private final PresentationMovement movement = new PresentationMovement();

    /** Crea o reutiliza la sesión local estándar para el banco visual. */
    MechanicsTestController() {
        game.startSession(java.util.List.of("150", "450", "5"),
                java.util.List.of("domain.BlueAura", "domain.Lightning"),
                SESSION_TYPE, MATCH_TYPE, true);
    }

    /** Devuelve la fachada que suministra el estado que se dibuja.
     * @return fachada de la sesión activa
     */
    Game game() { return game; }

    /** Avanza un paso de la simulación.
     */
    void update() {
        movement.update(game);
        game.updateGame();
    }

    /** Reinicia la ronda manteniendo la sesión activa.
     */
    void reset() {
        game.resetSession();
        movement.reset();
    }

    /** Cambia el estado de un control sostenido de movimiento horizontal.
     * @param rightPlayer {@code true} selecciona al participante derecho
     * @param rightDirection {@code true} selecciona la dirección derecha
     * @param pressed indica si la tecla está presionada
     */
    void setMovementControl(boolean rightPlayer, boolean rightDirection, boolean pressed) {
        movement.setControl(rightPlayer, rightDirection, pressed);
    }

    /** Intenta patear con el participante del lado indicado.
     * @param rightSide {@code true} para el lado derecho
     * @return {@code true} si la patada alcanza el balón o al rival
     */
    boolean kick(boolean rightSide) { return game.kickPlayer(rightSide); }

}
