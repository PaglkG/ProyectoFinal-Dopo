package presentation;

import javax.swing.SwingUtilities;


/**
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class App {

    /**
     * Inicia la aplicación gráfica de POOBSoccer.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            GameGUI gameGUI = new GameGUI();

            gameGUI.setVisible(true);

        });
    }
}
