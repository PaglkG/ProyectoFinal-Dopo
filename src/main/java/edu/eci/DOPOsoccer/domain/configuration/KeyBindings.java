package edu.eci.DOPOSoccer.domain.configuration;

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
    public KeyBindings() {
    }

    /**
     * Changes one of the key bindings.
     */
    public void changeConfiguration(String change) {
    }

    /**
     * Restores the default key bindings.
     */
    public void reboot() {
    }

    /**
     * Sets the key for moving right.
     */
    private void setRight(String key) {
    }

    /**
     * Sets the key for moving left.
     */
    private void setLeft(String key) {
    }

    /**
     * Sets the key for jumping.
     */
    private void setJump(String key) {
    }

    /**
     * Sets the key for using a power.
     */
    private void setPower(String key) {
    }

    /**
     * Changes the selected action key.
     */
    private void change(String change, String key) {
    }

    /**
     * Returns the key for moving right.
     */
    public String getRight() {
        return null;
    }

    /**
     * Returns the key for moving left.
     */
    public String getLeft() {
        return null;
    }

    /**
     * Returns the key for jumping.
     */
    public String getJump() {
        return null;
    }

    /**
     * Returns the key for using a power.
     */
    public String getPower() {
        return null;
    }

    /**
     * Checks whether the specified key is available.
     */
    private boolean isKeyFree(int key) {
        return false;
    }
}
