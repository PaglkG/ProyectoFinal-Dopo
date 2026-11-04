package edu.eci.DOPOsoccer;

import javax.swing.SwingUtilities;
import edu.eci.DOPOsoccer.presentation.GameGUI;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class App {

    /**
     * Starts the POOBSoccer graphical application.
     */
    // Inicia la aplicación gráfica de POOBSoccer.
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            GameGUI gameGUI = new GameGUI();

            gameGUI.setVisible(true);

        });
    }
}
