package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Menú de pausa de la partida.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> el constructor recibe el controlador
 *       de navegación y las acciones para ver resultados y guardar.</li>
 *   <li><strong>Modificación:</strong> {@code prepareController()} conecta
 *       Continuar, Guardar, Resultados y Menú principal. La ventana cambia de pantalla
 *       y gestiona el temporizador del partido mediante {@code showScreen()}.</li>
 *   <li><strong>Actualización:</strong> {@code prepareView()} construye el menú;
 *       el fondo se repinta cuando Swing muestra o redimensiona el panel.</li>
 * </ul>
 */
public class PausePanel extends BasePanel {
    private static final String COURT_IMAGE = "/images/ui/court@4x-8.png";
    private static final String LOGO_IMAGE = "/images/ui/logo@4x-8.png";
    private static final int LOGO_SIZE = 64;

    /** Controlador que coordina los cambios de pantalla. */
    private final NavigationController navigationController;
    /** Acción que muestra el resultado parcial desde la pausa. */
    private final Runnable resultsPreviewAction;
    /** Acción de la ventana que selecciona el destino y guarda la partida. */
    private final Runnable saveMatchAction;
    /** Botón para guardar la partida en pausa. */
    private JButton saveButton;
    /** Acción que vuelve a la pantalla de partida. */
    private JButton resumeButton;
    /** Acción que solicita la vista previa del resultado. */
    private JButton resultsButton;
    /** Acción que regresa al menú principal. */
    private JButton mainMenuButton;

    /** Crea la pantalla de pausa conectada con la navegación principal.
     * @param navigationController controlador para mostrar otras pantallas
     * @param resultsPreviewAction acción que actualiza y muestra la vista previa de resultados
     * @param saveMatchAction acción que selecciona el destino y guarda la partida
     */
    public PausePanel(NavigationController navigationController, Runnable resultsPreviewAction,
            Runnable saveMatchAction) {
        this.navigationController = navigationController;
        this.resultsPreviewAction = resultsPreviewAction;
        this.saveMatchAction = saveMatchAction;
        prepareView();
        prepareController();
    }

    /** Construye el fondo de cancha y la tarjeta central de pausa. */
    @Override
    public void prepareView() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        JPanel background = new CourtBackground();
        background.setLayout(new GridBagLayout());
        background.add(prepareViewPauseCard());
        add(background, BorderLayout.CENTER);
    }

    /** Crea la tarjeta con las acciones visibles del menú de pausa. */
    private JPanel prepareViewPauseCard() {
        RoundedPanel card = new RoundedPanel(Theme.BACKGROUND, 34);
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 28, 24, 28));
        card.setPreferredSize(new Dimension(360, 430));

        JLabel logo = new JLabel(loadLogo(), SwingConstants.CENTER);
        logo.setAlignmentX(CENTER_ALIGNMENT);
        card.add(logo);
        card.add(javax.swing.Box.createVerticalStrut(6));

        JLabel title = new JLabel("PAUSA", SwingConstants.CENTER);
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(Theme.DARK_GREEN);
        card.add(title);
        card.add(javax.swing.Box.createVerticalStrut(8));

        JLabel message = new JLabel("Partida en pausa", SwingConstants.CENTER);
        message.setAlignmentX(CENTER_ALIGNMENT);
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));
        message.setForeground(Theme.LABEL_COLOR);
        card.add(message);
        card.add(javax.swing.Box.createVerticalStrut(16));

        resumeButton = new RoundedButton("Reanudar partida",
                Theme.GREEN, Color.WHITE, Theme.SAND_BORDER, 14);
        resumeButton.setAlignmentX(CENTER_ALIGNMENT);
        resumeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(resumeButton);
        card.add(javax.swing.Box.createVerticalStrut(8));

        JButton restartButton = new RoundedButton("Reiniciar partida",
                Theme.SAND, Theme.DARK_GREEN, Theme.SAND_BORDER, 14);
        restartButton.setAlignmentX(CENTER_ALIGNMENT);
        restartButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(restartButton);
        card.add(javax.swing.Box.createVerticalStrut(8));

        saveButton = new RoundedButton("Guardar partida",
                Theme.SAND, Theme.DARK_GREEN, Theme.SAND_BORDER, 14);
        saveButton.setAlignmentX(CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(saveButton);
        card.add(javax.swing.Box.createVerticalStrut(8));

        resultsButton = new RoundedButton("Ver resultados", Theme.SAND,
                Theme.DARK_GREEN, Theme.SAND_BORDER, 14);
        resultsButton.setAlignmentX(CENTER_ALIGNMENT);
        resultsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(resultsButton);
        card.add(javax.swing.Box.createVerticalStrut(8));

        mainMenuButton = new RoundedButton("Volver al menú principal",
                Theme.SAND, Theme.DARK_GREEN, Theme.SAND_BORDER, 14);
        mainMenuButton.setAlignmentX(CENTER_ALIGNMENT);
        mainMenuButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(mainMenuButton);
        return card;
    }

    /** Conecta reanudar, guardar, ver resultados y volver al menú. */
    @Override
    public void prepareController() {
        // Volver a la cancha permite que la ventana reanude el reloj.
        resumeButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.MATCH));
        // La acción recibida muestra los resultados a partir de la captura vigente.
        resultsButton.addActionListener(event -> resultsPreviewAction.run());
        saveButton.addActionListener(event -> saveMatchAction.run());
        // La ventana mantiene el reloj detenido al volver al menú.
        mainMenuButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.MAIN_MENU));
    }

    /** Carga el logo del proyecto en el tamaño de la tarjeta. */
    private ImageIcon loadLogo() {
        URL url = getClass().getResource(LOGO_IMAGE);
        if (url == null) {
            return null;
        }
        Image image = new ImageIcon(url).getImage()
                .getScaledInstance(LOGO_SIZE, LOGO_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /** Recibe la imagen de cancha en el constructor y la escala al repintar el fondo. */
    private static final class CourtBackground extends JPanel {
        private final Image courtImage = loadCourtImage();

        private CourtBackground() {
            setOpaque(true);
            setBackground(new Color(0x29A8DB));
        }

        /** Dibuja el fondo de la pantalla de pausa.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            if (courtImage != null) {
                graphics.drawImage(courtImage, 0, 0, getWidth(), getHeight(), this);
            }
        }

        private static Image loadCourtImage() {
            URL url = PausePanel.class.getResource(COURT_IMAGE);
            return url == null ? null : new ImageIcon(url).getImage();
        }
    }
}
