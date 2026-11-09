package edu.eci.DOPOSoccer.presentation.controllers;

import edu.eci.DOPOSoccer.domain.Game;
import edu.eci.DOPOSoccer.domain.GameSession;
import edu.eci.DOPOSoccer.domain.SessionParticipant;

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

    /** Solicita iniciar una partida mediante el modelo. */
    public void startGame() {
    }

    /** Solicita mover al participante en la dirección indicada. */
    public void movePlayer(SessionParticipant participant, char direction) {
    }

    /** Solicita que el participante salte. */
    public void jumpPlayer(SessionParticipant participant) {
    }

    /** Solicita que el participante active su poder. */
    public void activatePower(SessionParticipant participant) {
    }
}
