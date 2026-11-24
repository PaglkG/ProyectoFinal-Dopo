package domain;

/**
 * Contrato Strategy para una operación de detección entre formas geométricas.
 * Cada implementación declara qué pares admite y evalúa solo su geometría;
 * no recibe entidades de juego ni cambia posiciones o velocidades.
 */
public interface CollisionDetectionStrategy {

    /**
     * Indica si esta estrategia admite el par de formas indicado en ese orden.
     * Una estrategia que admite formas simétricas puede aceptar ambos órdenes.
     *
     * @param first primera forma
     * @param second segunda forma
     * @return {@code true} si ambas formas son compatibles
     */
    boolean supports(CollisionShape first, CollisionShape second);

    /**
     * Determina si las formas compatibles se tocan o se solapan.
     * El detector llama este método después de que {@link #supports} devuelve
     * {@code true}; la implementación puede entonces tratarlas como sus tipos
     * geométricos concretos.
     *
     * @param first primera forma
     * @param second segunda forma
     * @return {@code true} si hay contacto
     */
    boolean detect(CollisionShape first, CollisionShape second);
}
