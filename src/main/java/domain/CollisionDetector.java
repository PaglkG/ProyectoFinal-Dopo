package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordina la detección entre entidades mediante el patrón Strategy.
 * El detector extrae las formas, busca la primera estrategia compatible y
 * encapsula su resultado; no contiene fórmulas geométricas ni aplica respuestas
 * físicas.
 */
public final class CollisionDetector {

    /** Estrategias consultadas en orden para resolver cada par de formas. */
    private final List<CollisionDetectionStrategy> strategies = new ArrayList<>();

    /** Crea el detector con estrategias para las tres combinaciones de formas. */
    public CollisionDetector() {
        registerStrategy(new CircleCircleStrategy());
        registerStrategy(new RectangleRectangleStrategy());
        registerStrategy(new CircleRectangleStrategy());
    }

    /**
     * Agrega una estrategia al final de la lista de selección.
     * Las estrategias se consultan en orden; las predeterminadas se registran
     * durante la construcción del detector.
     *
     * @param strategy estrategia geométrica que se agregará
     */
    public void registerStrategy(CollisionDetectionStrategy strategy) {
        strategies.add(strategy);
    }

    /**
     * Detecta contacto entre las formas expuestas por las entidades.
     * Si una entidad o su forma es nula, o no hay una estrategia compatible,
     * devuelve un resultado sin contacto y conserva las entidades recibidas.
     *
     * @param first primera entidad
     * @param second segunda entidad
     * @return entidades evaluadas y resultado de contacto
     */
    public CollisionContact detect(Collidable first, Collidable second) {
        if (first == null || second == null) {
            return new CollisionContact(first, second, false);
        }
        CollisionShape firstShape = first.getCollisionShape();
        CollisionShape secondShape = second.getCollisionShape();
        if (firstShape == null || secondShape == null) {
            return new CollisionContact(first, second, false);
        }
        for (CollisionDetectionStrategy strategy : strategies) {
            if (strategy.supports(firstShape, secondShape)) {
                return new CollisionContact(first, second, strategy.detect(firstShape, secondShape));
            }
        }
        return new CollisionContact(first, second, false);
    }
}
