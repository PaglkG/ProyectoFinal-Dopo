package domain;

/**
 * Representa al participante controlado por una persona.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Player extends SessionParticipant {

    /** Identificador humano del jugador, independiente de su lado. */
    private int user;

    /**
     * Crea un jugador a partir del identificador de usuario.
     * @param user identificador del usuario
     */
    public Player(int user) {
        this.user = user;
    }

    /** Devuelve el identificador estable del usuario.
     * @return identificador usado para conservar estadísticas entre sesiones
     */
    public int getUserId() {
        return user;
    }

}
