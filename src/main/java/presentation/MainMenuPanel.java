
package presentation;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
 
/**
 * Pantalla del menú principal: jugar, abrir una partida, configuración y salir.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> {@code prepareView()} construye las opciones
 *       del menú y {@code loadLogo()} lee la imagen del classpath.</li>
 *   <li><strong>Modificación:</strong> {@code prepareController()} conecta los
 *       botones con {@link NavigationController}. {@code openMatch()} recibe
 *       la selección del archivo mediante un {@code JFileChooser} y delega su
 *       restauración a la ventana principal.</li>
 *   <li><strong>Actualización:</strong> {@code loadLogo()} asigna la imagen o
 *       el título alternativo. La ventana muestra el menú al navegar a él.</li>
 * </ul>
 *
 * <p><strong>Para agregar una opción:</strong> coloca el botón en
 * {@code prepareViewMainButtons()} o {@code prepareViewBottomButtons()} y
 * registra su acción en {@code prepareController()}. La navegación usa las
 * rutas de {@link NavigationController}; la ventana las registra en
 * {@code DOPOSoccerGUI.prepareViewScreens()}.</p>
 */
public class MainMenuPanel extends BasePanel 
{
    // Medidas del panel
    private static final int OUTER_MARGIN = 25;
    private static final int CONTENT_ARC = 50;
    private static final int CONTENT_PADDING_TOP = 30;
    private static final int CONTENT_PADDING_SIDES = 40;
    private static final int CONTENT_PADDING_BOTTOM = 30;
 
    // Medidas del logo
    private static final String LOGO_PATH = "/images/logo-menu.png";
    private static final int LOGO_HEIGHT = 180;
    private static final int LOGO_CARD_ARC = 40;
    private static final int LOGO_PADDING_TOP = 15;
    private static final int LOGO_PADDING_BOTTOM = 15;
    private static final int LOGO_PADDING_SIDES = 20;
 
    // Medidas de los botones y separaciones
    private static final int MAIN_BUTTON_HEIGHT = 64;
    private static final int BOTTOM_BUTTON_HEIGHT = 56;
    private static final int BOTTOM_BUTTONS_GAP = 25;
    private static final int SECTION_GAP = 30;
    private static final int LABEL_GAP = 12;
    private static final int BUTTON_GAP = 20;
 
    /** Controlador que coordina los cambios de pantalla. */
    private NavigationController navigationController;
    /** Acción que restaura una partida desde el archivo seleccionado. */
    private final Consumer<File> openMatchAction;
    /** Rótulo de título del menú inicial. */
    private JLabel titleLabel;
    /** Acción para ir a la configuración de una nueva partida. */
    private JButton playButton;
    /** Acción que permite elegir una partida guardada. */
    private JButton openMatchButton;
    /** Acción para abrir la configuración de teclas. */
    private JButton configurationButton;
    /** Acción de salida de la aplicación. */
    private JButton exitButton;
 
    /**
     * Crea el panel del menú principal.
     *
     * @param navigationController controlador que cambia de pantalla
     * @param openMatchAction operación que restaura el archivo seleccionado
     */
    public MainMenuPanel(NavigationController navigationController, Consumer<File> openMatchAction)
    {
        this.navigationController = navigationController;
        this.openMatchAction = openMatchAction;
        prepareView();
        prepareController();
    }
 
    // ------------------------------------------------------------------
    // Vista
    // ------------------------------------------------------------------
 
    @Override
    public void prepareView() 
    {
        prepareViewPanel();
        JPanel content = createContentPanel();
        prepareViewLogo(content);
        prepareViewModeLabel(content);
        prepareViewMainButtons(content);
        prepareViewBottomButtons(content);
        content.add(Box.createVerticalGlue());
        add(content, BorderLayout.CENTER);
    }
 
