package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import domain.Hitbox;
import domain.HitboxContact;
import domain.CircularHitbox;
import domain.RectangularHitbox;
import domain.GameException;


/**
 * Representa la cancha, sus hitboxes y los balones que contiene.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class SoccerField implements Hitbox, Serializable {

    private static final long serialVersionUID = 1L;

    private final double courtHeight;
    private final double courtWidth;

    private RectangularHitbox LeftGoalHitbox;
    private RectangularHitbox RightGoalHitbox;
    private RectangularHitbox fieldHitbox;
    private Ball[] balls;
    private transient CollisionFacade collisions;

    /**
     * Crea una cancha provisional de 450 por 150 unidades y un balón de radio 10.
     */
    public SoccerField() {
        this(150, 450);
    }

    /** Crea una cancha con las dimensiones indicadas.
     * @param height alto de la cancha
     * @param width ancho de la cancha
     * @throws GameException si una dimensión no es positiva o no es finita
     */
    public SoccerField(double height, double width) {
        if (!Double.isFinite(height) || !Double.isFinite(width) || height <= 0 || width <= 0) {
            throw new GameException(GameException.INVALID_RULE_PARAMETERS);
        }
        this.courtHeight = height;
        this.courtWidth = width;
        balls = new Ball[] {new Ball(width, height, 10)};
        reposition(0, 0);
        collisions = new CollisionFacade(this);
    }

    /** Reconstruye la fachada de colisiones que no se serializa con la cancha.
     * @param input flujo desde el que se restauran los campos serializados
     * @throws IOException si falla la lectura del objeto
     * @throws ClassNotFoundException si falta una clase almacenada
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        collisions = new CollisionFacade(this);
    }

    /** Avanza todos los balones contenidos usando el paso y gravedad indicados.
     * @param deltaSeconds duración del paso en segundos
     * @param gravity aceleración vertical en unidades por segundo cuadrado
     */
    public void advanceBalls(double deltaSeconds, double gravity) {
        for (Ball ball : balls) {
            ball.advance(deltaSeconds, gravity);
        }
    }

    /** Agrega a la cancha un balón nuevo, conservando sus coordenadas y estado iniciales.
     * @param ball balón que se agregará
     * @throws GameException si el balón es nulo o ya está agregado
     */
    public void addBall(Ball ball) {
        if (ball == null || Arrays.stream(balls).anyMatch(existing -> existing == ball)) {
            throw new GameException(GameException.INVALID_BALL);
        }
        Ball[] expanded = Arrays.copyOf(balls, balls.length + 1);
        expanded[balls.length] = ball;
        balls = expanded;
    }

    /** Resuelve las colisiones entre todos los pares de balones con masas iguales.
     * @param restitution coeficiente de restitución normal
     */
    public void resolveBallCollisions(double restitution) {
        collisions.resolveBallCollisions(restitution);
    }

    /** Resuelve la penetración y el rebote de cada balón contra participantes.
     * @param participants participantes que pueden tocar los balones
     * @param restitution coeficiente de restitución normal
     * @param minimumLandingSpeed velocidad mínima que se aplica al caer sobre un balón
     */
    public void resolveBallParticipantCollisions(Iterable<SessionParticipant> participants,
            double restitution, double minimumLandingSpeed) {
        collisions.resolveBallParticipantCollisions(participants, restitution, minimumLandingSpeed);
    }

    /** Resuelve contactos creados al caminar empujando balones en su dirección de avance.
     * @param moved participante que caminó
     * @param previousX coordenada horizontal anterior del participante
     * @param participants participantes que bloquean posiciones inválidas del balón
     */
    public void resolveWalkingContact(SessionParticipant moved, double previousX,
            Iterable<SessionParticipant> participants) {
        collisions.resolveWalkingContact(moved, previousX, participants);
    }

    /** Corrige la salida del balón y aplica rebote hacia el interior en cada borde de cancha.
     * @param ball balón que se corregirá
     * @param retention fracción de velocidad conservada tras el rebote
     */
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
        Hitbox[] hitbox = new Hitbox[4 + 2 + balls.length];
        System.arraycopy(getHitboxField(), 0, hitbox, 0, 4);
        hitbox[4] = LeftGoalHitbox;
        hitbox[5] = RightGoalHitbox;
        for (int i = 0; i < balls.length; i++) {
            hitbox[6 + i] = balls[i];
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
    public RectangularHitbox getFieldHitbox() {
        return fieldHitbox;
    }

    /** Devuelve los límites derivados de los cuatro bordes del rectángulo exterior.
     * @return límites en orden superior, inferior, izquierdo y derecho
     */
    public RectangularHitbox[] getHitboxField() {
        double x = fieldHitbox.getX();
        double y = fieldHitbox.getY();
        double width = fieldHitbox.getWidth();
        double height = fieldHitbox.getHeight();
        double thickness = boundaryThickness();
        return new RectangularHitbox[] {
            new RectangularHitbox(x, y, width, thickness),
            new RectangularHitbox(x, y + height - thickness, width, thickness),
            new RectangularHitbox(x, y, thickness, height),
            new RectangularHitbox(x + width - thickness, y, thickness, height)
        };
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

    /** Devuelve una copia del arreglo que contiene los balones de cancha.
     * @return copia del arreglo; los objetos {@link Ball} no se duplican
     */
    public Ball[] getBalls() {
        return balls.clone();
    }

    /** Devuelve las hitboxes de los arcos.
     * @return arcos en orden izquierdo y derecho
     */
    public RectangularHitbox[] getGoalHitboxes() {
        return new RectangularHitbox[] {LeftGoalHitbox, RightGoalHitbox};
    }

    /** Devuelve una copia del arreglo de balones para cálculos del dominio.
     * @return copia del arreglo que conserva las mismas instancias de balón
     */
    public Ball[] balls() {
        return balls.clone();
    }

    /** Devuelve el borde izquierdo del rectángulo exterior.
     * @return coordenada horizontal
     */
    public double courtX() { return fieldHitbox.getX(); }
    /** Devuelve el borde superior del rectángulo exterior.
     * @return coordenada vertical
     */
    public double courtY() { return fieldHitbox.getY(); }
    /** Devuelve el ancho del rectángulo exterior.
     * @return ancho de cancha
     */
    public double courtWidth() { return fieldHitbox.getWidth(); }
    /** Devuelve el alto del rectángulo exterior.
     * @return alto de cancha
     */
    public double courtHeight() { return fieldHitbox.getHeight(); }
    /** Calcula el grosor de los límites de cancha.
     * @return grosor igual a uno por ciento de la altura exterior
     */
    public double boundaryThickness() { return courtHeight() / 100; }
    /** Devuelve la coordenada del borde superior del límite inferior.
     * @return coordenada del suelo
     */
    public double groundY() { return courtY() + courtHeight() - boundaryThickness(); }

    /** Resuelve los rebotes de un balón contra límites laterales, techo y arcos.
     * @param ball balón cuyo contacto se resolverá
     * @param retention fracción de velocidad normal conservada tras el rebote
     */
    public void bounceBallAtWallsAndGoalFrames(Ball ball, double retention) {
        collisions.bounceBallAtWallsAndGoalFrames(ball, retention);
    }

    /** Recoloca el balón sobre el suelo y delega en él la respuesta del rebote.
     * @param ball balón cuyo contacto se resolverá
     * @param retention fracción de velocidad vertical conservada tras el rebote
     * @param minimumSpeed velocidad mínima para conservar el rebote
     */
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
    /** Comprueba si cualquier parte circular del balón excede el rectángulo exterior.
     * @param ball balón que se comprobará
     * @return {@code true} si el balón excede alguno de los bordes exteriores
     */
    public boolean isOutside(Ball ball) {
        return collisions.isOutside(ball);
    }

    /** Comprueba si el rectángulo del participante excede el rectángulo exterior.
     * @param participant participante que se comprobará
     * @return {@code true} si alguna parte de la hitbox queda fuera
     */
    public boolean isOutside(SessionParticipant participant) {
        return collisions.isOutside(participant);
    }

    /** Comprueba contacto del participante con los límites superior o laterales.
     * @param participant participante que se comprobará
     * @return {@code true} si toca un límite prohibido
     */
    public boolean touchesForbiddenBoundary(SessionParticipant participant) {
        return collisions.touchesForbiddenBoundary(participant);
    }

    /** Comprueba si la hitbox circular del balón toca el límite inferior.
     * @param ball balón que se comprobará
     * @return {@code true} si ambas hitboxes están en contacto
     */
    public boolean touchesGround(Ball ball) {
        return collisions.touchesGround(ball);
    }

    /** Calcula la altura del centro del balón cuando su borde inferior toca el suelo.
     * @param ball balón cuya posición de apoyo se calculará
     * @return coordenada vertical del centro en el contacto
     */
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
        left.rememberCurrentPosition();
        right.rememberCurrentPosition();
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
        for (RectangularHitbox boundary : getHitboxField()) {
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
        for (RectangularHitbox boundary : getHitboxField()) {
            if (boundary.intersects(other).isContact()) {
                return new HitboxContact("SoccerField", fieldHitbox);
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
        double width = courtWidth;
        double height = courtHeight;
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
        if (fieldHitbox == null) fieldHitbox = new RectangularHitbox(x, y, width, height);
        else fieldHitbox.reposition(x, y);
    }
}
