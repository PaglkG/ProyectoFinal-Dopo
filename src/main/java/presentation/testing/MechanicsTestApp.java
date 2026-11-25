package presentation.testing;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Inicia la aplicación independiente para observar manualmente el juego y sus colisiones. */
public final class MechanicsTestApp {

    /** Evita instanciar el punto de entrada de la aplicación. */
    private MechanicsTestApp() { }

    /** Muestra el banco de pruebas en el hilo de eventos de Swing.
     * @param args argumentos de consola, no utilizados
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("POOBSoccer · Banco de pruebas visual");
            MechanicsTestPanel panel = new MechanicsTestPanel();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(panel);
            frame.setMinimumSize(new Dimension(950, 640));
            frame.setSize(1280, 820);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.start();
        });
    }
}
