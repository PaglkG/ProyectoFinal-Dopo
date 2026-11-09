package edu.eci.DOPOSoccer.presentation;

import javax.swing.JFrame;
import edu.eci.DOPOSoccer.presentation.navigation.ScreenManager;

/**
 * Ventana principal de la aplicación. Su responsabilidad es alojar el administrador
 * de pantallas y representar el marco general de la interfaz.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class GameGUI extends JFrame {

    private ScreenManager screenManager;

    /** Crea la ventana principal sin configurar todavía sus componentes. */
    public GameGUI() {
    }
}
