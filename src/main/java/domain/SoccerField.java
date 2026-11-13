package domain;

import java.util.ArrayList;
import java.util.List;
import domain.hitbox.CircularHitbox;
import domain.hitbox.Hitbox;
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
    private List<Ball> balls;

    /**
     * Crea una cancha provisional de 450 por 150 unidades y un balón de radio 2.
     */
    public SoccerField() {
        balls = new ArrayList<>();
        balls.add(new Ball(450, 150, 2));
        reposition(0, 0);
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
     * Devuelve las hitboxes circulares de los balones.
     *
     * @return hitboxes de todos los balones
     */
    public CircularHitbox[] getHitboxBalls() {
        return balls.stream()
                .map(Ball::getHitbox)
                .toArray(CircularHitbox[]::new);
    }

    /**
     * Comprueba la intersección con otra hitbox.
     *
     * @param hitbox hitbox con la que se comprobará la intersección
     * @return {@code false} hasta que se definan las reglas de colisión
     */
    @Override
    public boolean intersects(Hitbox hitbox) {
        return false;
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

        if (LeftGoalHitbox == null) {
            LeftGoalHitbox = new RectangularHitbox(x, goalY, goalWidth, goalHeight);
        }
        else {
            LeftGoalHitbox.reposition(x, goalY);
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
