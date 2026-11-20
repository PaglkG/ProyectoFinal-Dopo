package domain;

import java.util.Arrays;
import domain.hitbox.Hitbox;
import domain.hitbox.HitboxContact;
import domain.hitbox.CircularHitbox;
import domain.hitbox.RectangularHitbox;
import domain.GameException;
import domain.collision.CollisionFacade;

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
    private CollisionFacade collisions;

    /**
     * Crea una cancha provisional de 450 por 150 unidades y un balón de radio 10.
     */
    public SoccerField() {
        balls = new Ball[] {new Ball(450, 150, 10)};
        reposition(0, 0);
        collisions = new CollisionFacade(this);
    }

    /** Avanza todos los balones contenidos usando el paso y gravedad indicados. */
    public void advanceBalls(double deltaSeconds, double gravity) {
        for (Ball ball : balls) {
            ball.advance(deltaSeconds, gravity);
        }
    }

    /** Agrega a la cancha un balón nuevo, conservando sus coordenadas y estado iniciales. */
    public void addBall(Ball ball) {
        if (ball == null || Arrays.stream(balls).anyMatch(existing -> existing == ball)) {
            throw new GameException(GameException.INVALID_BALL);
        }
        Ball[] expanded = Arrays.copyOf(balls, balls.length + 1);
        expanded[balls.length] = ball;
        balls = expanded;
    }

    /** Resuelve las colisiones entre todos los pares de balones con masas iguales. */
    public void resolveBallCollisions(double restitution) {
        collisions.resolveBallCollisions(restitution);
    }

    /** Resuelve la penetración y el rebote de cada balón contra participantes. */
    public void resolveBallParticipantCollisions(Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        collisions.resolveBallParticipantCollisions(participants, restitution, minimumLandingSpeed);
    }

    /** Resuelve contactos creados por caminar: empuja balones quietos y bloquea el paso ante los móviles. */
    public void resolveWalkingContact(SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants, double restitution) {
        collisions.resolveWalkingContact(moved, previousX, participants, restitution);
    }

    /** Corrige la salida del balón y aplica rebote hacia el interior en cada borde de cancha. */
    public void keepBallInsideCourt(Ball ball, double retention) {
        collisions.keepBallInsideCourt(ball, retention);
    }

    /**
     * Reúne en un arreglo los límites, los dos arcos y cada balón de la cancha.
     * El orden es límites, arco izquierdo, arco derecho y balones.
     *
     * @return arreglo de objetos que exponen hitboxes
     */
    public Hitbox[] getHitboxes() {
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

    /** @return copia del arreglo de balones, sin copiar los objetos contenidos */
    public Ball[] getBalls() {
        return balls.clone();
    }

    /** @return hitboxes de los arcos, primero el izquierdo y luego el derecho */
    public RectangularHitbox[] getGoalHitboxes() {
        return new RectangularHitbox[] {LeftGoalHitbox, RightGoalHitbox};
    }

    /** @return copia interna del arreglo de balones para cálculos del dominio */
    public Ball[] balls() {
        return balls.clone();
    }

    /** @return coordenada horizontal izquierda del rectángulo exterior */
    public double courtX() { return fieldHitbox[0].getX(); }
    /** @return coordenada vertical superior del rectángulo exterior */
    public double courtY() { return fieldHitbox[0].getY(); }
    /** @return ancho del rectángulo exterior */
    public double courtWidth() { return fieldHitbox[0].getWidth(); }
    /** @return alto del rectángulo exterior */
    public double courtHeight() { return fieldHitbox[2].getHeight(); }
    /** @return grosor de los límites superior e inferior */
    public double boundaryThickness() { return fieldHitbox[0].getHeight(); }
    /** @return coordenada vertical del borde superior del límite inferior */
    public double groundY() { return fieldHitbox[1].getY(); }

    /** Resuelve los rebotes de un balón contra límites laterales, techo y arcos. */
    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        collisions.bounceBallAtWallsAndGoalFrames(ball, retention);
    }

    /** Recoloca el balón sobre el suelo y delega en él la respuesta del rebote. */
    public void bounceBallAtGround(Ball ball, double retention, double minimumSpeed) {
        collisions.bounceBallAtGround(ball, retention, minimumSpeed);
    }

    /**
     * Devuelve {@code "left"} o {@code "right"} si el centro del balón está
     * dentro o sobre el borde del arco correspondiente; si no, devuelve cadena vacía.
     * @param ball balón cuya posición se compara
     * @return identificador del arco que contiene el centro o cadena vacía
     */
    public String goalSideFor(Ball ball) {
        return collisions.goalSideFor(ball);
    }

    /**
     * Compara ambas coordenadas del centro con los intervalos inclusivos del
     * rectángulo del arco; no usa el radio del balón.
     * @param goal hitbox rectangular del arco
     * @param ball balón cuya posición se consulta
     * @return {@code true} si el centro está dentro o sobre el borde
     */
        /** Comprueba si cualquier parte circular del balón excede el rectángulo exterior. */
    public boolean isOutside(Ball ball) {
        return collisions.isOutside(ball);
    }

    /** Comprueba si el rectángulo del participante excede el rectángulo exterior. */
    public boolean isOutside(SessionParticipant participant) {
        return collisions.isOutside(participant);
    }

    /** Comprueba contacto del participante con los límites superior o laterales. */
    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        return collisions.touchesForbiddenBoundary(participant);
    }

    /** Comprueba si la hitbox circular del balón toca el límite inferior. */
    public boolean touchesGround(Ball ball) {
        return collisions.touchesGround(ball);
    }

    /** Calcula la altura del centro del balón cuando su borde inferior toca el suelo. */
    public double groundCenterY(Ball ball) {
        return collisions.groundCenterY(ball);
    }

    /**
     * Ajusta hitboxes al tamaño provisional de cancha y coloca al primer
     * participante a un cuarto del ancho y al segundo a tres cuartos, ambos
     * apoyados sobre el suelo.
     *
     * @param left participante ubicado en el lado izquierdo
     * @param right participante ubicado en el lado derecho
     */
    public void positionParticipants(SessionParticipant left, SessionParticipant right) {
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
     * Reposiciona límites y arcos según el origen dado; al mover la cancha,
     * traslada los balones por el mismo desplazamiento para conservar su posición
     * relativa. Las dimensiones y la distribución geométrica permanecen fijas.
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
        double goalY = y + height - boundaryThickness - goalHeight;
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
