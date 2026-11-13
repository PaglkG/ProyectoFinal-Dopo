package domain;

/**
 * Representa al participante controlado por una persona.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Player extends SessionParticipant {

    private int user;

    /**
     * Crea un jugador a partir del identificador de usuario.
     * @param user identificador del usuario
     */
    public Player(int user) {
        this.user = user;
    }

}
