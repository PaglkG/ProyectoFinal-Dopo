package presentation.testing;

import domain.Game;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Inicia el banco independiente en modo manual o contra un perfil de bot.
 * {@code selectMode()} recibe los argumentos o la selección del diálogo y
 * toma el catálogo de perfiles de {@code Game}. {@code main()} entrega el modo
 * al panel, inicia su temporizador y registra el cierre y la pérdida de foco.
 */
public final class MechanicsTestApp {
    private static final String MANUAL_MODE = "Dos jugadores";

    /** Evita instanciar el punto de entrada de la aplicación. */
    private MechanicsTestApp() { }

    /** Muestra el banco de pruebas en el hilo de eventos de Swing.
     * @param args sin argumentos abre el selector; acepta {@code --manual} o el nombre simple de un bot
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String mode = selectMode(args);
            if (mode == null) return;

            boolean manual = MANUAL_MODE.equals(mode);
            String matchLabel = manual ? MANUAL_MODE : "Contra bot: " + mode;
            JFrame frame = new JFrame("POOBSoccer · Banco de pruebas visual · " + matchLabel);
            MechanicsTestPanel panel = new MechanicsTestPanel(manual ? null : mode);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            WindowAdapter windowEvents = new WindowAdapter() {
                /** Finaliza la actualización del banco al cerrar la ventana.
                 * @param event evento de cierre de la ventana
                 */
                @Override
                public void windowClosed(WindowEvent event) {
                    panel.stop();
                }

                /** Libera teclas cuya suelta puede ocurrir fuera de la ventana.
                 * @param event evento de pérdida de foco de la ventana
                 */
                @Override
                public void windowLostFocus(WindowEvent event) {
                    panel.releaseControls();
                }
            };
            frame.addWindowListener(windowEvents);
            frame.addWindowFocusListener(windowEvents);
            frame.setContentPane(panel);
            frame.setMinimumSize(new Dimension(950, 640));
            frame.setSize(1280, 820);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.start();
        });
    }

    /** Resuelve el modo antes de que se cree el singleton de la partida.
     * @param args argumentos opcionales recibidos por el punto de entrada
     * @return modo manual, nombre de perfil o {@code null} si se cancela o es desconocido
     */
    private static String selectMode(String[] args) {
        List<String> botProfiles = new Game().getBotTypes();
        if (args.length > 0) {
            if (args.length == 1 && "--manual".equals(args[0])) return MANUAL_MODE;
            if (args.length == 1 && botProfiles.contains(args[0])) return args[0];
            JOptionPane.showMessageDialog(null,
                    "Selecciona --manual o un perfil: " + String.join(", ", botProfiles),
                    "Modo de prueba desconocido", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        List<String> modes = new ArrayList<>();
        modes.add(MANUAL_MODE);
        modes.addAll(botProfiles);
        return (String) JOptionPane.showInputDialog(null,
                "Elige el modo de la sesión a probar. Para cambiarlo, vuelve a abrir el banco.",
                "Modo del banco visual", JOptionPane.QUESTION_MESSAGE,
                null, modes.toArray(String[]::new), MANUAL_MODE);
    }
}
