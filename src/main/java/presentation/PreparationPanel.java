package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Pantalla que anuncia la preparación del partido.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> la ventana llama a {@code refreshFromState()}
 *       al abrir la pantalla. El método lee {@code PresentationState.getMatch()}
 *       y obtiene los códigos de país de la configuración publicada.</li>
 *   <li><strong>Modificación:</strong> {@code prepareControllerCancel()} y
 *       {@code prepareControllerContinue()} solicitan la navegación a
 *       personalización y a la cancha, respectivamente.</li>
 *   <li><strong>Actualización:</strong> {@code setCountryCodes()} actualiza
 *       las banderas y {@code setReady()} marca el progreso al 100 % y cambia
 *       el mensaje cuando ya hay una partida publicada.</li>
 * </ul>
 */
public class PreparationPanel extends BasePanel {
    private static final String LOADING_BACKGROUND = "/images/ui/FONDO DE CARGA.png";
    private static final String BALL_IMAGE = "/images/ball/ball@4x-8.png";
    private static final String COUNTRY_FLAGS_DIRECTORY = "/images/characters/countries/";
    private static final int COUNTRY_FLAG_SIZE = 58;
    private static final int COUNTRY_CARD_WIDTH = 350;
    private static final int COUNTRY_CARD_HEIGHT = 94;
    private static final int PROGRESS_BAR_HEIGHT = 28;

    /** Controlador que coordina los cambios de pantalla. */
    private final NavigationController navigationController;

    /** Componente que presenta la bandera del lado izquierdo. */
    private JLabel leftCountryFlagLabel;
    /** Componente que presenta la bandera del lado derecho. */
    private JLabel rightCountryFlagLabel;
    /** Texto de estado de la preparación de la partida. */
    private JLabel statusLabel;
    /** Texto orientativo que acompaña al indicador de preparación. */
    private JLabel progressHintLabel;
    /** Indicador visual del avance de la preparación. */
    private JProgressBar progressBar;
    /** Acción para cancelar la preparación. */
    private JButton cancelButton;
    /** Acción para pasar de la preparación a la partida. */
    private JButton continueButton;

    /**
     * Crea la pantalla de carga y conecta sus acciones con la navegación.
     *
     * @param navigationController controlador de las transiciones de pantalla
     */
    public PreparationPanel(NavigationController navigationController) {
        this.navigationController = navigationController;
        prepareView();
        prepareController();
    }

    /** Construye la estructura visual por secciones. */
    @Override
    public void prepareView() {
        prepareViewPanel();
        prepareViewHeader();
        prepareViewLoadingBody();
    }

