package edu.eci.DOPOSoccer.presentation.screens;

import javax.swing.JPanel;
import edu.eci.DOPOSoccer.presentation.navigation.Screen;

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

    /** Prepara el panel de carga cuando se muestra. */
    @Override
    public void onShow() {
    }

    /** Ejecuta las tareas de ocultamiento del panel de carga. */
    @Override
    public void onHide() {
    }

    /** Libera los recursos asociados al panel de carga. */
    @Override
    public void dispose() {
    }
}
