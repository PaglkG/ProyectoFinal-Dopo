package edu.eci.DOPOsoccer.domain;

import edu.eci.DOPOsoccer.domain.configuration.Configuration;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class User {

    private String name;
    private Configuration configuration;

    /**
     * Creates a user.
     */
    // Inicializa un usuario.
    public User() {
    }

    /**
     * Returns the user name.
     */
    // Devuelve el nombre del usuario.
    public String getName() {
        return null;
    }

    /**
     * Returns the user configuration.
     */
    // Devuelve la configuración del usuario.
    public Configuration getConfiguration() {
        return null;
    }

    /**
     * Changes the user name.
     */
    // Cambia el nombre del usuario.
    public void setName(String name) {
    }

    /**
     * Changes the user configuration or key bindings.
     */
    // Modifica la configuración o los controles del usuario.
    public void changeConfiguration(
            boolean configurationOrKeyBindings,
            String change) {
    }

    /**
     * Restores the user default configuration.
     */
    // Restablece la configuración predeterminada del usuario.
    public void resetConfiguration() {
    }
}
