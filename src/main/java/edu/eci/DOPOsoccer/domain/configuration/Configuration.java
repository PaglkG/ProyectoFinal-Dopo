package edu.eci.DOPOSoccer.domain.configuration;

/**
 * Configuración de sonido, música y controles del usuario.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class Configuration {

    private boolean sound;
    private boolean music;
    private KeyBindings keyBindings;

    /** Crea una configuración. */
    public Configuration(int user) {
        sound = true;
        music = true;
        keyBindings = new KeyBindings(user);
    }

    /** Restablece los valores predeterminados. */
    public void reboot(int user) {
        sound = true;
        music = true;
        keyBindings.reboot(user);
    }

    /**
     * Cambia una opción de configuración o los controles.
     * @param configurationOrKeyBindings indica qué tipo de valor cambiar
     * @param change opción solicitada
     */
    public void changeConfiguration(boolean configurationOrKeyBindings, String change) {
    }

    /**
     * Procesa el cambio solicitado.
     * @param change cambio de configuración
     */
    private void change(String change) {
    }

    /**
     * Establece el estado de la música.
     * @param status nuevo estado
     */
    private void setMusic(boolean status) {
    }

    /**
     * Establece el estado del sonido.
     * @param status nuevo estado
     */
    private void setSound(boolean status) {
    }
}
