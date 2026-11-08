package edu.eci.DOPOSoccer;

import javax.swing.SwingUtilities;

import edu.eci.DOPOSoccer.presentation.GameGUI;

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
