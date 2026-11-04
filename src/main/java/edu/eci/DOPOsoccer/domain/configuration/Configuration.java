package edu.eci.DOPOsoccer.domain.configuration;

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
    // Inicializa la configuración con sus valores predeterminados.
    public Configuration() {
    }

    /**
     * Changes the sound setting.
     */
    // Cambia el estado del sonido.
    public void setSound() {
    }

    /**
     * Restores the default configuration values.
     */
    // Restablece los valores predeterminados de la configuración.
    public void reboot() {
    }

    /**
     * Changes the music setting.
     */
    // Cambia el estado de la música.
    public void setMusic() {
    }

    /**
     * Changes a configuration option or key bindings.
     */
    // Modifica una opción de configuración o los controles.
    public void changeConfiguration(
            boolean configurationOrKeyBindings,
            String change) {
    }

    /**
     * Processes the requested configuration change.
     */
    // Procesa el cambio de configuración solicitado.
    private void change(String change) {
    }

    /**
     * Sets the music setting to the specified status.
     */
    // Establece la música en el estado indicado.
    private void setMusic(boolean status) {
    }

    /**
     * Sets the sound setting to the specified status.
     */
    // Establece el sonido en el estado indicado.
    private void setSound(boolean status) {
    }
}
