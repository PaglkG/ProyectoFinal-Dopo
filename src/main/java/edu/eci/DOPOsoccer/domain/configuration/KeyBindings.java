package edu.eci.DOPOSoccer.domain.configuration;

/**
 * Asigna teclas a las acciones disponibles del jugador.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class KeyBindings {

    private String right;
    private String left;
    private String jump;
    private String power;

    /** Crea las asignaciones predeterminadas. */
    public KeyBindings() {
    }

    /**
     * Cambia una asignación.
     * @param change acción que se reasignará
     */
    public void changeConfiguration(String change) {
    }

    /** Restablece las asignaciones predeterminadas. */
    public void reboot() {
    }

    /**
     * Asigna la tecla de movimiento a la derecha.
     * @param key tecla asignada
     */
    private void setRight(String key) {
    }

    /**
     * Asigna la tecla de movimiento a la izquierda.
     * @param key tecla asignada
     */
    private void setLeft(String key) {
    }

    /**
     * Asigna la tecla de salto.
     * @param key tecla asignada
     */
    private void setJump(String key) {
    }

    /**
     * Asigna la tecla de poder.
     * @param key tecla asignada
     */
    private void setPower(String key) {
    }

    /**
     * Cambia la tecla de una acción.
     * @param change acción
     * @param key nueva tecla
     */
    private void change(String change, String key) {
    }

    /**
     * Devuelve la tecla de movimiento a la derecha.
     * @return tecla asignada
     */
    public String getRight() {
        return null;
    }

    /**
     * Devuelve la tecla de movimiento a la izquierda.
     * @return tecla asignada
     */
    public String getLeft() {
        return null;
    }

    /**
     * Devuelve la tecla de salto.
     * @return tecla asignada
     */
    public String getJump() {
        return null;
    }

    /**
     * Devuelve la tecla de poder.
     * @return tecla asignada
     */
    public String getPower() {
        return null;
    }

    /**
     * Comprueba si la tecla está libre.
     * @param key código de tecla
     * @return indica si se puede asignar
     */
    private boolean isKeyFree(int key) {
        return false;
    }
}
