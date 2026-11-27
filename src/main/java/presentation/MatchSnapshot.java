package presentation;

import java.util.List;
import java.util.Optional;

/**
 * Captura inmutable de los datos que la ventana lee de {@code Game} en
 * {@code DOPOSoccerGUI.publishMatch()}. La ventana publica una captura nueva
 * al iniciar, avanzar el juego o completar una compra. Los paneles la
 * consultan con {@link DOPOSoccerGUI.PresentationState#getMatch()}.
 * {@link #getParticipant(boolean)} selecciona un lado de la cancha;
 * {@link #getHumanPlayer(boolean)} selecciona una identidad humana.
 */
public final class MatchSnapshot {
    private final MatchConfiguration configuration;
    private final Bounds court;
    private final List<Bounds> goals;
    private final List<Ball> balls;
    private final Participant left;
    private final Participant right;
    private final int leftGoals;
    private final int rightGoals;
    private final double elapsedSeconds;
    private final boolean suddenDeath;
    private final boolean finished;

    /**
     * Reúne los datos del mismo paso y copia las listas que recibe.
     * @param configuration configuración con la que se inició la sesión
     * @param court rectángulo exterior de la cancha
     * @param goals rectángulos de los arcos izquierdo y derecho, en ese orden
     * @param balls balones en el orden de índices de la fachada
     * @param left participante del lado izquierdo
     * @param right participante del lado derecho
     * @param leftGoals goles anotados por el lado izquierdo
     * @param rightGoals goles anotados por el lado derecho
     * @param elapsedSeconds segundos transcurridos
     * @param suddenDeath indica si la regla temporal está en muerte súbita
     * @param finished indica si la partida terminó
     */
    MatchSnapshot(MatchConfiguration configuration, Bounds court, List<Bounds> goals,
            List<Ball> balls, Participant left, Participant right, int leftGoals, int rightGoals,
            double elapsedSeconds, boolean suddenDeath, boolean finished) {
        this.configuration = configuration;
        this.court = court;
        this.goals = List.copyOf(goals);
        this.balls = List.copyOf(balls);
        this.left = left;
        this.right = right;
        this.leftGoals = leftGoals;
        this.rightGoals = rightGoals;
        this.elapsedSeconds = elapsedSeconds;
        this.suddenDeath = suddenDeath;
        this.finished = finished;
    }

    /** Devuelve la configuración del partido.
     * @return configuración inmutable elegida antes de iniciar
     */
    public MatchConfiguration getConfiguration() { return configuration; }

    /** Devuelve los límites exteriores de la cancha.
     * @return rectángulo inmutable de la cancha
     */
    public Bounds getCourt() { return court; }

    /** Devuelve los rectángulos de gol en orden izquierda, derecha.
     * @return lista inmutable de arcos
     */
    public List<Bounds> getGoals() { return goals; }

    /** Devuelve los balones presentes en el paso capturado.
     * @return lista inmutable de balones
     */
    public List<Ball> getBalls() { return balls; }

    /** Devuelve los datos del participante por lado físico.
     * @param rightSide indica si se consulta el lado derecho
     * @return participante del lado indicado
     */
    public Participant getParticipant(boolean rightSide) { return rightSide ? right : left; }

    /** Devuelve el participante humano por identidad.
     * @param playerOne indica si se consulta al jugador 1
     * @return participante humano, o vacío si esa identidad no está en la partida
     */
    public Optional<Participant> getHumanPlayer(boolean playerOne) {
        if (configuration.againstBot()) {
            // El humano seleccionado está a la izquierda y la otra identidad no se ofrece a la tienda.
            return playerOne == configuration.playerOne() ? Optional.of(left) : Optional.empty();
        }
        return Optional.of(getParticipant(playerOne));
    }

    /** Devuelve los goles del lado izquierdo.
     * @return goles del lado izquierdo
     */
    public int getLeftGoals() { return leftGoals; }

    /** Devuelve los goles del lado derecho.
     * @return goles del lado derecho
     */
    public int getRightGoals() { return rightGoals; }

    /** Devuelve el tiempo transcurrido en la sesión.
     * @return segundos transcurridos
     */
    public double getElapsedSeconds() { return elapsedSeconds; }

