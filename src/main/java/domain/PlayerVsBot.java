package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import domain.GameException;
import domain.PowerFactory;
import domain.Clumsy;
import domain.Offensive;
import domain.Defensive;
import domain.Expert;

/**
 * Sesión entre un jugador humano y un bot.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class PlayerVsBot extends GameSession {
    private static final long serialVersionUID = -5259341864465568882L;
    /** Mantiene la solicitud de patada como una pulsación hasta que el bot deja de pedirla. */
    private boolean kickHeld;

    /**
     * Crea una sesión con un jugador humano y un bot.
     *
     * @param player selecciona el identificador del jugador humano: {@code true}
     *        agrega al jugador 1 y {@code false} agrega al jugador 2
     * @param typeMatche nombre completo de la clase de modalidad del partido
     * @param parametersRules parámetros entregados a la modalidad del partido
     * @param parametersPowers poderes del humano y del bot seguidos por el perfil bot
     * @throws GameException si la modalidad no existe o no se puede instanciar
     */
    private PlayerVsBot(boolean player, String typeMatche, List<String> parametersRules,
            List<String> parametersPowers) throws GameException {
        super(player, typeMatche, parametersRules);
        if (parametersPowers == null || parametersPowers.size() != 3) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        Player human;
        if (player) {
            human = new Player(1);
        }
        else {
            human = new Player(2);
        }
        human.setSpecialPower(PowerFactory.create(parametersPowers.get(0)));
        BotPlayer bot = createBot(parametersPowers.get(2));
        bot.setSpecialPower(PowerFactory.create(parametersPowers.get(1)));
        participantsLeft.add(human);
        participantsRight.add(bot);
        positionParticipants();
    }

    /** Refleja las partidas antiguas que guardaron al humano a la derecha.
     * @param input flujo que contiene el estado serializado
     * @throws IOException si falla la lectura de la partida
     * @throws ClassNotFoundException si falta una clase almacenada
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        if (!participantsLeft.isEmpty() && participantsLeft.get(0) instanceof BotPlayer) {
            mirrorPlayingSides();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void updateAutonomousParticipants() {
        BotPlayer bot = (BotPlayer) participantsRight.get(0);
        var actions = bot.decideAction(getField(), getGoalsRight() - getGoalsLeft(), jumpSupportY(bot));
        boolean wantsKick = actions.contains("kick");
        if (wantsKick && !kickHeld) kickPlayer(bot);
        kickHeld = wantsKick;
        for (String action : actions) {
            switch (action) {
                case "moveLeft" -> movePlayer(bot, false, true);
                case "moveRight" -> movePlayer(bot, true, true);
                case "jump" -> jumpPlayer(bot);
                default -> { } // Una acción aún no conectada no cambia el estado.
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void restartRound() {
        super.restartRound();
    }

    /** {@inheritDoc} */
    @Override
    protected void resetRoundInputState() { kickHeld = false; }

    /** Construye el perfil de bot solicitado y convierte errores de reflexión a GameException. */
    private static BotPlayer createBot(String difficulty) throws GameException {
        if (difficulty == null) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        String className = switch (difficulty) {
            case "Clumsy" -> Clumsy.class.getName();
            case "Offensive" -> Offensive.class.getName();
            case "Defensive" -> Defensive.class.getName();
            case "Expert" -> Expert.class.getName();
            default -> throw new GameException(GameException.NO_FOUND_SESSION);
        };
        try {
            Class<?> targetClass = Class.forName(className);
            if (!BotPlayer.class.isAssignableFrom(targetClass)) {
                throw new GameException(GameException.NO_FOUND_SESSION);
            }
            return BotPlayer.class.cast(targetClass.getDeclaredConstructor().newInstance());
        }
        catch (ClassNotFoundException e) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        catch (NoSuchMethodException | IllegalAccessException e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        }
        catch (InstantiationException | InvocationTargetException e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
    }


}
