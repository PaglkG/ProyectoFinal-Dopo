package domain;

import java.util.List;
import java.util.Objects;

/** Instantánea inmutable del estado expuesto por la inspección de sesión. */
public final class SessionView {
    private final RectangleView court;
    private final List<RectangleView> boundaries;
    private final List<RectangleView> goals;
    private final List<ParticipantView> participants;
    private final List<BallView> balls;
    private final int goalsLeft;
    private final int goalsRight;
    private final double elapsedSeconds;
    private final boolean finished;

    /** Crea una instantánea de la cancha, las entidades y el avance del partido.
     * @param court rectángulo exterior de la cancha
     * @param boundaries límites en orden superior, inferior, izquierdo y derecho
     * @param goals arcos en orden izquierdo y derecho
     * @param participants participantes de ambos lados
     * @param balls balones de la sesión
     * @param goalsLeft goles del lado izquierdo
     * @param goalsRight goles del lado derecho
     * @param elapsedSeconds tiempo transcurrido en segundos
     * @param finished indica si la sesión terminó
     */
    public SessionView(RectangleView court, List<RectangleView> boundaries, List<RectangleView> goals,
            List<ParticipantView> participants, List<BallView> balls, int goalsLeft, int goalsRight,
            double elapsedSeconds, boolean finished) {
        this.court = court; this.boundaries = List.copyOf(boundaries); this.goals = List.copyOf(goals);
        this.participants = List.copyOf(participants); this.balls = List.copyOf(balls);
        this.goalsLeft = goalsLeft; this.goalsRight = goalsRight;
        this.elapsedSeconds = elapsedSeconds; this.finished = finished;
    }
    /** Devuelve la geometría exterior de la cancha.
     * @return rectángulo de cancha
     */
    public RectangleView court() { return court; }
    /** Devuelve los límites en orden superior, inferior, izquierdo y derecho.
     * @return lista inmutable de límites
     */
    public List<RectangleView> boundaries() { return boundaries; }
    /** Devuelve los arcos en orden izquierdo y derecho.
     * @return lista inmutable de arcos
     */
    public List<RectangleView> goals() { return goals; }
    /** Devuelve los participantes de la sesión.
     * @return lista inmutable de participantes
     */
    public List<ParticipantView> participants() { return participants; }
    /** Devuelve los balones de la sesión.
     * @return lista inmutable de balones
     */
    public List<BallView> balls() { return balls; }
    /** Devuelve los goles del lado izquierdo.
     * @return goles anotados por el lado izquierdo
     */
    public int goalsLeft() { return goalsLeft; }
    /** Devuelve los goles del lado derecho.
     * @return goles anotados por el lado derecho
     */
    public int goalsRight() { return goalsRight; }
    /** Devuelve el tiempo de juego transcurrido.
     * @return segundos transcurridos
     */
    public double elapsedSeconds() { return elapsedSeconds; }
    /** Indica si la sesión terminó.
     * @return {@code true} si la partida finalizó
     */
    public boolean finished() { return finished; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SessionView that)) return false;
        return goalsLeft == that.goalsLeft && goalsRight == that.goalsRight && finished == that.finished
                && Double.compare(elapsedSeconds, that.elapsedSeconds) == 0 && Objects.equals(court, that.court)
                && Objects.equals(boundaries, that.boundaries) && Objects.equals(goals, that.goals)
                && Objects.equals(participants, that.participants) && Objects.equals(balls, that.balls);
    }
    @Override public int hashCode() {
        return Objects.hash(court, boundaries, goals, participants, balls, goalsLeft, goalsRight, elapsedSeconds, finished);
    }
    @Override public String toString() {
        return "SessionView[court=" + court + ", boundaries=" + boundaries + ", goals=" + goals
                + ", participants=" + participants + ", balls=" + balls + ", goalsLeft=" + goalsLeft
                + ", goalsRight=" + goalsRight + ", elapsedSeconds=" + elapsedSeconds + ", finished=" + finished + "]";
    }
}
