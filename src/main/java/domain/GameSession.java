package domain;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import domain.GameException;
import domain.TypeOfMatch;

/**
 * Mantiene el estado de una sesión, sus participantes, la cancha, el marcador
 * y la modalidad que determina cuándo termina el partido.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class GameSession implements Serializable {
    private static final long serialVersionUID = 3293638541171582728L;
    private static GameSession instance;

    /** Devuelve la sesión singleton existente sin crear una nueva.
     * @return instancia única existente
     * @throws GameException si todavía no se ha creado la sesión
     */
    public static GameSession getInstance() throws GameException {
        if (instance == null) {
            throw new GameException(GameException.NO_FOUND_SESSION);
        }
        return instance;
    }

    /**
     * Reemplaza la sesión singleton administrada al abrir una partida guardada.
     *
     * @param restoredSession sesión deserializada que debe pasar a ser única
     * @throws GameException si la sesión restaurada es nula
     */
    public static void restoreInstance(GameSession restoredSession) throws GameException {
        if (restoredSession == null) throw new GameException(GameException.NO_FOUND_SESSION);
        restoredSession.participantsLeft.forEach(restoredSession.field::fitRestoredParticipantHitbox);
        restoredSession.participantsRight.forEach(restoredSession.field::fitRestoredParticipantHitbox);
        instance = restoredSession;
    }

    /**
     * Devuelve la sesión activa compartida por todas las clases concretas.
     * Las llamadas durante una partida la reutilizan; después de finalizarla
     * crean otra sesión y transfieren las estadísticas por identidad humana.
     * @param typeGame nombre completo de la clase concreta de sesión
     * @param player identidad humana seleccionada por las modalidades que la usan
     * @param typeMatche nombre completo de la clase de regla del partido
     * @param parametersRules dimensiones y parámetros de la regla, en el orden acordado
     * @param parametersPowers nombres de clases de poderes y, para PVB, perfil de bot
     * @return sesión singleton creada o existente
     * @throws GameException si faltan clases o constructores válidos para crearla
     */
    public static GameSession getInstance(String typeGame, boolean player,
            String typeMatche, List<String> parametersRules, List<String> parametersPowers) throws GameException {
        GameSession previous = instance;
        if (previous != null && !previous.finished) return previous;
        try {
            Class<? extends GameSession> targetClass = findSessionSubclass(typeGame);
            Constructor<?> constructor = targetClass.getDeclaredConstructor(
                    boolean.class, String.class, List.class, List.class);
            
            // La fábrica puede acceder al constructor privado de las subclases.
            constructor.setAccessible(true);
            GameSession created = GameSession.class.cast(constructor.newInstance(
                    player, typeMatche, parametersRules, parametersPowers));
            if (previous != null) created.preservePlayerProgress(previous);
            instance = created;
            return instance;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new GameException(GameException.NO_CONSTRUCTOR_FOUND);
        } catch (InstantiationException | java.lang.reflect.InvocationTargetException e) {
            throw new GameException(GameException.NO_INSTANTIATE_CLASS, e);
        }
    }

    /** Conserva puntos y mejoras al crear una partida nueva en la misma ejecución. */
    private void preservePlayerProgress(GameSession previous) {
        List<SessionParticipant> oldParticipants = new ArrayList<>(previous.participantsLeft);
        oldParticipants.addAll(previous.participantsRight);
        List<SessionParticipant> newParticipants = new ArrayList<>(participantsLeft);
        newParticipants.addAll(participantsRight);
        for (SessionParticipant current : newParticipants) {
            if (!(current instanceof Player currentPlayer)) continue;
            for (SessionParticipant old : oldParticipants) {
                if (old instanceof Player oldPlayer
                        && oldPlayer.getUserId() == currentPlayer.getUserId()) {
                    current.setStats(old.getStats());
                    break;
                }
            }
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

    /** Duración de un paso de física, en segundos. */
    static final double STEP_SECONDS = 1.0 / 60.0;
    private static final double BOUNCE_RETENTION = 0.70;
    private static final double MIN_BOUNCE_SPEED = 0.5;
    private static final double GRAVITY = 40.0;
    private static final double KICK_REACH = 8.0;
    private static final double KICK_HORIZONTAL_IMPULSE = 150.0;
    private static final double KICK_VERTICAL_IMPULSE = -50.0;

    /** Tiempo transcurrido de la sesión, en segundos. */
    private double timeLeft;
    /** Regla aplicada al reloj y al marcador para decidir el final. */
    private TypeOfMatch TypeOfMatch;
    /** Cancha que contiene los balones y los límites de esta sesión. */
    private SoccerField field;
    /** Goles anotados por el lado izquierdo. */
    private int goalsLeft;
    /** Goles anotados por el lado derecho. */
    private int goalsRight;
    /** Indica si la regla del partido dio por finalizada la sesión. */
    private boolean finished;
    /** Participantes que la modalidad asigna al lado derecho. */
    protected List<SessionParticipant> participantsRight;
    /** Participantes que la modalidad asigna al lado izquierdo. */
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
        if (parametersRules == null || parametersRules.size() < 2) {
            throw new GameException(GameException.INVALID_RULE_PARAMETERS);
        }
        double height = parseDimension(parametersRules.get(0));
        double width = parseDimension(parametersRules.get(1));
        Object typeMatcheObject = createMatchRule(typeMatche, parametersRules.subList(2, parametersRules.size()));
        TypeOfMatch typeMatcheFinal = (TypeOfMatch) typeMatcheObject;
        field = new SoccerField(height, width);
        timeLeft = 0;
        TypeOfMatch = typeMatcheFinal;
    }

    private double parseDimension(String value) {
        try {
            double dimension = Double.parseDouble(value);
            if (!Double.isFinite(dimension) || dimension <= 0) throw new NumberFormatException();
            return dimension;
        } catch (NumberFormatException exception) {
            throw new GameException(GameException.INVALID_RULE_PARAMETERS, exception);
        }
    }

    /**
     * Desplaza al participante si pertenece a esta sesión, la partida sigue activa
     * y el paso respeta los límites laterales, los arcos y las hitboxes de los demás.
     * {@code true} representa derecha y {@code false} izquierda.
     * @param user participante
     * @param direction dirección
     */
    public void movePlayer(SessionParticipant user, boolean direction) {
        movePlayer(user, direction, false);
    }

    /** Aplica el paso horizontal y permite que el bot salte si se topa con un rival. */
    void movePlayer(SessionParticipant user, boolean direction, boolean jumpOverOpponent) {
        if (finished || user == null || user.blocksMovement()
                || (!participantsRight.contains(user) && !participantsLeft.contains(user))) {
            return;
        }
        RectangularHitbox current = user.getHitbox();
        double step = user.horizontalStep();
        double nextX = current.getX() + (direction ? step : -step);
        double margin = field.boundaryThickness();
        if (nextX <= field.courtX() + margin
                || nextX + current.getWidth() >= field.courtX() + field.courtWidth() - margin) return;
        RectangularHitbox path = new RectangularHitbox(Math.min(current.getX(), nextX),
                current.getY(), current.getWidth() + step, current.getHeight());
        for (RectangularHitbox goal : field.getGoalHitboxes()) {
            if (blocksHorizontalStep(current, path, nextX, goal)) return;
        }

        List<SessionParticipant> all = new ArrayList<>(participantsLeft);
        all.addAll(participantsRight);
        for (SessionParticipant other : all) {
            if (other == user || !blocksHorizontalStep(current, path, nextX, other.getHitbox())) continue;
            if (jumpOverOpponent && participantsLeft.contains(other) != participantsLeft.contains(user)) {
                jumpPlayer(user);
            }
            return;
        }

        double previousX = current.getX();
        user.move(direction);
        field.resolveWalkingContact(user, previousX, all);
    }

    /** Inicia el salto de un participante de la sesión apoyado en el suelo o en otro jugador.
     * @param user participante que solicita el salto
     */
    public void jumpPlayer(SessionParticipant user) {
        if (finished || user == null
                || (!participantsLeft.contains(user) && !participantsRight.contains(user))) return;
        user.jumpFromSupport(jumpSupportY(user), field.courtY() + field.boundaryThickness());
    }

    /** Localiza el borde de apoyo que coincide con los pies; usa el suelo como referencia en el aire. */
    final double jumpSupportY(SessionParticipant user) {
        RectangularHitbox box = user.getHitbox();
        double ground = field.groundY();
        if (box.getY() == ground - box.getHeight()) return ground;

        return Stream.concat(participantsLeft.stream(), participantsRight.stream())
                .filter(other -> other != user)
                .map(SessionParticipant::getHitbox)
                .filter(other -> box.getY() == other.getY() - box.getHeight()
                        && hasHorizontalOverlap(box, other))
                .mapToDouble(RectangularHitbox::getY)
                .findFirst()
                .orElse(ground);
    }

    /** Avanza los saltos y recorta sus recorridos contra cancha, arcos y participantes. */
    private void advanceJumps(List<SessionParticipant> participants) {
        RectangularHitbox[] goals = field.getGoalHitboxes();
        double ceiling = Math.nextUp(field.courtY() + field.boundaryThickness());
        for (SessionParticipant participant : participants) {
            RectangularHitbox box = participant.getHitbox();
            double nextY = participant.nextJumpY(STEP_SECONDS);
            if (nextY == box.getY()) continue;
            double groundTop = field.groundY() - box.getHeight();
            double allowedY = Math.clamp(nextY, ceiling, groundTop);
            for (RectangularHitbox goal : goals) {
                allowedY = limitVerticalStep(box, goal, allowedY);
            }
            for (SessionParticipant other : participants) {
                if (other != participant) allowedY = limitVerticalStep(box, other.getHitbox(), allowedY);
            }
            participant.applyJumpStep(nextY, allowedY, groundTop);
        }
    }

    /** Devuelve la altura alcanzable antes del primer borde que corta el recorrido vertical. */
    private double limitVerticalStep(RectangularHitbox box, RectangularHitbox obstacle, double nextY) {
        if (!hasHorizontalOverlap(box, obstacle)) return nextY;
        double obstacleBottom = obstacle.getY() + obstacle.getHeight();
        if (obstacle.getY() <= box.getY() - obstacle.getHeight() && nextY < obstacleBottom) {
            return Math.min(box.getY(), obstacleBottom);
        }
        double obstacleTop = obstacle.getY() - box.getHeight();
        if (box.getY() <= obstacleTop && nextY > obstacleTop) {
            return obstacleTop;
        }
        return nextY;
    }

    /** Comprueba que las proyecciones X compartan superficie de apoyo. */
    private static boolean hasHorizontalOverlap(RectangularHitbox first, RectangularHitbox second) {
        return first.getX() > second.getX() - first.getWidth()
                && second.getX() > first.getX() - second.getWidth();
    }

    /** Impide cruzar un obstáculo y permite alejarse si el contacto ya existía. */
    private boolean blocksHorizontalStep(RectangularHitbox current, RectangularHitbox path,
            double nextX, RectangularHitbox obstacle) {
        if (current.getY() <= obstacle.getY() - current.getHeight()
                || obstacle.getY() <= current.getY() - obstacle.getHeight()) return false;
        if (!CollisionDetector.intersects(path, obstacle)) return false;
        if (!CollisionDetector.intersects(current, obstacle)) return true;
        double separation = current.getX() + current.getWidth() / 2
                - obstacle.getX() - obstacle.getWidth() / 2;
        double displacement = nextX - current.getX();
        return separation != 0 && separation * displacement <= 0;
    }

    /** Reinicia marcador, reloj y estado físico del partido, conservando estadísticas.
     */
    public void restartRound() {
        goalsLeft = 0;
        goalsRight = 0;
        timeLeft = 0;
        finished = false;
        if (TypeOfMatch instanceof ByTime timeRule) timeRule.reset();
        for (SessionParticipant participant : participantsLeft) participant.resetForNewMatch();
        for (SessionParticipant participant : participantsRight) participant.resetForNewMatch();
        resetPositionsAfterGoal();
    }

    /** Recoloca balón y participantes al registrar un gol, sin reiniciar la partida. */
    private void resetPositionsAfterGoal() {
        resetRoundInputState();
        field.resetBall(field.courtX() + field.courtWidth() / 2,
                field.courtY() + field.courtHeight() / 2);
        positionParticipants();
    }

    /** Punto de extensión para limpiar entradas retenidas tras gol o reinicio. */
    public void resetRoundInputState() { }


    /**
     * Registra el gol si {@code side} es {@code "left"} o {@code "right"}; en
     * cualquier otro caso no cambia el marcador. Añade un cuarto de carga a
     * los poderes del lado que recibió el gol. Después reinicia la ronda y
     * comprueba la regla del partido.
     * @param side lado que anotó; se aceptan {@code "left"} y {@code "right"}
     */
    public void goal(String side) {
        if ("left".equals(side)) goalsLeft++;
        else if ("right".equals(side)) goalsRight++;
        else return;
        List<SessionParticipant> concedingSide = "left".equals(side)
                ? participantsRight : participantsLeft;
        for (SessionParticipant participant : concedingSide) {
            if (participant.getSpecialPower() != null) participant.getSpecialPower().addGoalBonus();
        }
        resetPositionsAfterGoal();
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
            if (isBallInKickReach(box, ball)) {
                ball.applyImpulse(direction * KICK_HORIZONTAL_IMPULSE, KICK_VERTICAL_IMPULSE);
                return true;
            }
        }
        return false;
    }

    /** Comparte la regla de alcance de la patada con la observación del bot. */
    static boolean isBallInKickReach(RectangularHitbox box, Ball ball) {
        double closestX = Math.max(box.getX(), Math.min(ball.getX(), box.getX() + box.getWidth()));
        double closestY = Math.max(box.getY(), Math.min(ball.getY(), box.getY() + box.getHeight()));
        return Math.hypot(ball.getX() - closestX, ball.getY() - closestY) <= ball.getRadius() + KICK_REACH;
    }

    /** Busca el rival más cercano cuya hitbox esté dentro del alcance de patada. */
    private SessionParticipant closestOpponentInReach(
            domain.RectangularHitbox attacker, List<SessionParticipant> opponents) {
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
     * terminar, otorga puntos a cada participante humano una sola vez según
     * el resultado: 100 por victoria y 25 por derrota o empate.
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
            awardMatchPoints();
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

    /** Reparte los puntos del resultado a los jugadores humanos, sin premiar al bot. */
    private void awardMatchPoints() {
        if (goalsLeft == goalsRight) {
            awardDrawPoints(participantsLeft);
            awardDrawPoints(participantsRight);
            return;
        }
        boolean rightWon = goalsRight > goalsLeft;
        awardSideResult(participantsLeft, !rightWon);
        awardSideResult(participantsRight, rightWon);
    }

    /** Otorga puntos de empate a los participantes humanos del lado indicado. */
    private void awardDrawPoints(List<SessionParticipant> side) {
        for (SessionParticipant participant : side) {
            if (participant instanceof Player) participant.getStats().awardDraw();
        }
    }

    /** Otorga puntos de victoria o derrota a los participantes humanos del lado indicado. */
    private void awardSideResult(List<SessionParticipant> side, boolean won) {
        for (SessionParticipant participant : side) {
            if (participant instanceof Player) {
                if (won) participant.getStats().awardWin();
                else participant.getStats().awardLoss();
            }
        }
    }

    /**
     * Avanza una sesión activa un paso fijo: recarga poderes, actualiza efectos temporales,
     * procesa participantes autónomos, incrementa el tiempo, integra el balón
     * con gravedad, procesa contactos y
     * vuelve a consultar la regla si la sesión sigue activa.
     */
    public void updateGame() {
        if (finished) return;
        for (SessionParticipant participant : participantsLeft) {
            if (participant.getSpecialPower() != null) participant.getSpecialPower().update();
        }
        for (SessionParticipant participant : participantsRight) {
            if (participant.getSpecialPower() != null) participant.getSpecialPower().update();
        }
        for (SessionParticipant participant : participantsLeft) participant.updateStatusEffects();
        for (SessionParticipant participant : participantsRight) participant.updateStatusEffects();
        updateAutonomousParticipants();
        List<SessionParticipant> allParticipants = new ArrayList<>(participantsLeft);
        allParticipants.addAll(participantsRight);
        advanceJumps(allParticipants);
        timeLeft += STEP_SECONDS;
        field.advanceBalls(STEP_SECONDS, GRAVITY);
        collision(allParticipants);
        for (SessionParticipant participant : allParticipants) participant.rememberCurrentPosition();
        if (!finished) checkTypeOfMatch();
    }

    /** Ejecuta las decisiones autónomas de la modalidad, si tiene participantes de ese tipo. */
    public void updateAutonomousParticipants() { }

    /**
     * Valida la posición de los participantes, desplaza los balones apoyados,
     * recupera contactos atravesados entre pasos y comprueba si hay gol. Si no hay
     * gol, resuelve los rebotes entre balones, luego el contacto con participantes
     * y finalmente los rebotes contra el contorno de la cancha y los arcos.
     * @param allParticipants todos los participantes que se actualizaron en el paso
     * @throws GameException si un participante está fuera del área permitida
     */
    private void collision(List<SessionParticipant> allParticipants) {
        for (SessionParticipant participant : allParticipants) {
            if (field.isOutside(participant) || field.touchesForbiddenBoundary(participant)) {
                throw new GameException(GameException.OBJECT_OUT_OF_MATCH);
            }
        }
        field.rollSupportedBalls(allParticipants, STEP_SECONDS, GRAVITY);
        field.rewindMissedBallParticipantContacts(allParticipants);
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

    /** Refleja los lados de una partida guardada con la orientación anterior. */
    public final void mirrorPlayingSides() {
        List<SessionParticipant> oldLeft = participantsLeft;
        participantsLeft = participantsRight;
        participantsRight = oldLeft;

        int oldLeftGoals = goalsLeft;
        goalsLeft = goalsRight;
        goalsRight = oldLeftGoals;

        double axis = 2 * field.courtX() + field.courtWidth();
        for (SessionParticipant participant : participantsLeft) mirrorParticipant(participant, axis);
        for (SessionParticipant participant : participantsRight) mirrorParticipant(participant, axis);
        for (Ball ball : field.balls()) {
            ball.reposition(axis - ball.getX(), ball.getY());
            ball.applyImpulse(-2 * ball.getVelocityX(), 0);
        }
    }

    /** Refleja la esquina de la hitbox y sincroniza la posición anterior. */
    private static void mirrorParticipant(SessionParticipant participant, double axis) {
        RectangularHitbox hitbox = participant.getHitbox();
        participant.resetPosition(axis - hitbox.getX() - hitbox.getWidth(), hitbox.getY());
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

    /** Devuelve los goles anotados por el lado izquierdo.
     * @return goles del lado izquierdo
     */
    public int getGoalsLeft() { return goalsLeft; }
    /** Devuelve los goles anotados por el lado derecho.
     * @return goles del lado derecho
     */
    public int getGoalsRight() { return goalsRight; }
    /** Indica si la regla del partido ya terminó la sesión.
     * @return {@code true} si la sesión terminó
     */
    public boolean isFinished() { return finished; }
    /** Devuelve el tiempo transcurrido de la sesión.
     * @return tiempo transcurrido en segundos
     */
    public double getElapsedSeconds() { return timeLeft; }

    /** Devuelve la cancha de esta sesión para que la presentación pueda dibujarla.
     * @return cancha de la sesión
     */
    public SoccerField getField() { return field; }
    /** Devuelve la regla que determina el final del partido.
     * @return regla de partido activa
     */
    public TypeOfMatch getTypeOfMatch() { return TypeOfMatch; }

    /** Devuelve una copia de los participantes del lado izquierdo.
     * @return lista inmutable de participantes de ese lado
     */
    public List<SessionParticipant> getParticipantsLeft() { return List.copyOf(participantsLeft); }

    /** Devuelve una copia de los participantes del lado derecho.
     * @return lista inmutable de participantes de ese lado
     */
    public List<SessionParticipant> getParticipantsRight() { return List.copyOf(participantsRight); }
}
