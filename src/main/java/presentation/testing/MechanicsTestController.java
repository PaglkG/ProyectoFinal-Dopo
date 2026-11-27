package presentation.testing;

import domain.Game;
import java.util.List;

/**
 * Adapta las acciones manuales del banco a las operaciones públicas de {@link Game}.
 * Recibe el perfil de bot en el constructor y los eventos de
 * {@link MechanicsTestPanel} en sus métodos de control. {@code update()}
 * aplica las direcciones sostenidas y avanza la sesión; {@code game()} entrega
 * la fachada que el panel lee al actualizar las etiquetas y al repintar.
 */
final class MechanicsTestController {
    private static final String MANUAL_SESSION_TYPE = "domain.PlayerVsPlayer";
    private static final String BOT_SESSION_TYPE = "domain.PlayerVsBot";
    private static final String MATCH_TYPE = "domain.ByGoals";
    private final Game game = new Game();
    private final PresentationMovement movement = new PresentationMovement();
    private final String botProfile;

    /** Abre la sesión del banco con el humano a la izquierda cuando hay un bot.
     * @param botProfile nombre simple del perfil, o {@code null} para dos jugadores
     */
    MechanicsTestController(String botProfile) {
        this.botProfile = botProfile;
        List<String> powers = hasBot()
                ? List.of("domain.BlueAura", "domain.Lightning", botProfile)
                : List.of("domain.BlueAura", "domain.Lightning");
        game.startSession(List.of("150", "450", "5"), powers,
                hasBot() ? BOT_SESSION_TYPE : MANUAL_SESSION_TYPE, MATCH_TYPE, true);
    }

    /** Devuelve la fachada que suministra el estado que se dibuja.
     * @return fachada de la sesión activa
     */
    Game game() { return game; }

    /** Indica si la sesión se configuró con el bot del lado derecho.
     * @return {@code true} si el lado derecho se controla desde el dominio
     */
    boolean hasBot() { return botProfile != null; }

    /** Traduce un lado visual a la identidad que recibe la fachada.
     * @param rightSide indica el lado derecho
     * @return {@code true} para la identidad del jugador 1
     */
    boolean playerIdentity(boolean rightSide) {
        // En manual, el jugador 1 queda a la derecha. Contra bot queda a la izquierda.
        return rightSide != hasBot();
    }

    /** Devuelve el nombre visible de la configuración seleccionada.
     * @return nombre de la partida libre o del perfil de bot
     */
    String matchLabel() { return hasBot() ? "Contra bot: " + botProfile : "Partida libre"; }

    /** Avanza un paso de la simulación.
     */
    void update() {
        if (game.isFinished()) return;
        // Aplica primero las direcciones registradas por Swing y luego avanza la física.
        movement.update(game, playerIdentity(false));
        game.updateGame();
    }

    /** Reinicia la ronda manteniendo la sesión activa.
     */
    void reset() {
        game.resetSession();
        releaseMovement();
    }

    /** Libera las direcciones sostenidas cuando se pierde el foco o se pausa el banco. */
    void releaseMovement() {
        movement.reset();
    }

    /** Cambia el estado de un control sostenido de movimiento horizontal.
     * @param rightPlayer {@code true} selecciona al participante derecho
     * @param rightDirection {@code true} selecciona la dirección derecha
     * @param pressed indica si la tecla está presionada
     */
    void setMovementControl(boolean rightPlayer, boolean rightDirection, boolean pressed) {
        // Si hay bot, el lado derecho se mueve únicamente mediante el dominio.
        if (!rightPlayer || !hasBot()) movement.setControl(rightPlayer, rightDirection, pressed);
    }

    /** Solicita el salto de un participante humano por medio de Game.
     * @param rightSide {@code true} para el lado derecho
     */
    void jump(boolean rightSide) {
        // El banco convierte el lado de la tecla en la identidad esperada por Game.
        if (!rightSide || !hasBot()) game.jumpPlayer(playerIdentity(rightSide));
    }

    /** Intenta patear con el participante del lado indicado.
     * @param rightSide {@code true} para el lado derecho
     * @return {@code true} si la patada alcanza el balón o al rival
     */
    boolean kick(boolean rightSide) {
        // El retorno del dominio indica si el panel debe mostrar contacto.
        return (!rightSide || !hasBot()) && game.kickPlayer(playerIdentity(rightSide));
    }

}
