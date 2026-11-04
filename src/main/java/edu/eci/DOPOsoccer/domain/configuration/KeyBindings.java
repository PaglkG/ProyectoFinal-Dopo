package edu.eci.DOPOsoccer.domain.configuration;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class KeyBindings {

    private String right;
    private String left;
    private String jump;
    private String power;

    /**
     * Creates the default key bindings.
     */
    // Inicializa los controles predeterminados.
    public KeyBindings() {
    }

    /**
     * Changes one of the key bindings.
     */
    // Modifica una de las asignaciones de teclas.
    public void changeConfiguration(String change) {
    }

    /**
     * Restores the default key bindings.
     */
    // Restablece las asignaciones de teclas predeterminadas.
    public void reboot() {
    }

    /**
     * Sets the key for moving right.
     */
    // Establece la tecla para moverse a la derecha.
    private void setRight(String key) {
    }

    /**
     * Sets the key for moving left.
     */
    // Establece la tecla para moverse a la izquierda.
    private void setLeft(String key) {
    }

    /**
     * Sets the key for jumping.
     */
    // Establece la tecla para saltar.
    private void setJump(String key) {
    }

    /**
     * Sets the key for using a power.
     */
    // Establece la tecla para utilizar un poder.
    private void setPower(String key) {
    }

    /**
     * Changes the selected action key.
     */
    // Cambia la tecla asignada a la acción seleccionada.
    private void change(String change, String key) {
    }

    /**
     * Returns the key for moving right.
     */
    // Devuelve la tecla para moverse a la derecha.
    public String getRight() {
        return null;
    }

    /**
     * Returns the key for moving left.
     */
    // Devuelve la tecla para moverse a la izquierda.
    public String getLeft() {
        return null;
    }

    /**
     * Returns the key for jumping.
     */
    // Devuelve la tecla para saltar.
    public String getJump() {
        return null;
    }

    /**
     * Returns the key for using a power.
     */
    // Devuelve la tecla para utilizar un poder.
    public String getPower() {
        return null;
    }

    /**
     * Checks whether the specified key is available.
     */
    // Comprueba si la tecla indicada está disponible.
    private boolean isKeyFree(int key) {
        return false;
    }
}
