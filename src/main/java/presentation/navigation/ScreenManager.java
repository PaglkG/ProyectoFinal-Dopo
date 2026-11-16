package presentation.navigation;

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

    /** Punto de extensión para registrar una pantalla; actualmente el cuerpo está vacío. */
    public void registerScreen(String screenId, Screen screen) {
    }

    /** Punto de extensión para mostrar una pantalla; actualmente el cuerpo está vacío. */
    public void showScreen(String screenId) {
    }

    /** Punto de extensión para ocultar la pantalla visible; actualmente el cuerpo está vacío. */
    public void hideCurrentScreen() {
    }

    /** Punto de extensión para liberar pantallas; actualmente el cuerpo está vacío. */
    public void disposeScreens() {
    }
}
