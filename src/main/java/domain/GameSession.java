package domain;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import domain.GameException;
import domain.hitbox.Hitbox;
import domain.TypeOfMatch;

/**
 * Mantiene el estado de una sesión, sus participantes, la cancha, el marcador
 * y la modalidad que determina cuándo termina el partido.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class GameSession {

    private static GameSession instance;

    /** Devuelve la sesión singleton existente sin crear una nueva. */
    public static GameSession getInstance() throws GameException {
        if (instance == null) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        return instance;
    }

    /**
     * Devuelve la única sesión compartida por todas las clases concretas.
     * La primera llamada crea la subclase indicada; llamadas posteriores
     * devuelven esa sesión sin aplicar nuevos parámetros ni cambiar modalidad.
     */
    public static GameSession getInstance(String typeGame, boolean player,
            String typeMatche, List<String> parametersRules, List<String> parametersPowers) throws GameException {
        if (instance != null) return instance;
        try {
            Class<? extends GameSession> targetClass = findSessionSubclass(typeGame);
            var constructor = targetClass.getDeclaredConstructor(
                    boolean.class, String.class, List.class, List.class);
            if (java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())
                    && targetClass != PlayerVsPlayer.class && targetClass != PlayerVsBot.class) {
                throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
            }
            constructor.setAccessible(true);
            instance = GameSession.class.cast(constructor.newInstance(
                    player, typeMatche, parametersRules, parametersPowers));
            return instance;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        } catch (InstantiationException | java.lang.reflect.InvocationTargetException e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
    }

    /** Busca y valida una clase concreta que herede de {@code GameSession}. */
    private static Class<? extends GameSession> findSessionSubclass(String typeGame)
            throws GameException {
        try {
            Class<?> candidate = Class.forName(typeGame);
            if (!GameSession.class.isAssignableFrom(candidate)) {
                throw new GameException(GameException.NO_FOUND_SESSION);
            }
            return candidate.asSubclass(GameSession.class);
        } catch (ClassNotFoundException e) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
    }

    private static final double STEP_SECONDS = 1.0 / 60.0;
    private static final double BOUNCE_RETENTION = 0.70;
    private static final double MIN_BOUNCE_SPEED = 0.5;
    private static final double GRAVITY = 40.0;
    private static final double KICK_REACH = 8.0;
    private static final double KICK_HORIZONTAL_IMPULSE = 150.0;
    private static final double KICK_VERTICAL_IMPULSE = -50.0;

    private double timeLeft;
    private TypeOfMatch TypeOfMatch;
    private SoccerField field;
    private int goalsLeft;
    private int goalsRight;
    private boolean finished;
    protected List<SessionParticipant> participantsRight;
    protected List<SessionParticipant> participantsLeft;
    

    /**
     * Crea una sesión e instancia reflectivamente su modalidad de partido.
     *
     * @param player indicador que la modalidad concreta puede usar para elegir
     *        el jugador humano; no determina el lado de la cancha
     * @param typeMatche nombre completo de la clase de modalidad, como
     *        {@code ByGoals} o {@code ByTime}
     * @param parametersRules parámetros requeridos por el constructor de esa modalidad
     * @throws GameException si la clase no existe, no hereda de {@link TypeOfMatch},
     *         no expone el constructor requerido o no se puede instanciar
     */
    public GameSession(boolean player,String typeMatche, List<String> parametersRules) throws GameException {
        participantsRight = new ArrayList<>();
        participantsLeft = new ArrayList<>();
        Object typeMatcheObject = createMatchRule(typeMatche, parametersRules);
        TypeOfMatch typeMatcheFinal = (TypeOfMatch) typeMatcheObject;
        field = new SoccerField();
        timeLeft = 0;
        TypeOfMatch = typeMatcheFinal;
    }

    /**
     * Desplaza al participante si pertenece a esta sesión y la partida sigue activa.
     * {@code true} representa derecha y {@code false} izquierda.
     * @param user participante
     * @param direction dirección
     */
    public void movePlayer(SessionParticipant user, boolean direction) {
        if (finished || user == null || (!participantsRight.contains(user) && !participantsLeft.contains(user))) {
            return;
        }
        double previousX = user.getHitbox().getX();
        user.move(direction);
        List<SessionParticipant> all = new ArrayList<>(participantsLeft);
        all.addAll(participantsRight);
        field.resolveWalkingContact(user, previousX, all, BOUNCE_RETENTION);
    }

    /**
     * Reinicia cada balón en el centro de la cancha, recoloca los participantes
     * y limpia el estado de la ronda.
     */
    public void restartRound() {
        field.resetBall(field.courtX() + field.courtWidth() / 2,
                field.courtY() + field.courtHeight() / 2);
        positionParticipants();
    }


    /**
     * Registra el gol si {@code side} es {@code "left"} o {@code "right"}; en
     * cualquier otro caso no cambia el marcador. Después reinicia la ronda y
     * comprueba la regla del partido.
     * @param side lado que anotó; se aceptan {@code "left"} y {@code "right"}
     */
    public void goal(String side) {
        if ("left".equals(side)) goalsLeft++;
        else if ("right".equals(side)) goalsRight++;
        else return;
        restartRound();
        checkTypeOfMatch();
    }

    /**
     * Busca si el centro de algún balón entró en un arco. El arco izquierdo
     * suma al lado derecho y el arco derecho suma al izquierdo; al encontrar
     * el primer gol, delega el registro y termina la búsqueda.
     */
    private void goal() {
        for (Ball ball : field.balls()) {
            String goalSide = field.goalSideFor(ball);
            if ("left".equals(goalSide)) {
                goal("right");
                return;
            }
            if ("right".equals(goalSide)) {
                goal("left");
                return;
            }
        }
    }


    /**
     * Punto de extensión para solicitar un poder; actualmente no modifica al
     * participante.
     * @param player participante que usará el poder
     */
    public void powerPlayer(SessionParticipant player) {
    }

    /**
     * Patea al rival si está a 8 unidades o menos de la hitbox del atacante;
     * si no hay rival en alcance, patea el balón si está a su alcance. El jugador
     * izquierdo impulsa el balón hacia la derecha y el derecho hacia la izquierda.
     *
     * @param player participante que intenta patear
     * @return {@code true} si golpeó a un rival o impulsó el balón
     */
    public boolean kickPlayer(SessionParticipant player) {
        if (finished || player == null || player.blocksMovement()) return false;
        int direction;
        List<SessionParticipant> opponents;
        if (participantsLeft.contains(player)) {
            direction = 1;
            opponents = participantsRight;
        }
        else if (participantsRight.contains(player)) {
            direction = -1;
            opponents = participantsLeft;
        }
        else return false;

        var box = player.getHitbox();
        SessionParticipant opponent = closestOpponentInReach(box, opponents);
        if (opponent != null) {
            opponent.takeDamage(player.getStats().getKickDamage());
            return true;
        }
        for (Ball ball : field.balls()) {
            double closestX = Math.max(box.getX(), Math.min(ball.getX(), box.getX() + box.getWidth()));
            double closestY = Math.max(box.getY(), Math.min(ball.getY(), box.getY() + box.getHeight()));
            if (Math.hypot(ball.getX() - closestX, ball.getY() - closestY) <= ball.getRadius() + KICK_REACH) {
                ball.applyImpulse(direction * KICK_HORIZONTAL_IMPULSE, KICK_VERTICAL_IMPULSE);
                return true;
            }
        }
        return false;
    }

    /** Busca el rival más cercano cuya hitbox esté dentro del alcance de patada. */
    private SessionParticipant closestOpponentInReach(
            domain.hitbox.RectangularHitbox attacker, List<SessionParticipant> opponents) {
        SessionParticipant closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (SessionParticipant opponent : opponents) {
            var target = opponent.getHitbox();
            double dx = Math.max(0, Math.max(attacker.getX() - target.getX() - target.getWidth(),
                    target.getX() - attacker.getX() - attacker.getWidth()));
            double dy = Math.max(0, Math.max(attacker.getY() - target.getY() - target.getHeight(),
                    target.getY() - attacker.getY() - attacker.getHeight()));
            double distance = Math.hypot(dx, dy);
            if (distance <= KICK_REACH && distance < closestDistance) {
                closest = opponent;
                closestDistance = distance;
            }
        }
        return closest;
    }

    /**
     * Carga la clase por nombre, verifica que herede de {@link TypeOfMatch} y
     * la instancia con el constructor que recibe una {@link List}. Convierte
     * los errores de carga, firma, acceso e invocación a {@link GameException}.
     * @param typeGame nombre completo de la clase de modalidad
     * @param parametersRules argumentos de configuración de la modalidad
     * @return instancia de la modalidad creada
     * @throws GameException si el nombre no existe, el tipo no hereda de
     *         {@link TypeOfMatch}, falta el constructor de lista o la clase no
     *         puede instanciarse
     */
    private TypeOfMatch createMatchRule(String typeGame, List<String> parametersRules) throws GameException{
        try {
            Class<?> targetClass = Class.forName(typeGame);
            if (!TypeOfMatch.class.isAssignableFrom(targetClass)) {
                throw new GameException(GameException.NO_FOUND_TYPE_GAME);
            }
            return TypeOfMatch.class.cast(
                    targetClass.getDeclaredConstructor(List.class).newInstance(parametersRules));
        }
        catch (ClassNotFoundException e) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        catch (NoSuchMethodException  e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        }
        catch (InstantiationException  e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
        catch (IllegalAccessException  e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        }
        catch (InvocationTargetException  e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
    }

    /**
     * Evalúa la regla activa con los marcadores y, para {@link domain.ByTime},
     * los minutos completos transcurridos. Actualiza {@code finished}; la regla
     * temporal devuelve si continúa, por lo que su resultado se invierte. Al
     * terminar con marcador desigual, otorga puntos a participantes humanos
     * del lado ganador una sola vez.
     */
    public void checkTypeOfMatch() {
        boolean wasFinished = finished;
        if (TypeOfMatch instanceof domain.ByGoals) {
            finished = TypeOfMatch.check(List.of(Integer.toString(goalsLeft), Integer.toString(goalsRight)));
        } else if (TypeOfMatch instanceof domain.ByTime) {
            finished = !TypeOfMatch.check(List.of(Integer.toString(goalsLeft), Integer.toString(goalsRight),
                    Integer.toString((int) Math.floor((timeLeft + 1e-9) / 60))));
        }
        if (!wasFinished && finished) {
            awardWinningPlayers();
        }
    }

    /**
     * Mejora una estadística del jugador seleccionado.
     *
     * @param player {@code true} selecciona derecha/jugador 1; {@code false} izquierda/jugador 2
     * @param stats estadística que se mejorará
     * @throws GameException si no hay participante seleccionado, faltan puntos
     *         o la habilidad no existe
     */
    public void improveStatistics(boolean player, String stats) {
        List<SessionParticipant> selectedSide = player ? participantsRight : participantsLeft;
        if (selectedSide.isEmpty()) {
            throw new GameException(GameException.INVALID_PARTICIPANT_ID);
        }
        selectedSide.get(0).improveStatistics(stats);
    }

    /** Otorga los puntos de victoria a los jugadores humanos del lado ganador. */
    private void awardWinningPlayers() {
        if (goalsLeft == goalsRight) return;
        List<SessionParticipant> winningSide = goalsRight > goalsLeft ? participantsRight : participantsLeft;
        for (SessionParticipant participant : winningSide) {
            if (participant instanceof Player) {
                participant.getStats().awardWin();
            }
        }
    }

    /**
     * Avanza una sesión activa un paso fijo: actualiza efectos temporales,
     * incrementa el tiempo, integra el balón con gravedad, procesa contactos y
     * vuelve a consultar la regla si la sesión sigue activa.
     */
    public void updateGame() {
        if (finished) return;
        for (SessionParticipant participant : participantsLeft) participant.updateStatusEffects();
        for (SessionParticipant participant : participantsRight) participant.updateStatusEffects();
        timeLeft += STEP_SECONDS;
        field.advanceBalls(STEP_SECONDS, GRAVITY);
        collision();
        if (!finished) checkTypeOfMatch();
    }

    /**
     * Comprueba primero participantes fuera de cancha y superpuestos; después
     * procesa goles antes de salidas del balón, rebotes con límites/arcos y suelo,
     * y contactos físicos entre balones y participantes. La cancha corrige los
     * balones contra los límites para mantenerlos dentro; lanza {@link GameException}
     * si un participante excede el campo o se superpone con otro.
     */
    private void collision() {
        List<SessionParticipant> allParticipants = new ArrayList<>(participantsLeft);
        allParticipants.addAll(participantsRight);
        for (SessionParticipant participant : allParticipants) {
            if (field.isOutside(participant) || field.touchesForbiddenBoundary(participant)) {
                throw new GameException(GameException.OBJECT_OUT_OF_MATCH);
            }
        }
        for (int i = 0; i < allParticipants.size(); i++) {
            for (int j = i + 1; j < allParticipants.size(); j++) {
                if (allParticipants.get(i).intersects(allParticipants.get(j)).isContact()) {
                    throw new GameException(GameException.OBJECTS_OVERLAPPED);
                }
            }
        }

        int leftBefore = goalsLeft;
        int rightBefore = goalsRight;
        goal();
        if (leftBefore != goalsLeft || rightBefore != goalsRight) return;

        field.resolveBallCollisions(BOUNCE_RETENTION);
        field.resolveBallParticipantCollisions(allParticipants, BOUNCE_RETENTION, MIN_BOUNCE_SPEED);
        for (Ball ball : field.balls()) {
            field.bounceBallAtWallsAndGoalFrames(ball, BOUNCE_RETENTION);
            field.bounceBallAtGround(ball, BOUNCE_RETENTION, MIN_BOUNCE_SPEED);
            field.keepBallInsideCourt(ball, BOUNCE_RETENTION);
        }
    }

    /**
     * Delega a la cancha la colocación simétrica del primer participante de
     * cada lista; si alguno de los lados no tiene participantes, no hace cambios.
     */
    public void positionParticipants() {
        if (!participantsLeft.isEmpty() && !participantsRight.isEmpty()) {
            field.positionParticipants(participantsLeft.get(0), participantsRight.get(0));
        }
    }

    /** @return goles anotados por el lado izquierdo */
    public int getGoalsLeft() { return goalsLeft; }
    /** @return goles anotados por el lado derecho */
    public int getGoalsRight() { return goalsRight; }
    /** @return {@code true} si la regla del partido ya terminó la sesión */
    public boolean isFinished() { return finished; }
    /** @return tiempo transcurrido de la sesión, en segundos */
    public double getElapsedSeconds() { return timeLeft; }

    /** Devuelve la cancha de esta sesión para que la presentación pueda dibujarla. */
    public SoccerField getField() { return field; }

    /** Devuelve una copia de los participantes del lado izquierdo. */
    public List<SessionParticipant> getParticipantsLeft() { return List.copyOf(participantsLeft); }

    /** Devuelve una copia de los participantes del lado derecho. */
    public List<SessionParticipant> getParticipantsRight() { return List.copyOf(participantsRight); }
}