    /** Indica si la sesión está en muerte súbita.
     * @return estado de la muerte súbita
     */
    public boolean isSuddenDeath() { return suddenDeath; }

    /** Indica si la regla del partido dio por terminada la sesión.
     * @return estado de finalización
     */
    public boolean isFinished() { return finished; }

    /** Rectángulo inmutable en coordenadas de juego. */
    public static final class Bounds {
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        /** Guarda una esquina superior izquierda y sus dimensiones. */
        Bounds(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        /** Devuelve la posición horizontal de la esquina.
         * @return coordenada X
         */
        public double getX() { return x; }

        /** Devuelve la posición vertical de la esquina.
         * @return coordenada Y
         */
        public double getY() { return y; }

        /** Devuelve el ancho del rectángulo.
         * @return ancho
         */
        public double getWidth() { return width; }

        /** Devuelve el alto del rectángulo.
         * @return alto
         */
        public double getHeight() { return height; }
    }

    /** Geometría y velocidad inmutables de un balón. */
    public static final class Ball {
        private final double x;
        private final double y;
        private final double radius;
        private final double velocityX;
        private final double velocityY;

        /** Guarda el centro, el radio y las dos componentes de velocidad. */
        Ball(double x, double y, double radius, double velocityX, double velocityY) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        /** Devuelve la posición horizontal del centro.
         * @return coordenada X
         */
        public double getX() { return x; }

        /** Devuelve la posición vertical del centro.
         * @return coordenada Y
         */
        public double getY() { return y; }

        /** Devuelve el radio.
         * @return radio del balón
         */
        public double getRadius() { return radius; }

        /** Devuelve la componente horizontal de la velocidad.
         * @return velocidad en X
         */
        public double getVelocityX() { return velocityX; }

        /** Devuelve la componente vertical de la velocidad.
         * @return velocidad en Y
         */
        public double getVelocityY() { return velocityY; }
    }

    /** Datos inmutables de un participante disponibles desde la fachada del juego. */
    public static final class Participant {
        private final Bounds bounds;
        private final int life;
        private final int maxLife;
        private final double powerRemainingSeconds;
        private final int points;
        private final int speedLevel;
        private final int jumpLevel;
        private final int kickLevel;
        private final int nextUpgradeCost;

        /** Guarda la geometría, estado de juego, estadísticas y costo de mejora del participante. */
        Participant(Bounds bounds, int life, int maxLife, double powerRemainingSeconds,
                int points, int speedLevel, int jumpLevel, int kickLevel, int nextUpgradeCost) {
            this.bounds = bounds;
            this.life = life;
            this.maxLife = maxLife;
            this.powerRemainingSeconds = powerRemainingSeconds;
            this.points = points;
            this.speedLevel = speedLevel;
            this.jumpLevel = jumpLevel;
            this.kickLevel = kickLevel;
            this.nextUpgradeCost = nextUpgradeCost;
        }

        /** Devuelve la hitbox del participante.
         * @return rectángulo inmutable
         */
        public Bounds getBounds() { return bounds; }

        /** Devuelve la vida actual.
         * @return puntos de vida actuales
         */
        public int getLife() { return life; }

        /** Devuelve el límite de vida actual.
         * @return puntos de vida máximos
         */
        public int getMaxLife() { return maxLife; }

        /** Devuelve cuánto falta para recargar el poder.
         * @return segundos restantes
         */
        public double getPowerRemainingSeconds() { return powerRemainingSeconds; }

        /** Devuelve los puntos disponibles para mejoras.
         * @return saldo de puntos
         */
        public int getPoints() { return points; }

        /** Devuelve el nivel de velocidad.
         * @return nivel actual de velocidad
         */
        public int getSpeedLevel() { return speedLevel; }

        /** Devuelve el nivel de salto.
         * @return nivel actual de salto
         */
        public int getJumpLevel() { return jumpLevel; }

        /** Devuelve el nivel de patada.
         * @return nivel actual de patada
         */
        public int getKickLevel() { return kickLevel; }

        /** Devuelve el costo de la siguiente mejora.
         * @return costo en puntos
         */
        public int getNextUpgradeCost() { return nextUpgradeCost; }
    }
}
