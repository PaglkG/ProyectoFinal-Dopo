package domain;

import java.util.List;
import java.util.Objects;

/** Immutable snapshot exposed by session inspection. */
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

    public SessionView(RectangleView court, List<RectangleView> boundaries, List<RectangleView> goals,
            List<ParticipantView> participants, List<BallView> balls, int goalsLeft, int goalsRight,
            double elapsedSeconds, boolean finished) {
        this.court = court; this.boundaries = List.copyOf(boundaries); this.goals = List.copyOf(goals);
        this.participants = List.copyOf(participants); this.balls = List.copyOf(balls);
        this.goalsLeft = goalsLeft; this.goalsRight = goalsRight;
        this.elapsedSeconds = elapsedSeconds; this.finished = finished;
    }
    public RectangleView court() { return court; }
    public List<RectangleView> boundaries() { return boundaries; }
    public List<RectangleView> goals() { return goals; }
    public List<ParticipantView> participants() { return participants; }
    public List<BallView> balls() { return balls; }
    public int goalsLeft() { return goalsLeft; }
    public int goalsRight() { return goalsRight; }
    public double elapsedSeconds() { return elapsedSeconds; }
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
