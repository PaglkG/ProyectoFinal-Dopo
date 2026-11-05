package edu.eci.DOPOSoccer;

import javax.swing.SwingUtilities;

import edu.eci.DOPOSoccer.presentation.GameGUI;

/**
 * @author Pablo Andres Gualdron Lindo
 */
public class App {

    /**
     * Starts the POOBSoccer graphical application.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            GameGUI gameGUI = new GameGUI();

            gameGUI.setVisible(true);

        });
    }
}