    /** Configura el contenedor raíz de la pantalla. */
    private void prepareViewPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 14, 18));
        setBackground(Theme.BACKGROUND);
    }

    /** Crea la franja superior con el título y el balón. */
    private void prepareViewHeader() {
        RoundedPanel header = new RoundedPanel(Theme.DARK_GREEN, 22);
        header.setLayout(new BorderLayout(10, 0));
        header.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));

        JLabel titleLabel = new JLabel("04  ·  PREPARANDO EL PARTIDO");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createBallIcon(22));
        titleLabel.setIconTextGap(9);
        header.add(titleLabel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
    }

    /** Organiza los códigos de país, el mensaje, la barra y el botón. */
    private void prepareViewLoadingBody() {
        JPanel body = new FieldBackgroundPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        body.add(Box.createVerticalGlue());
        prepareViewCountryCard(body);
        body.add(Box.createVerticalStrut(22));
        prepareViewStatus(body);
        body.add(Box.createVerticalStrut(16));
        prepareViewProgressBar(body);
        body.add(Box.createVerticalStrut(12));
        prepareViewProgressHint(body);
        body.add(Box.createVerticalStrut(15));
        prepareViewPreparationButtons(body);
        body.add(Box.createVerticalGlue());

        add(body, BorderLayout.CENTER);
    }

    /** Muestra las banderas de los países alrededor del balón. */
    private void prepareViewCountryCard(JPanel body) {
        RoundedPanel card = new RoundedPanel(Theme.INNER_BACKGROUND, 26);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(COUNTRY_CARD_WIDTH, COUNTRY_CARD_HEIGHT));
        card.setPreferredSize(new Dimension(COUNTRY_CARD_WIDTH, COUNTRY_CARD_HEIGHT));

        JPanel codes = new JPanel(new BorderLayout(14, 0));
        codes.setOpaque(false);
        prepareViewCountryCodes(codes);
        card.add(codes, BorderLayout.CENTER);

        JLabel subtitle = new JLabel("¡Los jugadores saltan al campo!", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        subtitle.setForeground(Theme.DARK_GREEN);
        card.add(subtitle, BorderLayout.SOUTH);
        body.add(card);
    }

    /** Inicializa las banderas que después se actualizan desde el menú. */
    private void prepareViewCountryCodes(JPanel codes) {
        leftCountryFlagLabel = createCountryFlagLabel("KR");
        rightCountryFlagLabel = createCountryFlagLabel("NG");
        JLabel ball = new JLabel(createBallIcon(34));
        ball.setHorizontalAlignment(SwingConstants.CENTER);

        codes.add(leftCountryFlagLabel, BorderLayout.WEST);
        codes.add(ball, BorderLayout.CENTER);
        codes.add(rightCountryFlagLabel, BorderLayout.EAST);
    }

    /** Crea una etiqueta para la bandera de un país. */
    private JLabel createCountryFlagLabel(String code) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(90, 64));
        setCountryFlag(label, code);
        return label;
    }

    /** Muestra el mensaje central mientras se prepara la partida. */
    private void prepareViewStatus(JPanel body) {
        statusLabel = new JLabel("Preparando el césped y reuniendo a los jugadores",
                SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(statusLabel);
    }

    /** Crea una barra animada porque el dominio aún no informa un porcentaje real. */
    private void prepareViewProgressBar(JPanel body) {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(new Color(0x2E9A70));
        progressBar.setBackground(new Color(0xD9F3E6));
        progressBar.setOpaque(true);
        progressBar.setPreferredSize(new Dimension(420, PROGRESS_BAR_HEIGHT));
        progressBar.setMaximumSize(new Dimension(520, PROGRESS_BAR_HEIGHT));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(progressBar);
    }

    /** Añade un texto breve bajo la barra; no implica una duración fija. */
    private void prepareViewProgressHint(JPanel body) {
        progressHintLabel = new JLabel("El partido está por comenzar", SwingConstants.CENTER);
        progressHintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        progressHintLabel.setForeground(Color.WHITE);
        progressHintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(progressHintLabel);
    }

    /** Crea los botones para volver a editar o entrar al partido preparado. */
    private void prepareViewPreparationButtons(JPanel body) {
        JPanel buttons = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(360, 40));

        cancelButton = new RoundedButton("Cancelar", Theme.BACKGROUND, Theme.DARK_GREEN,
                Theme.SAND_BORDER, 14);
        cancelButton.setPreferredSize(new Dimension(170, 40));
        cancelButton.setMaximumSize(new Dimension(170, 40));
        continueButton = new RoundedButton("Entrar a la cancha", Theme.GREEN, Color.WHITE,
                Theme.GREEN, 14);
        buttons.add(cancelButton);
        buttons.add(continueButton);
        body.add(buttons);
    }

    /**
     * Actualiza las banderas cuando se reciben los países seleccionados.
     *
     * @param leftCode código del país del jugador 1
     * @param rightCode código del país del jugador 2 o de la máquina
     */
    public void setCountryCodes(String leftCode, String rightCode) {
        setCountryFlag(leftCountryFlagLabel, leftCode);
        setCountryFlag(rightCountryFlagLabel, rightCode);
    }

    /** Carga la bandera asociada al código; conserva el código si no hay recurso. */
    private void setCountryFlag(JLabel label, String code) {
        String normalizedCode = normalizeCountryCode(code);
        String fileName = countryFlagFileName(normalizedCode);
        URL imageUrl = fileName == null ? null
                : getClass().getResource(COUNTRY_FLAGS_DIRECTORY + fileName);

        if (imageUrl == null) {
            label.setIcon(null);
            label.setText(normalizedCode);
            label.setFont(new Font("SansSerif", Font.BOLD, 18));
            label.setForeground(new Color(0xE8783D));
            label.setToolTipText(null);
            return;
        }

        Image image = new ImageIcon(imageUrl).getImage()
                .getScaledInstance(COUNTRY_FLAG_SIZE, COUNTRY_FLAG_SIZE, Image.SCALE_SMOOTH);
        label.setText("");
        label.setIcon(new ImageIcon(image));
        label.setToolTipText(normalizedCode);
    }

    /** Relaciona cada código usado por la personalización con su recurso visual. */
    private String countryFlagFileName(String countryCode) {
        return switch (countryCode) {
            case "KR" -> "corea-del-sur@4x-8.png";
            case "CM" -> "camerun@4x-8.png";
            case "NG" -> "nigeria@4x-8.png";
            case "US" -> "estados-unidos@4x-8.png";
            default -> null;
        };
    }

    /** Muestra los países y confirma que la ventana ya publicó una partida. */
    void refreshFromState() {
        DOPOSoccerGUI.PresentationState.getMatch().ifPresent(match -> {
            MatchConfiguration configuration = match.getConfiguration();
            setCountryCodes(configuration.countryCodeForSide(false),
                    configuration.countryCodeForSide(true));
            setReady();
        });
    }

    /** Marca el progreso real como completo después de crear la sesión en Game. */
    public void setReady() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressBar.setStringPainted(true);
        progressBar.setString("100 %");
        statusLabel.setText("El partido está listo");
        progressHintLabel.setText("Selecciona Entrar a la cancha para comenzar");
    }

    private String normalizeCountryCode(String code) {
        if (code == null || code.isBlank()) {
            return "??";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** Conecta las acciones de la pantalla. */
    @Override
    public void prepareController() {
        prepareControllerCancel();
        prepareControllerContinue();
    }

    /** Hace que Cancelar regrese a la personalización de la partida. */
    private void prepareControllerCancel() {
        cancelButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.PERSONALIZATION));
    }

    /** Abre la cancha preparada y actualiza su instantánea visual. */
    private void prepareControllerContinue() {
        continueButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.MATCH));
    }

    /** Carga y escala el icono del balón usado en el título y entre los códigos. */
    private ImageIcon createBallIcon(int size) {
        URL imageUrl = getClass().getResource(BALL_IMAGE);
        if (imageUrl == null) {
            return null;
        }
        Image image = new ImageIcon(imageUrl).getImage()
                .getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /** Recibe la imagen de cancha en el constructor y la dibuja en
     * {@code paintComponent} con el tamaño actual del panel. */
    private static final class FieldBackgroundPanel extends JPanel {
        private final Image backgroundImage;

        private FieldBackgroundPanel() {
            URL imageUrl = PreparationPanel.class.getResource(LOADING_BACKGROUND);
            backgroundImage = imageUrl == null ? null : new ImageIcon(imageUrl).getImage();
            setOpaque(true);
            setBackground(new Color(0x28619A));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (backgroundImage == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g.dispose();
        }
    }
}
