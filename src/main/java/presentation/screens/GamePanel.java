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

    /** Prepara la vista de juego cuando se muestra. */
    @Override
    public void onShow() {
    }

    /** Ejecuta las tareas de ocultamiento de la vista de juego. */
    @Override
    public void onHide() {
    }

    /** Libera los recursos asociados a la vista de juego. */
    @Override
    public void dispose() {
    }
}
