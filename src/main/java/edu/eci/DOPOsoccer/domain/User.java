package edu.eci.DOPOSoccer.domain;

import edu.eci.DOPOSoccer.domain.configuration.Configuration;

/**
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class User {

    private String name;
    private Configuration configuration;

    /** * Crea un usuario. */
    public User() {
    }

    /** * Devuelve el nombre del usuario. */
    public String getName() {
        return null;
    }

    /** * Devuelve la configuración del usuario. */
    public Configuration getConfiguration() {
        return null;
    }

    /** * Cambia el nombre del usuario. */
    public void setName(String name) {
    }

    /** * Cambia la configuración del usuario o sus controles. */
    public void changeConfiguration(
            boolean configurationOrKeyBindings,
            String change) {
    }

    /** * Restablece la configuración predeterminada del usuario. */
    public void resetConfiguration() {
    }
}
