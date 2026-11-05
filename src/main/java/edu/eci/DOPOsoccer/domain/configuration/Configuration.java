package edu.eci.DOPOSoccer.domain.configuration;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class Configuration {

    private boolean sound;
    private boolean music;
    private KeyBindings keyBindings;

    /**
     * Creates a configuration with its default settings.
     */
    public Configuration() {
    }

    /**
     * Changes the sound setting.
     */
    public void setSound() {
    }

    /**
     * Restores the default configuration values.
     */
    public void reboot() {
    }

    /**
     * Changes the music setting.
     */
    public void setMusic() {
    }

    /**
     * Changes a configuration option or key bindings.
     */
    public void changeConfiguration(
            boolean configurationOrKeyBindings,
            String change) {
    }

    /**
     * Processes the requested configuration change.
     */
    private void change(String change) {
    }

    /**
     * Sets the music setting to the specified status.
     */
    private void setMusic(boolean status) {
    }

    /**
     * Sets the sound setting to the specified status.
     */
    private void setSound(boolean status) {
    }
}
