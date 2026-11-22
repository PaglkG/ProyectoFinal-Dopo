package presentation.testing;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Punto de entrada independiente de la aplicación principal para probar mecánicas. */
public final class MechanicsTestApp {

    /** Impide instanciar el punto de entrada, que solo contiene el método estático {@code main}. */
    private MechanicsTestApp() { }

    /** Crea y muestra la ventana del banco manual en el hilo de eventos de Swing.
     * @param args argumentos de línea de comandos; no se utilizan
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame setup = new JFrame("POOBSoccer · Configuración del banco de pruebas");
            setup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setup.setContentPane(new MechanicsTestSetupPanel(configuration -> {
                try {
                    showMechanicsWindow(configuration);
                    setup.dispose();
                } catch (RuntimeException error) {
                    JOptionPane.showMessageDialog(null, error.getMessage(), "No se pudo crear la sesión",
                            JOptionPane.ERROR_MESSAGE);
                }
            }));
            setup.setMinimumSize(new java.awt.Dimension(780, 500));
            setup.pack();
            setup.setLocationRelativeTo(null);
            setup.setVisible(true);
        });
    }

    /** Abre la ventana de simulación con la configuración seleccionada. */
    private static void showMechanicsWindow(MechanicsTestConfiguration configuration) {
        MechanicsTestPanel panel = new MechanicsTestPanel(configuration);
        JFrame window = new JFrame("POOBSoccer · Banco de pruebas de mecánicas");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
    }
}
