package presentation.testing;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Punto de entrada independiente de la aplicación principal para probar mecánicas. */
public final class MechanicsTestApp {

    /** Impide instanciar el punto de entrada, que solo contiene el método estático {@code main}. */
    private MechanicsTestApp() { }

    /** Crea y muestra la ventana del banco manual en el hilo de eventos de Swing. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("POOBSoccer · Banco de pruebas de mecánicas");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            MechanicsTestPanel panel = new MechanicsTestPanel();
            window.setContentPane(panel);
            window.addWindowListener(new WindowAdapter() {
                /** Detiene la simulación cuando se cierra la ventana. */
                @Override
                public void windowClosed(WindowEvent event) {
                    panel.stopSimulation();
                }
            });
            window.setMinimumSize(new java.awt.Dimension(1000, 650));
            window.setSize(1280, 820);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