    /**
     * Configura el panel principal: distribución, color de fondo y márgenes.
     */
    private void prepareViewPanel() 
    {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(
                OUTER_MARGIN, OUTER_MARGIN, OUTER_MARGIN, OUTER_MARGIN));
    }
 
    /**
     * Crea el contenedor redondeado donde se colocan todos los elementos del menú.
     *
     * @return contenedor con distribución vertical
     */
    private JPanel createContentPanel() 
    {
        RoundedPanel content = new RoundedPanel(Theme.INNER_BACKGROUND, CONTENT_ARC);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(
                CONTENT_PADDING_TOP, CONTENT_PADDING_SIDES,
                CONTENT_PADDING_BOTTOM, CONTENT_PADDING_SIDES));
        return content;
    }
 
    /**
     * Crea la tarjeta blanca con el logo y la agrega al contenedor.
     *
     * @param content contenedor del menú
     */
    private void prepareViewLogo(JPanel content) 
    {
        RoundedPanel logoCard = new RoundedPanel(Color.WHITE, LOGO_CARD_ARC);
        logoCard.setLayout(new BoxLayout(logoCard, BoxLayout.Y_AXIS));
        logoCard.setBorder(BorderFactory.createEmptyBorder(
                LOGO_PADDING_TOP, LOGO_PADDING_SIDES,
                LOGO_PADDING_BOTTOM, LOGO_PADDING_SIDES));
        logoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                LOGO_HEIGHT + LOGO_PADDING_TOP + LOGO_PADDING_BOTTOM));
 
        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadLogo();
 
        logoCard.add(titleLabel);
        content.add(logoCard);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }
 
    /**
     * Carga el logo en el título. Si el archivo no se encuentra, muestra
     * el nombre del juego como texto.
     */
    private void loadLogo() 
    {
        URL url = getClass().getResource(LOGO_PATH);
        if (url == null) 
        {
            titleLabel.setText("DOPOSoccer");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
            titleLabel.setForeground(Theme.DARK_GREEN);
            return;
        }
        Image scaled = new ImageIcon(url).getImage()
                .getScaledInstance(-1, LOGO_HEIGHT, Image.SCALE_SMOOTH);
        titleLabel.setIcon(new ImageIcon(scaled));
    }
 
    /**
     * Agrega el texto que invita a elegir el modo de juego.
     *
     * @param content contenedor del menú
     */
    private void prepareViewModeLabel(JPanel content) 
    {
        JLabel modeLabel = new JLabel("ELIGE EL MODO DE JUEGO");
        modeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        modeLabel.setForeground(Theme.LABEL_COLOR);
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        content.add(modeLabel);
        content.add(Box.createVerticalStrut(LABEL_GAP));
    }
 
    // Menú de botones: content contiene los accesos grandes y bottomRow los inferiores.
    // Declara el botón como campo y conecta su clic en prepareController().
    /**
     * Crea los botones grandes (Jugar y Abrir Partida) y los agrega al contenedor.
     *
     * @param content contenedor del menú
     */
    private void prepareViewMainButtons(JPanel content) 
    {
        playButton = new RoundedButton("Jugar", Theme.GREEN, Color.WHITE, Theme.GREEN, 22);
        openMatchButton = new RoundedButton("Abrir Partida", Theme.SAND, Theme.DARK_GREEN, Theme.SAND_BORDER, 20);
        adjustMainButton(playButton);
        adjustMainButton(openMatchButton);
 
        content.add(playButton);
        content.add(Box.createVerticalStrut(BUTTON_GAP));
        content.add(openMatchButton);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }
 
    /**
     * Hace que un botón grande ocupe todo el ancho disponible con la altura definida.
     *
     * @param button botón que se ajustará
     */
    private void adjustMainButton(JButton button) 
    {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAIN_BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(100, MAIN_BUTTON_HEIGHT));
    }
 
    /**
     * Crea la fila inferior (Configuración y Salir) y la agrega al contenedor.
     *
     * @param content contenedor del menú
     */
    private void prepareViewBottomButtons(JPanel content) 
    {
        configurationButton = new RoundedButton("Configuración", Theme.BACKGROUND, Theme.DARK_GREEN, Theme.SAND_BORDER, 18);
        exitButton = new RoundedButton("Salir", Theme.RED, Theme.DARK_GREEN, Theme.RED_BORDER, 18);
 
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, BOTTOM_BUTTONS_GAP, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, BOTTOM_BUTTON_HEIGHT));
        bottomRow.setPreferredSize(new Dimension(100, BOTTOM_BUTTON_HEIGHT));
        bottomRow.add(configurationButton);
        bottomRow.add(exitButton);
 
        content.add(bottomRow);
    }
 
    @Override
    public void prepareController() 
    {
        prepareControllerPlay();
        prepareControllerOpenMatch();
        prepareControllerConfiguration();
        prepareControllerExit();
    }
 
    /**
     * Asocia el botón Jugar con la pantalla de personalización.
     */
    private void prepareControllerPlay() 
    {
        // Jugar abre el formulario donde se reúne la configuración de la partida.
        playButton.addActionListener(e -> 
            navigationController.showScreen(NavigationController.PERSONALIZATION));
    }
 
    /**
     * Asocia el botón Abrir Partida con la selección del archivo de la partida.
     */
    private void prepareControllerOpenMatch() 
    {
        // Este clic abre el selector de archivos.
        openMatchButton.addActionListener(e -> openMatch());
    }
 
    /**
     * Asocia el botón Configuración con la pantalla de configuración.
     */
    private void prepareControllerConfiguration() 
    {
        // Abre el panel que captura las asignaciones de teclado.
        configurationButton.addActionListener(e -> 
            navigationController.showScreen(NavigationController.CONFIGURATION));
    }
 
    /**
     * Asocia el botón Salir con el cierre del juego.
     */
    private void prepareControllerExit() 
    {
        // Delega el cierre al controlador de navegación.
        exitButton.addActionListener(e -> navigationController.exit());
    }
 
    /**
     * Muestra el selector de archivos para abrir una partida guardada.
     */
    private void openMatch()
    {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            openMatchAction.accept(chooser.getSelectedFile());
        }
    }

    /**
     * Recibe el color y el radio de esquina en el constructor y los usa
     * en {@code paintComponent} cada vez que Swing repinta el fondo del menú.
     */
    private static class RoundedPanel extends JPanel 
    {
        private final int arc;

        /** Crea el fondo del menú.
         * @param color relleno del panel
         * @param arc tamaño de las esquinas redondeadas
         */
        RoundedPanel(Color color, int arc)
        {
            this.arc = arc;
            setBackground(color);
            setOpaque(false);
        }

        /** Dibuja el fondo redondeado del panel.
         * @param g contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Botón con esquinas redondeadas y borde de color.
     */
    private static class RoundedButton extends JButton 
    {
        private static final int ARC = 22;
        private final Color borderColor;

        /** Crea un botón del menú con su rótulo, colores y tamaño de fuente.
         * @param text rótulo visible
         * @param background color de relleno
         * @param foreground color del texto
         * @param borderColor color del borde
         * @param fontSize tamaño del texto
         */
        RoundedButton(String text, Color background, Color foreground, Color borderColor, int fontSize)
        {
            super(text);
            this.borderColor = borderColor;
            setBackground(background);
            setForeground(foreground);
            setFont(new Font("SansSerif", Font.BOLD, fontSize));
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /** Dibuja el fondo y el borde del botón.
         * @param g contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getBackground();

            if (getModel().isPressed()) 
            {
                fill = fill.darker();
            }

            else if (getModel().isRollover()) 
            {
                fill = fill.brighter();
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
