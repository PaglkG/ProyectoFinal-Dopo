package domain;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Set;
import domain.exception.GameException;
import domain.hitbox.Hitbox;
import domain.rules.TypeOfMatch;

/**
 * Mantiene el estado de una sesión, sus participantes, la cancha, el marcador
 * y la modalidad que determina cuándo termina el partido.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class GameSession {

    private static final double STEP_SECONDS = 1.0 / 60.0;
    private static final double BOUNCE_RETENTION = 0.70;
    private static final double MIN_BOUNCE_SPEED = 0.5;
    private static final double GRAVITY = 40.0;
    private static final double KICK_REACH = 8.0;
    private static final double KICK_HORIZONTAL_IMPULSE = 150.0;
    private static final double KICK_VERTICAL_IMPULSE = -50.0;

    private double timeLeft;
    private int yBase;
    private int xBase;
    private TypeOfMatch TypeOfMatch;
    private Hitbox[] allHitbox;
    private SoccerField field;
    private int goalsLeft;
    private int goalsRight;
    private boolean finished;
    private final IdentityHashMap<Ball, Set<SessionParticipant>> activeBallContacts = new IdentityHashMap<>();
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
     * @throws GameException si la clase no existe o no se puede instanciar
     */
    public GameSession(boolean player,String typeMatche, List<String> parametersRules) throws GameException {
        participantsRight = new ArrayList<>();
        participantsLeft = new ArrayList<>();
        Object typeMatcheObject = whatsRules(typeMatche, parametersRules);
        TypeOfMatch typeMatcheFinal = (TypeOfMatch) typeMatcheObject;
        field = new SoccerField();
        timeLeft = 0;
        TypeOfMatch = typeMatcheFinal;
    }

    /**
     * Solicita el movimiento del participante.
     * @param user participante
     * @param direction dirección
     */
    public void movePlayer(SessionParticipant user, boolean direction) {
    }

    /** Reinicia la ronda en el centro de la cancha después de un gol. */
    public void restartRound() {
        field.resetBall(field.courtX() + field.courtWidth() / 2,
                field.courtY() + field.courtHeight() / 2);
        positionParticipants();
        activeBallContacts.clear();
    }

    /**
     * Asigna la modalidad de partido indicada a la sesión.
     *
     * @param typeMatche nombre de la modalidad que se asignará
     */
    public void assignRules(String typeMatche) {
    }

    /**
     * Comprueba si se cumple la cantidad mínima de participantes.
     * @return sesión con participantes suficientes o estado pendiente
     */
    public GameSession minParticipants() {
        return null;
    }


    

    /**
     * Registra un gol para el lado indicado.
     * @param side lado que anotó
     */
    public void goal(String side) {
        if ("left".equals(side)) goalsLeft++;
        else if ("right".equals(side)) goalsRight++;
        else return;
        restartRound();
        checkTypeOfMatch();
    }

    /** Registra un gol detectado durante la actualización de la sesión. */
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
     * Solicita un salto al participante.
     * @param player participante que saltará
     */
    public void jumpPlayer(SessionParticipant player) {
    }

    /**
     * Solicita la activación del poder del participante.
     * @param player participante que usará el poder
     */
    public void powerPlayer(SessionParticipant player) {
    }

    /**
     * Patea el balón desde un participante registrado cuando está a su alcance.
     * El jugador izquierdo impulsa hacia la derecha y el derecho hacia la izquierda.
     *
     * @param player participante que intenta patear
     * @return {@code true} si se aplicó el impulso
     */
    public boolean kickPlayer(SessionParticipant player) {
        if (finished || player == null) return false;
        int direction;
        if (participantsLeft.contains(player)) direction = 1;
        else if (participantsRight.contains(player)) direction = -1;
        else return false;

        var box = player.getHitbox();
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

    /**
     * Crea la modalidad de partido indicada usando su constructor que recibe
     * la lista de parámetros.
     * @param typeGame nombre completo de la clase de modalidad
     * @param parametersRules argumentos de configuración de la modalidad
     * @return instancia de la modalidad creada
     * @throws GameException si la clase no existe o no se puede instanciar
     */
    private TypeOfMatch whatsRules(String typeGame, List<String> parametersRules) throws GameException{
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

    /** Comprueba el estado del tipo de partido asociado a la sesión. */
    public void checkTypeOfMatch() {
        if (TypeOfMatch instanceof domain.rules.ByGoals) {
            finished = TypeOfMatch.check(List.of(Integer.toString(goalsLeft), Integer.toString(goalsRight)));
        } else if (TypeOfMatch instanceof domain.rules.ByTime) {
            finished = !TypeOfMatch.check(List.of(Integer.toString(goalsLeft), Integer.toString(goalsRight),
                    Integer.toString((int) Math.floor((timeLeft + 1e-9) / 60))));
        }
    }

    /**
     * Mejora una estadística del jugador seleccionado.
     *
     * @param player indica el jugador seleccionado
     * @param stats estadística que se mejorará
     */
    public void ImproveStatistics(boolean player, String stats) {
    }

    /** Actualiza el estado de la sesión. */
    public void updateGame() {
        if (finished) return;
        timeLeft += STEP_SECONDS;
        for (Ball ball : field.balls()) ball.advance(STEP_SECONDS, GRAVITY);
        collision();
        if (!finished) checkTypeOfMatch();
    }

    /** Detecta las colisiones de la sesión. */
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

        // El gol tiene prioridad sobre la comprobación de salida del balón.
        int leftBefore = goalsLeft;
        int rightBefore = goalsRight;
        goal();
        if (leftBefore != goalsLeft || rightBefore != goalsRight) return;

        for (Ball ball : field.balls()) {
            bounceAtWallsAndGoalFrames(ball);
            if (field.isOutside(ball)) throw new GameException(GameException.OBJECT_OUT_OF_MATCH);
            if (field.touchesGround(ball)) {
                ball.bounceAt(field.groundCenterY(ball), BOUNCE_RETENTION, MIN_BOUNCE_SPEED);
            }
            Set<SessionParticipant> currentContacts = new HashSet<>();
            Set<SessionParticipant> previousContacts = activeBallContacts.computeIfAbsent(ball,
                    ignored -> java.util.Collections.newSetFromMap(new IdentityHashMap<>()));
            for (SessionParticipant participant : allParticipants) {
                if (ball.intersects(participant).isContact()) {
                    currentContacts.add(participant);
                    int awayDirection = separateBallFromParticipant(ball, participant);
                    if (!previousContacts.contains(participant)) {
                        applyParticipantImpulse(ball, awayDirection);
                    }
                }
            }
            previousContacts.clear();
            previousContacts.addAll(currentContacts);
        }
    }

    private void bounceAtWallsAndGoalFrames(Ball ball) {
        var walls = field.getHitboxField();
        double radius = ball.getRadius();
        double left = walls[2].getX() + walls[2].getWidth();
        double right = walls[3].getX() - radius;
        double top = walls[0].getY() + walls[0].getHeight() + radius;
        if (ball.getX() < left + radius) ball.bounceHorizontallyAt(left + radius, BOUNCE_RETENTION);
        else if (ball.getX() > right) ball.bounceHorizontallyAt(right, BOUNCE_RETENTION);
        if (ball.getY() < top) ball.bounceVerticallyAt(top, BOUNCE_RETENTION);

        for (var goal : field.getGoalHitboxes()) {
            double closestX = Math.max(goal.getX(), Math.min(ball.getX(), goal.getX() + goal.getWidth()));
            double closestY = Math.max(goal.getY(), Math.min(ball.getY(), goal.getY() + goal.getHeight()));
            double dx = ball.getX() - closestX;
            double dy = ball.getY() - closestY;
            if (Math.hypot(dx, dy) > radius) continue;
            if (Math.abs(dx) >= Math.abs(dy)) {
                int direction = dx < 0 ? -1 : 1;
                ball.bounceHorizontallyAt(closestX + direction * radius, BOUNCE_RETENTION);
            } else {
                int direction = dy < 0 ? -1 : 1;
                ball.bounceVerticallyAt(closestY + direction * radius, BOUNCE_RETENTION);
            }
        }
    }

    private void applyParticipantImpulse(Ball ball, int awayDirection) {
        double deltaX = awayDirection * 4.0;
        ball.applyImpulse(deltaX, -1.0);
        ball.ensureHorizontalDirection(awayDirection, Math.abs(deltaX));
    }

    /** Aparta horizontalmente el balón hasta dejarlo justo fuera del participante. */
    private int separateBallFromParticipant(Ball ball, SessionParticipant participant) {
        var box = participant.getHitbox();
        double participantCenter = box.getX() + box.getWidth() / 2;
        int direction = Double.compare(ball.getX(), participantCenter);
        if (direction == 0) {
            direction = participantsLeft.contains(participant) ? 1 : -1;
        }
        double separation = 1e-6;
        double leftX = box.getX() - ball.getRadius() - separation;
        double rightX = box.getX() + box.getWidth() + ball.getRadius() + separation;
        double minCenterX = field.getHitboxField()[2].getX() + ball.getRadius() + separation;
        double maxCenterX = field.getHitboxField()[3].getX() - ball.getRadius() - separation;
        double separatedX = direction > 0 ? rightX : leftX;
        if (separatedX < minCenterX || separatedX > maxCenterX) {
            direction *= -1;
            separatedX = direction > 0 ? rightX : leftX;
        }
        ball.reposition(separatedX, ball.getY());
        return direction;
    }

    /** Coloca los participantes de ambos lados de forma simétrica respecto a la cancha. */
    protected void positionParticipants() {
        if (!participantsLeft.isEmpty() && !participantsRight.isEmpty()) {
            field.positionParticipants(participantsLeft.get(0), participantsRight.get(0));
        }
    }

    public int getGoalsLeft() { return goalsLeft; }
    public int getGoalsRight() { return goalsRight; }
    public boolean isFinished() { return finished; }
    public double getElapsedSeconds() { return timeLeft; }

    /** Devuelve la cancha de esta sesión para que la presentación pueda dibujarla. */
    public SoccerField getField() { return field; }

    /** Devuelve una copia de los participantes del lado izquierdo. */
    public List<SessionParticipant> getParticipantsLeft() { return List.copyOf(participantsLeft); }

    /** Devuelve una copia de los participantes del lado derecho. */
    public List<SessionParticipant> getParticipantsRight() { return List.copyOf(participantsRight); }
}
