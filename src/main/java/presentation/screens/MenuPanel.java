package presentation.screens;

import javax.swing.JPanel;
import presentation.navigation.Screen;

/**
 * Pantalla del menú principal. Presentará las opciones para iniciar una partida
 * o salir de la aplicación.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class MenuPanel extends JPanel implements Screen {

    /** Crea el panel del menú principal. */
    public MenuPanel() {
    }

    /** Punto de extensión para preparar el menú al mostrarlo; actualmente el cuerpo está vacío. */
    @Override
    public void onShow() {
    }

    /** Punto de extensión para ocultar el menú; actualmente el cuerpo está vacío. */
    @Override
    public void onHide() {
    }

    /** Punto de extensión para liberar recursos; actualmente el cuerpo está vacío. */
    @Override
    public void dispose() {
    }
}
