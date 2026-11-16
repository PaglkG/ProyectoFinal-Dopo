package presentation.screens;

import javax.swing.JPanel;
import presentation.navigation.Screen;

/**
 * Pantalla de juego. Su responsabilidad será mostrar la cancha y el estado de
 * la partida, además de comunicar las entradas del usuario al controlador.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GamePanel extends JPanel implements Screen {

    /** Crea el panel de juego. */
    public GamePanel() {
    }

    /** Punto de extensión para preparar la vista al mostrarla; actualmente el cuerpo está vacío. */
    @Override
    public void onShow() {
    }

    /** Punto de extensión para ocultar la vista; actualmente el cuerpo está vacío. */
    @Override
    public void onHide() {
    }

    /** Punto de extensión para liberar recursos; actualmente el cuerpo está vacío. */
    @Override
    public void dispose() {
    }
}
