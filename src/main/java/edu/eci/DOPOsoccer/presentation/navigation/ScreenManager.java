package edu.eci.DOPOSoccer.presentation.navigation;

import java.util.Map;

/**
 * Centraliza el registro y la navegación entre las pantallas de presentación,
 * evitando que cada pantalla administre directamente a las demás.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ScreenManager {

    private Map<String, Screen> screens;
    private String currentScreen;

    /** Registra una pantalla con el identificador indicado. */
    public void registerScreen(String screenId, Screen screen) {
    }

    /** Solicita la navegación hacia la pantalla identificada. */
    public void showScreen(String screenId) {
    }

    /** Solicita ocultar la pantalla actualmente visible. */
    public void hideCurrentScreen() {
    }

    /** Libera los recursos de las pantallas registradas. */
    public void disposeScreens() {
    }
}
