package edu.eci.DOPOSoccer.domain;

import java.util.Set;
import edu.eci.DOPOSoccer.domain.configuration.Configuration;
import edu.eci.DOPOSoccer.domain.configuration.KeyBindings;
import edu.eci.DOPOSoccer.domain.enums.Action;

/**
 * Representa al participante controlado por una persona.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Player extends SessionParticipant {

    private int user;
    private Configuration configuration;

    /**
     * Crea un jugador a partir del identificador de usuario.
     * @param user identificador del usuario
     */
    public Player(int user) {
    }

    /**
     * Registra una tecla presionada.
     * @param key código de la tecla
     */
    public void keyPressed(int key) {
    }

    /**
     * Registra la liberación de una tecla.
     * @param key código de la tecla
     */
    public void keyReleased(int key) {
    }

    /**
     * Determina las acciones solicitadas por las teclas activas.
     * @param field cancha actual
     * @return conjunto de acciones
     */
    public Set<Action> decideAction(SoccerField field) {
        return null;
    }

    /**
     * Comprueba si las asignaciones de teclas entran en conflicto.
     * @param other asignaciones que se compararán
     * @return indica si hay conflicto
     */
    public boolean conflictsWith(KeyBindings other) {
        return false;
    }

    /**
     * Comprueba si hay una tecla activa.
     * @return indica si alguna tecla está activa
     */
    public boolean isKey() {
        return false;
    }
}
