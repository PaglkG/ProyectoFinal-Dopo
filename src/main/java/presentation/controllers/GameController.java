package presentation.controllers;

import domain.Game;
import domain.GameSession;
import domain.SessionParticipant;

/**
 * Recibe las solicitudes de la interfaz durante una partida y las coordina con
 * el modelo de dominio, manteniendo las reglas fuera de los componentes visuales.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GameController {

    private Game game;
    private GameSession session;

    /** Punto de extensión para iniciar una partida; actualmente no llama al modelo. */
    public void startGame() {
    }

    /** Punto de extensión para enviar el movimiento; actualmente no llama al modelo. */
    public void movePlayer(SessionParticipant participant, char direction) {
    }

    /** Punto de extensión para enviar el salto; actualmente no llama al modelo. */
    public void jumpPlayer(SessionParticipant participant) {
    }

    /** Punto de extensión para enviar la activación del poder; actualmente no llama al modelo. */
    public void activatePower(SessionParticipant participant) {
    }
}
