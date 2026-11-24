package domain;

/**
 * Forma geométrica con una posición de referencia que puede consultarse y
 * actualizarse sin alterar sus dimensiones. El punto de referencia depende de
 * la forma concreta: centro para círculos y esquina superior izquierda para
 * rectángulos.
 */
public interface CollisionShape {

    /**
     * Devuelve la coordenada horizontal del punto de referencia.
     *
     * @return coordenada horizontal
     */
    double getX();

    /**
     * Devuelve la coordenada vertical del punto de referencia.
     *
     * @return coordenada vertical
     */
    double getY();

    /**
     * Actualiza la posición de referencia de la forma.
     *
     * @param x nueva coordenada horizontal
     * @param y nueva coordenada vertical
     */
    void reposition(double x, double y);
}
