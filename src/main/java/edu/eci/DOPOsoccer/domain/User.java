package edu.eci.DOPOSoccer.domain;

import edu.eci.DOPOSoccer.domain.configuration.Configuration;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class User {

    private String name;
    private Configuration configuration;

    /**
     * Creates a user.
     */
    public User() {
    }

    /**
     * Returns the user name.
     */
    public String getName() {
        return null;
    }

    /**
     * Returns the user configuration.
     */
    public Configuration getConfiguration() {
        return null;
    }

    /**
     * Changes the user name.
     */
    public void setName(String name) {
    }

    /**
     * Changes the user configuration or key bindings.
     */
    public void changeConfiguration(
            boolean configurationOrKeyBindings,
            String change) {
    }

    /**
     * Restores the user default configuration.
     */
    public void resetConfiguration() {
    }
}
