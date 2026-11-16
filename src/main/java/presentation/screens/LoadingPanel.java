package presentation.screens;

import javax.swing.JPanel;
import presentation.navigation.Screen;

/**
 * Pantalla temporal para informar que la aplicación está preparando una
 * operación, como cargar recursos o iniciar una partida.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class LoadingPanel extends JPanel implements Screen {

    /** Crea el panel de carga. */
    public LoadingPanel() {
    }

    /** Punto de extensión para preparar el panel al mostrarlo; actualmente el cuerpo está vacío. */
    @Override
    public void onShow() {
    }

    /** Punto de extensión para ocultar el panel; actualmente el cuerpo está vacío. */
    @Override
    public void onHide() {
    }

    /** Punto de extensión para liberar recursos; actualmente el cuerpo está vacío. */
    @Override
    public void dispose() {
    }
}
