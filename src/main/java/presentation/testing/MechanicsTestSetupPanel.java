package presentation.testing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/** Pantalla inicial para elegir las clases y parámetros que se probarán. */
public final class MechanicsTestSetupPanel extends JPanel {
    private final Consumer<MechanicsTestConfiguration> onStart;
    private final JComboBox<MechanicsTestController.SessionMode> sessionMode =
            new JComboBox<>(MechanicsTestController.SessionMode.values());
    private final JComboBox<MechanicsTestController.MatchMode> matchMode =
            new JComboBox<>(MechanicsTestController.MatchMode.values());
    private final JTextField sessionClass = new JTextField("domain.PlayerVsPlayer");
    private final JTextField matchClass = new JTextField("domain.ByGoals");
    private final JTextField ruleParameters = new JTextField("150,450,5");
    private final JTextField limit = new JTextField("5");
    private final JTextField rightPower = new JTextField("domain.BlueAura");
    private final JTextField leftPower = new JTextField("domain.Lightning");
    private final JTextField botProfile = new JTextField("Clumsy");
    private final JCheckBox playerOne = new JCheckBox("Seleccionar jugador 1", true);

    /** Construye el formulario inicial y registra el destino de la configuración.
     * @param onStart consumidor que recibe las opciones al iniciar el banco
     */
    public MechanicsTestSetupPanel(Consumer<MechanicsTestConfiguration> onStart) {
        super(new BorderLayout(12, 12));
        this.onStart = onStart;
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Modo de sesión")); fields.add(sessionMode);
        fields.add(new JLabel("Modalidad")); fields.add(matchMode);
        addField(fields, "Clase de sesión (nombre completo)", sessionClass);
        addField(fields, "Clase de regla (nombre completo)", matchClass);
        addField(fields, "Altura, anchura y parámetro de modalidad", ruleParameters);
        addField(fields, "Límite (goles o minutos)", limit);
        addField(fields, "Poder lado derecho (clase completa)", rightPower);
        addField(fields, "Poder lado izquierdo / bot", leftPower);
        addField(fields, "Perfil del bot", botProfile);
        fields.add(new JLabel("Identidad humana (si aplica)")); fields.add(playerOne);
        JButton start = new JButton("Iniciar banco de pruebas");
        start.addActionListener(event -> start());
        add(new JLabel("Configuración extensible del banco de pruebas", JLabel.CENTER), BorderLayout.NORTH);
        add(fields, BorderLayout.CENTER);
        add(start, BorderLayout.SOUTH);
    }

    /** Añade una fila con etiqueta y editor. */
    private void addField(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    /** Convierte el formulario en una configuración y la entrega a la aplicación. */
    private void start() {
        try {
            int configuredLimit = Integer.parseInt(limit.getText().trim());
            List<String> parameters = Arrays.stream(ruleParameters.getText().split(","))
                    .map(String::trim).filter(value -> !value.isEmpty()).toList();
            MechanicsTestConfiguration configuration = new MechanicsTestConfiguration(
                    (MechanicsTestController.SessionMode) sessionMode.getSelectedItem(),
                    (MechanicsTestController.MatchMode) matchMode.getSelectedItem(), configuredLimit,
                    sessionClass.getText().trim(), matchClass.getText().trim(), parameters,
                    rightPower.getText().trim(), leftPower.getText().trim(), botProfile.getText().trim(),
                    playerOne.isSelected());
            onStart.accept(configuration);
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Configuración inválida",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
