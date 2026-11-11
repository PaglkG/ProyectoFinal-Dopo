package edu.eci.DOPOSoccer.domain;

import java.util.List;

import edu.eci.DOPOSoccer.domain.exception.GameException;

/**
 * Sesión entre un jugador humano y un bot.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class PlayerVsBot extends GameSession {

    /**
     * Crea una sesión con un jugador humano y un bot.
     *
     * @param player selecciona el identificador del jugador humano: {@code true}
     *        agrega al jugador 1 y {@code false} agrega al jugador 2
     * @param typeMatche nombre completo de la clase de modalidad del partido
     * @param parametersRules parámetros entregados a la modalidad del partido
     * @throws GameException si la modalidad no existe o no se puede instanciar
     */
    public PlayerVsBot(boolean player, String typeMatche, List<String> parametersRules) throws GameException {
        super(player, typeMatche, parametersRules);
        if (player) {
            Player player1 = new Player(1);
            participantsRight.add(player1);
        }
        else {
            Player player2 = new Player(2);
            participantsRight.add(player2);
        }
        BotPlayer bot = new BotPlayer();
        participantsLeft.add(bot);
    }

    @Override 
    public void resetConfiguration(boolean player) {
        participantsRight.get(0).resetConfiguration();
    }

    /**
     * Agrega el usuario indicado a la sesión.
     * @param user identificador de usuario
     */
    public void addParticipant(int user) {
    }
}
