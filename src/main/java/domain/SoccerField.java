package domain;

import java.util.Arrays;
import domain.hitbox.Hitbox;
import domain.hitbox.HitboxContact;
import domain.hitbox.CircularHitbox;
import domain.hitbox.RectangularHitbox;

/**
 * Representa la cancha, sus hitboxes y los balones que contiene.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class SoccerField implements Hitbox {

    private RectangularHitbox LeftGoalHitbox;
    private RectangularHitbox RightGoalHitbox;
    private RectangularHitbox[] fieldHitbox;
    private Ball[] balls;

    /**
     * Crea una cancha provisional de 450 por 150 unidades y un balón de radio 2.
     */
    public SoccerField() {
        balls = new Ball[] {new Ball(450, 150, 2)};
        reposition(0, 0);
    }
    public Hitbox[] hitboxs() {
        Hitbox[] hitbox = new Hitbox[fieldHitbox.length + 2 + balls.length];
        System.arraycopy(fieldHitbox, 0, hitbox, 0, fieldHitbox.length);
        hitbox[fieldHitbox.length] = LeftGoalHitbox;
        hitbox[fieldHitbox.length + 1] = RightGoalHitbox;
        for (int i = 0; i < balls.length; i++) {
            hitbox[fieldHitbox.length + 2 + i] = balls[i];
        }
        return hitbox;
    }
    /**
     * Reinicia los balones para que su centro quede en las coordenadas indicadas.
     * Para usar el reinicio por dimensiones de {@link Ball}, se envían ancho
     * {@code 2 * x} y alto {@code 2 * y}; el reinicio también deja sus velocidades
     * en cero.
     *
     * @param x coordenada horizontal del centro
     * @param y coordenada vertical del centro
     */
    public void resetBall(double x, double y) {
        for (Ball ball : balls) {
            ball.resetBall(x * 2, y * 2);
        }
    }

    /**
     * Devuelve las cuatro hitboxes que delimitan la cancha.
     *
     * @return hitboxes superior, inferior, izquierda y derecha
     */
    public RectangularHitbox[] getHitboxField() {
        return fieldHitbox;
    }

    /**
     * Devuelve las hitboxes circulares asociadas a los balones de esta cancha.
     *
     * @return arreglo con la hitbox de cada balón
     */
    public CircularHitbox[] getHitboxBalls() {
        return Arrays.stream(balls)
                .map(Ball::getHitbox)
                .toArray(CircularHitbox[]::new);
    }

    /** Devuelve una copia del arreglo de balones de la cancha. */
    public Ball[] getBalls() {
        return balls.clone();
    }

    /** Devuelve las hitboxes de los arcos: izquierda y derecha, en ese orden. */
    public RectangularHitbox[] getGoalHitboxes() {
        return new RectangularHitbox[] {LeftGoalHitbox, RightGoalHitbox};
    }

    Ball[] balls() {
        return balls.clone();
    }

    double courtX() { return fieldHitbox[0].getX(); }
    double courtY() { return fieldHitbox[0].getY(); }
    double courtWidth() { return fieldHitbox[0].getWidth(); }
    double courtHeight() { return fieldHitbox[2].getHeight(); }
    double boundaryThickness() { return fieldHitbox[0].getHeight(); }
    double groundY() { return fieldHitbox[1].getY(); }

    /** Devuelve el arco cuyo rectángulo contiene el centro del balón, si existe. */
    String goalSideFor(Ball ball) {
        if (containsCenter(LeftGoalHitbox, ball)) return "left";
        if (containsCenter(RightGoalHitbox, ball)) return "right";
        return "";
    }

    private boolean containsCenter(RectangularHitbox goal, Ball ball) {
        return ball.getX() >= goal.getX() && ball.getX() <= goal.getX() + goal.getWidth()
                && ball.getY() >= goal.getY() && ball.getY() <= goal.getY() + goal.getHeight();
    }

    boolean isOutside(Ball ball) {
        double radius = ball.getRadius();
        return ball.getX() - radius < courtX() || ball.getX() + radius > courtX() + courtWidth()
                || ball.getY() - radius < courtY() || ball.getY() + radius > courtY() + courtHeight();
    }

    boolean isOutside(SessionParticipant participant) {
        RectangularHitbox box = participant.getHitbox();
        return box.getX() < courtX() || box.getX() + box.getWidth() > courtX() + courtWidth()
                || box.getY() < courtY() || box.getY() + box.getHeight() > courtY() + courtHeight();
    }

    boolean touchesForbiddenBoundary(SessionParticipant participant) {
        return fieldHitbox[0].intersects(participant).isContact()
                || fieldHitbox[2].intersects(participant).isContact()
                || fieldHitbox[3].intersects(participant).isContact();
    }

    boolean touchesGround(Ball ball) {
        return fieldHitbox[1].intersects(ball).isContact();
    }

    double groundCenterY(Ball ball) {
        return groundY() - ball.getRadius();
    }

    void positionParticipants(SessionParticipant left, SessionParticipant right) {
        double width = courtWidth() / 20;
        double height = courtHeight() / 3;
        double floor = groundY();
        left.resizeHitbox(width, height);
        right.resizeHitbox(width, height);
        left.reposition(courtX() + courtWidth() / 4 - width / 2, floor - height);
        right.reposition(courtX() + courtWidth() * 3 / 4 - width / 2, floor - height);
    }


    /**
     * Busca el primer objeto de dominio que toca cualquiera de las partes de
     * esta cancha. El resultado identifica el objeto recibido y su hitbox.
     *
     * @param hitbox objeto cuya hitbox se comparará con la cancha
     * @return información de la entidad contactada, o un resultado con nombre
     *         vacío si no hay contacto
     */
    @Override
    public HitboxContact intersects(Hitbox hitbox) {
        if (hitbox == null) {
            return HitboxContact.none();
        }
        for (RectangularHitbox boundary : fieldHitbox) {
            HitboxContact contact = boundary.intersects(hitbox);
            if (contact.isContact()) {
                return contact;
            }
        }
        HitboxContact leftGoalContact = LeftGoalHitbox.intersects(hitbox);
        if (leftGoalContact.isContact()) {
            return leftGoalContact;
        }
        HitboxContact rightGoalContact = RightGoalHitbox.intersects(hitbox);
        if (rightGoalContact.isContact()) {
            return rightGoalContact;
        }
        for (Ball ball : balls) {
            HitboxContact contact = ball.intersects(hitbox);
            if (contact.isContact()) {
                return contact;
            }
        }
        return HitboxContact.none();
    }

    /**
     * Identifica qué parte propia de la cancha toca al objeto recibido. Se usa
     * cuando la cancha es el objeto contactado, de modo que el resultado puede
     * señalar un límite/arco o un balón contenido.
     *
     * @param other objeto que toca la cancha
     * @return cancha y la hitbox concreta tocada, o balón y su hitbox si toca
     *         uno contenido; resultado vacío si no hay contacto
     */
    public HitboxContact contactingPart(Hitbox other) {
        if (other == null) {
            return HitboxContact.none();
        }
        for (RectangularHitbox boundary : fieldHitbox) {
            if (boundary.intersects(other).isContact()) {
                return new HitboxContact("SoccerField", boundary);
            }
        }
        if (LeftGoalHitbox.intersects(other).isContact()) {
            return new HitboxContact("SoccerField", LeftGoalHitbox);
        }
        if (RightGoalHitbox.intersects(other).isContact()) {
            return new HitboxContact("SoccerField", RightGoalHitbox);
        }
        for (Ball ball : balls) {
            if (ball.intersects(other).isContact()) {
                return new HitboxContact("Ball", ball.getHitbox());
            }
        }
        return HitboxContact.none();
    }

    /**
     * Reposiciona las hitboxes de la cancha conservando su distribución.
     *
     * @param x nueva coordenada horizontal de la esquina superior izquierda
     * @param y nueva coordenada vertical de la esquina superior izquierda
     */
    @Override
    public void reposition(double x, double y) {
        double width = 450;
        double height = 150;
        double boundaryThickness = height / 100;
        double goalWidth = width / 10;
        double goalHeight = height / 3;
        double goalY = y + (height - goalHeight) / 2;
        double previousCenterX = fieldHitbox == null ? x + width / 2 : courtX() + width / 2;
        double previousCenterY = fieldHitbox == null ? y + height / 2 : courtY() + height / 2;

        if (LeftGoalHitbox == null) {
            LeftGoalHitbox = new RectangularHitbox(x, goalY, goalWidth, goalHeight);
        }
        else {
            LeftGoalHitbox.reposition(x, goalY);
        }
        for (Ball ball : balls) {
            ball.reposition(ball.getX() + (x + width / 2 - previousCenterX),
                    ball.getY() + (y + height / 2 - previousCenterY));
        }
        if (RightGoalHitbox == null) {
            RightGoalHitbox = new RectangularHitbox(x + width - goalWidth, goalY, goalWidth, goalHeight);
        }
        else {
            RightGoalHitbox.reposition(x + width - goalWidth, goalY);
        }
        if (fieldHitbox == null) {
            fieldHitbox = new RectangularHitbox[] {
                    new RectangularHitbox(x, y, width, boundaryThickness),
                    new RectangularHitbox(x, y + height - boundaryThickness, width, boundaryThickness),
                    new RectangularHitbox(x, y, boundaryThickness, height),
                    new RectangularHitbox(x + width - boundaryThickness, y, boundaryThickness, height)
            };
        }
        else {
            fieldHitbox[0].reposition(x, y);
            fieldHitbox[1].reposition(x, y + height - boundaryThickness);
            fieldHitbox[2].reposition(x, y);
            fieldHitbox[3].reposition(x + width - boundaryThickness, y);
        }
    }
}
