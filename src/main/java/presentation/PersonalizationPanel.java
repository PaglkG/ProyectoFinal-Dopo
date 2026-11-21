package presentation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Pantalla para personalizar la partida: modalidad, perfil de la máquina,
 * tipo de partido, tamaño de la cancha y personaje de cada jugador.
 */
public class PersonalizationPanel extends BasePanel 
{
    // Opciones de cada lista desplegable
    private static final String[] MODES = {"Jugador vs Bot", "Jugador vs Jugador"};
    private static final String[] BOT_PROFILES = {"Torpe", "Ofensiva", "Defensiva", "Experta"};
    private static final String[] MATCH_TYPES = {"Partido por tiempo", "Partido por goles"};
    private static final String[] FIELD_SIZES = {"800 x 450", "1024 x 576", "1280 x 720"};
    private static final CharacterProfile[] CHARACTER_PROFILES = {
        new CharacterProfile("Corea del Sur", "KR", "Tigre", "tiger@4x-8.png", "tiger_escalado.png",
                "domain.BlueAura", "Aura Azul",
                "Un delantero explosivo con garras afiladas y un rugido capaz de estremecer el estadio. "
                + "Desata un disparo recto y horizontal envuelto en energía azul a máxima velocidad; "
                + "si el rival intenta bloquearlo, la onda de choque lo lanzará directo hacia su propia portería."),
        new CharacterProfile("Camerún", "CM", "Leopardo", "leopard@4x-8.png", "leopard_escalado.png",
                "domain.Lightning", "Rayo",
                "Ágil, veloz y letal en el contraataque. Canaliza la energía de la tormenta en sus botas "
                + "para lanzar un proyectil recto cargado de electricidad. Al hacer contacto, electrocuta "
                + "al oponente, dejándolo sin salto y reduciendo su velocidad a la mitad."),
        new CharacterProfile("Nigeria", "NG", "Lobo", "wolf@4x-8.png", "wolf_referencia.png",
                "domain.Tornado", "Tornado",
                "Un cazador implacable que controla el ritmo del partido con ferocidad. Invoca un remolino "
                + "a ras de suelo que rebota de forma impredecible; si alcanza al adversario, lo lanza por "
                + "los aires dejándolo inconsciente e incapaz de moverse, saltar o patear."),
        new CharacterProfile("Estados Unidos", "US", "Oso", "bear@4x-8.png", "bear_escalado.png",
                "domain.Illusion", "Ilusión",
                "Un peso pesado que domina el área con fuerza bruta y astucia inesperada. Al activar su "
                + "poder, multiplica el balón en 8 copias idénticas que confunden tanto al rival como a él "
                + "mismo, desatando el caos en la cancha hasta que las 7 ilusiones desaparecen.")
    };
    private static final String[] COUNTRIES = Arrays.stream(CHARACTER_PROFILES)
            .map(CharacterProfile::country)
            .toArray(String[]::new);

    // Nombres de las tarjetas que alternan entre tiempo y goles
    private static final String TIME_CARD = "TIME";
    private static final String GOALS_CARD = "GOALS";

    // Valores iniciales
    private static final String DEFAULT_MINUTES = "5";
    private static final String DEFAULT_GOALS = "3";
    private static final int DEFAULT_PLAYER1_COUNTRY = 0;
    private static final int DEFAULT_PLAYER2_COUNTRY = 2;

    // Medidas del panel
    private static final int OUTER_MARGIN = 15;
    private static final int CONTENT_ARC = 50;
    private static final int CONTENT_PADDING_TOP = 8;
    private static final int CONTENT_PADDING_SIDES = 40;
    private static final int CONTENT_PADDING_BOTTOM = 8;
    private static final int CARD_ARC = 24;
    private static final int SECTION_GAP = 8;
    private static final int LABEL_GAP = 3;
    private static final int GRID_HGAP = 14;

    // Medidas de los personajes
    private static final String COUNTRY_IMAGE_FOLDER = "/images/outfits/heads/Heads Scaled/";
    private static final int PREVIEW_SIZE = 80;
    private static final int COMBO_WIDTH = 200;
    private static final int DESCRIPTION_ROWS = 6;

    // Medidas de los botones
    private static final int STORE_BUTTON_HEIGHT = 36;
    private static final int BOTTOM_BUTTON_HEIGHT = 44;
    private static final int BOTTOM_BUTTONS_GAP = 20;

    private NavigationController navigationController;
    private JComboBox<String> modeComboBox;
    private JComboBox<String> botProfileComboBox;
    private JComboBox<String> matchTypeComboBox;
    private JTextField minutesTextField;
    private JTextField goalsTextField;
    private JPanel matchLimitPanel;
    private JComboBox<String> fieldSizeComboBox;
    private JComboBox<String> player1CountryComboBox;
    private JLabel player1PreviewLabel;
    private JTextArea player1DescriptionTextArea;
    private JLabel player2TitleLabel;
    private JComboBox<String> player2CountryComboBox;
    private JLabel player2PreviewLabel;
    private JTextArea player2DescriptionTextArea;
    private JButton storeButton;
    private JButton startButton;
    private JButton backToMenuButton;

    /**
     * Crea el panel de personalización de la partida.
     *
     * @param navigationController controlador que cambia de pantalla
     */
    public PersonalizationPanel(NavigationController navigationController) 
    {
        this.navigationController = navigationController;
        prepareView();
        prepareController();
    }

    @Override
    public void prepareView() 
    {
        prepareViewPanel();
        JPanel content = createContentPanel();
        prepareViewTitle(content);
        prepareViewModeOptions(content);
        prepareViewMatchOptions(content);
        prepareViewPlayers(content);
        prepareViewButtons(content);
        add(createScrollPane(content), BorderLayout.CENTER);
        updateMode();
        updateMatchType();
    }

    /** Permite desplazarse verticalmente cuando el contenido supera la ventana. */
    private JScrollPane createScrollPane(JPanel content)
    {
        JScrollPane scrollPane = new JScrollPane(content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
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
     * Crea el contenedor redondeado donde se colocan todas las secciones.
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
     * Agrega el título de la pantalla.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewTitle(JPanel content) 
    {
        JLabel title = new JLabel("PERSONALIZA TU PARTIDA");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Theme.DARK_GREEN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }

    /**
     * Crea la fila con la modalidad de juego y el perfil de la máquina.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewModeOptions(JPanel content) 
    {
        modeComboBox = new JComboBox<>(MODES);
        botProfileComboBox = new JComboBox<>(BOT_PROFILES);

        JPanel row = new JPanel(new GridLayout(1, 2, GRID_HGAP, 0));
        row.setOpaque(false);
        row.add(createLabeledField("¿CÓMO QUIERES JUGAR?", modeComboBox));
        row.add(createLabeledField("PERFIL DE LA MÁQUINA", botProfileComboBox));
        limitHeight(row);
        content.add(row);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }

    /**
     * Crea la fila con el tipo de partido, su límite (tiempo o goles)
     * y el tamaño de la cancha.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewMatchOptions(JPanel content) 
    {
        matchTypeComboBox = new JComboBox<>(MATCH_TYPES);
        fieldSizeComboBox = new JComboBox<>(FIELD_SIZES);
        prepareViewMatchLimit();

        JPanel row = new JPanel(new GridLayout(1, 3, GRID_HGAP, 0));
        row.setOpaque(false);
        row.add(createLabeledField("TIPO DE PARTIDA", matchTypeComboBox));
        row.add(matchLimitPanel);
        row.add(createLabeledField("TAMAÑO DE LA CANCHA", fieldSizeComboBox));
        limitHeight(row);
        content.add(row);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }

    /**
     * Crea el espacio que muestra los minutos o los goles según el tipo de partido.
     */
    private void prepareViewMatchLimit() 
    {
        minutesTextField = new JTextField(DEFAULT_MINUTES);
        goalsTextField = new JTextField(DEFAULT_GOALS);

        matchLimitPanel = new JPanel(new CardLayout());
        matchLimitPanel.setOpaque(false);
        matchLimitPanel.add(createLabeledField("TIEMPO (MINUTOS)", minutesTextField), TIME_CARD);
        matchLimitPanel.add(createLabeledField("NÚMERO DE GOLES", goalsTextField), GOALS_CARD);
    }

    /**
     * Crea la fila con el personaje de cada jugador.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewPlayers(JPanel content) 
    {
        player1CountryComboBox = new JComboBox<>(COUNTRIES);
        player1CountryComboBox.setSelectedIndex(DEFAULT_PLAYER1_COUNTRY);
        player1PreviewLabel = createPreviewLabel();
        player1DescriptionTextArea = createDescriptionTextArea();

        player2TitleLabel = createColumnTitle("");
        player2CountryComboBox = new JComboBox<>(COUNTRIES);
        player2CountryComboBox.setSelectedIndex(DEFAULT_PLAYER2_COUNTRY);
        player2PreviewLabel = createPreviewLabel();
        player2DescriptionTextArea = createDescriptionTextArea();

        JPanel row = new JPanel(new GridLayout(1, 2, GRID_HGAP, 0));
        row.setOpaque(false);
        row.add(createPlayerColumn(createColumnTitle("PAÍS JUGADOR 1"),
                player1CountryComboBox, player1PreviewLabel, player1DescriptionTextArea));
        row.add(createPlayerColumn(player2TitleLabel,
                player2CountryComboBox, player2PreviewLabel, player2DescriptionTextArea));
        limitHeight(row);
        content.add(row);
        content.add(Box.createVerticalStrut(SECTION_GAP));

        updateCountry(player1CountryComboBox, player1PreviewLabel, player1DescriptionTextArea);
        updateCountry(player2CountryComboBox, player2PreviewLabel, player2DescriptionTextArea);
    }

    /**
     * Crea los botones: tienda de mejoras, comenzar partido y volver al menú.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewButtons(JPanel content) 
    {
        storeButton = new RoundedButton("Tienda de mejoras", Theme.SAND, Theme.DARK_GREEN, Theme.SAND_BORDER, 15);
        storeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        storeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, STORE_BUTTON_HEIGHT));
        storeButton.setPreferredSize(new Dimension(100, STORE_BUTTON_HEIGHT));

        startButton = new RoundedButton("Comenzar partido", Theme.GREEN, Color.WHITE, Theme.GREEN, 16);
        backToMenuButton = new RoundedButton("Volver al menú", Theme.BACKGROUND, Theme.DARK_GREEN, Theme.SAND_BORDER, 16);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, BOTTOM_BUTTONS_GAP, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, BOTTOM_BUTTON_HEIGHT));
        bottomRow.setPreferredSize(new Dimension(100, BOTTOM_BUTTON_HEIGHT));
        bottomRow.add(startButton);
        bottomRow.add(backToMenuButton);

        content.add(storeButton);
        content.add(Box.createVerticalStrut(SECTION_GAP));
        content.add(bottomRow);
        content.add(Box.createVerticalGlue());
    }


    /**
     * Crea una columna con un título pequeño encima de un campo.
     *
     * @param title texto del título
     * @param field componente que se coloca debajo del título
     * @return columna con el título y el campo
     */
    private JPanel createLabeledField(String title, JComponent field) 
    {
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(Theme.LABEL_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(label);
        column.add(Box.createVerticalStrut(LABEL_GAP));
        column.add(field);
        return column;
    }

    /**
     * Crea el título centrado de la columna de un jugador.
     *
     * @param text texto del título
     * @return etiqueta con el título
     */
    private JLabel createColumnTitle(String text) 
    {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(Theme.DARK_GREEN);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Crea la etiqueta donde se muestra la vista previa del personaje.
     *
     * @return etiqueta de vista previa
     */
    private JLabel createPreviewLabel() 
    {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Theme.DARK_GREEN);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        Dimension size = new Dimension(COMBO_WIDTH, PREVIEW_SIZE + 22);
        label.setPreferredSize(size);
        label.setMinimumSize(size);
        label.setMaximumSize(size);
        return label;
    }

    /**
     * Crea el área de texto (solo lectura) con la descripción del personaje.
     *
     * @return área de texto con ajuste de línea
     */
    private JTextArea createDescriptionTextArea() 
    {
        JTextArea area = new JTextArea(DESCRIPTION_ROWS, 20);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(new Font("SansSerif", Font.PLAIN, 12));
        area.setForeground(Theme.DARK_GREEN);
        return area;
    }

    /**
     * Arma la columna de un jugador: país, vista previa del animal y descripción.
     *
     * @param title título de la columna
     * @param comboBox selector del país
     * @param preview vista previa del personaje
     * @param description descripción del personaje
     * @return columna del jugador
     */
    private JPanel createPlayerColumn(JLabel title, JComboBox<String> comboBox,
                                      JLabel preview, JTextArea description) 
    {
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboBox.setMaximumSize(new Dimension(COMBO_WIDTH, comboBox.getPreferredSize().height));

        RoundedPanel descriptionCard = new RoundedPanel(Color.WHITE, CARD_ARC);
        descriptionCard.setLayout(new BorderLayout());
        descriptionCard.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        descriptionCard.add(description, BorderLayout.CENTER);
        descriptionCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                descriptionCard.getPreferredSize().height));

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(title);
        column.add(Box.createVerticalStrut(LABEL_GAP));
        column.add(comboBox);
        column.add(Box.createVerticalStrut(SECTION_GAP));
        column.add(createColumnTitle("VISTA PREVIA"));
        column.add(preview);
        column.add(Box.createVerticalStrut(LABEL_GAP));
        column.add(createColumnTitle("DESCRIPCIÓN"));
        column.add(Box.createVerticalStrut(LABEL_GAP));
        column.add(descriptionCard);
        return column;
    }

    /**
     * Evita que un componente crezca en alto dentro del {@code BoxLayout}
     * y lo alinea a la izquierda. Debe llamarse cuando el componente ya
     * tiene sus hijos.
     *
     * @param component componente que se limitará
     */
    private void limitHeight(JComponent component) 
    {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    @Override
    public void prepareController() 
    {
        prepareControllerMode();
        prepareControllerMatchType();
        prepareControllerCountries();
        prepareControllerStore();
        prepareControllerStart();
        prepareControllerBack();
    }

    /**
     * Asocia la modalidad con el perfil de la máquina y el título del jugador 2.
     */
    private void prepareControllerMode() 
    {
        modeComboBox.addActionListener(e -> updateMode());
    }

    /**
     * Asocia el tipo de partido con el límite que se muestra (tiempo o goles).
     */
    private void prepareControllerMatchType() 
    {
        matchTypeComboBox.addActionListener(e -> updateMatchType());
    }

    /**
     * Asocia cada selector de país con la vista previa y la descripción del personaje.
     */
    private void prepareControllerCountries() 
    {
        player1CountryComboBox.addActionListener(e ->
            updateCountry(player1CountryComboBox, player1PreviewLabel, player1DescriptionTextArea));
        player2CountryComboBox.addActionListener(e ->
            updateCountry(player2CountryComboBox, player2PreviewLabel, player2DescriptionTextArea));
    }

    /**
     * Asocia el botón de la tienda con la pantalla de mejoras.
     */
    private void prepareControllerStore() 
    {
        storeButton.addActionListener(e ->
            navigationController.showScreen(NavigationController.STORE_IMPROVEMENT));
    }

    /**
     * Asocia el botón Comenzar partido con la pantalla de preparación,
     * siempre que el límite del partido sea válido.
     */
    private void prepareControllerStart() 
    {
        startButton.addActionListener(e -> 
        {
            if (isMatchLimitValid()) {
                try {
                    navigationController.startMatch(createMatchConfiguration());
                } catch (RuntimeException exception) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo iniciar el partido: " + exception.getMessage(),
                            "Error al iniciar", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Asocia el botón Volver con el regreso al menú principal.
     */
    private void prepareControllerBack() 
    {
        backToMenuButton.addActionListener(e ->
            navigationController.showScreen(NavigationController.MAIN_MENU));
    }

    // ----- Acciones -----

    /**
     * Activa el perfil de la máquina solo si se juega contra ella y
     * ajusta el título del jugador 2.
     */
    private void updateMode() 
    {
        boolean againstBot = modeComboBox.getSelectedIndex() == 0;
        botProfileComboBox.setEnabled(againstBot);
        player2TitleLabel.setText(againstBot ? "PAÍS DE LA MÁQUINA" : "PAÍS JUGADOR 2");
    }

    /**
     * Muestra el campo de minutos o el de goles según el tipo de partido.
     */
    private void updateMatchType() 
    {
        CardLayout layout = (CardLayout) matchLimitPanel.getLayout();
        boolean byTime = matchTypeComboBox.getSelectedIndex() == 0;
        layout.show(matchLimitPanel, byTime ? TIME_CARD : GOALS_CARD);
    }

    /**
     * Actualiza la vista previa y la descripción del personaje de un jugador.
     *
     * @param comboBox selector del país
     * @param preview etiqueta de la vista previa
     * @param description área de la descripción
     */
    private void updateCountry(JComboBox<String> comboBox, JLabel preview, JTextArea description) 
    {
        int index = comboBox.getSelectedIndex();
        if (index < 0 || index >= CHARACTER_PROFILES.length)
        {
            return;
        }
        CharacterProfile profile = CHARACTER_PROFILES[index];
        description.setText("Tiro de Poder: " + profile.powerName()
                + "\nDescripción: " + profile.description());
        description.setCaretPosition(0);
        loadPreview(preview, profile);
    }

    /**
     * Carga la imagen del personaje. Si el archivo no se encuentra, muestra
     * el nombre del país como texto.
     *
     * @param preview etiqueta de la vista previa
     * @param profile personaje que se mostrará
     */
    private void loadPreview(JLabel preview, CharacterProfile profile)
    {
        URL url = getClass().getResource(COUNTRY_IMAGE_FOLDER + profile.previewImageFile());
        if (url == null) 
        {
            preview.setIcon(null);
            preview.setText(profile.animal());
            return;
        }
        Image scaled = new ImageIcon(url).getImage()
                .getScaledInstance(-1, PREVIEW_SIZE, Image.SCALE_SMOOTH);
        preview.setText(profile.animal());
        preview.setIcon(new ImageIcon(scaled));
    }

    /** Convierte las selecciones actuales al orden de parámetros acordado por Game. */
    private MatchConfiguration createMatchConfiguration() {
        CharacterProfile playerOneProfile = selectedProfile(player1CountryComboBox);
        CharacterProfile playerTwoProfile = selectedProfile(player2CountryComboBox);
        int limit = Integer.parseInt((matchTypeComboBox.getSelectedIndex() == 0
                ? minutesTextField : goalsTextField).getText().trim());
        String[] size = ((String) fieldSizeComboBox.getSelectedItem()).split("\\s*x\\s*");
        String height = size[1];
        String width = size[0];
        String typeMatch = matchTypeComboBox.getSelectedIndex() == 0
                ? "domain.ByTime" : "domain.ByGoals";
        boolean playerOne = true;
        boolean againstBot = modeComboBox.getSelectedIndex() == 0;
        String typeGame = againstBot ? "domain.PlayerVsBot" : "domain.PlayerVsPlayer";
        java.util.List<String> powers = new java.util.ArrayList<>();
        powers.add(playerOneProfile.powerClassName());
        powers.add(playerTwoProfile.powerClassName());
        if (againstBot) {
            powers.add(selectedBotProfileClass());
        }
        return new MatchConfiguration(java.util.List.of(height, width, Integer.toString(limit)), powers,
                typeGame, typeMatch, playerOne, againstBot, playerOneProfile.countryCode(),
                playerTwoProfile.countryCode(), playerOneProfile.imageFile(), playerTwoProfile.imageFile());
    }

    /** Traduce el perfil visible al identificador que recibe la sesión de bot. */
    private String selectedBotProfileClass() {
        return switch (botProfileComboBox.getSelectedIndex()) {
            case 0 -> "Clumsy";
            case 1 -> "Offensive";
            case 2 -> "Defensive";
            default -> "Expert";
        };
    }

    /** Obtiene los metadatos del personaje elegido en un desplegable. */
    private CharacterProfile selectedProfile(JComboBox<String> countryComboBox) {
        return CHARACTER_PROFILES[countryComboBox.getSelectedIndex()];
    }

    private record CharacterProfile(String country, String countryCode, String animal, String imageFile,
                                    String previewImageFile, String powerClassName, String powerName,
                                    String description) { }

    /**
     * Verifica que el campo visible (minutos o goles) contenga un entero positivo.
     * Si no es así, avisa al jugador.
     *
     * @return {@code true} si el límite del partido es válido
     */
    private boolean isMatchLimitValid() 
    {
        boolean byTime = matchTypeComboBox.getSelectedIndex() == 0;
        JTextField field = byTime ? minutesTextField : goalsTextField;
        String name = byTime ? "minutos" : "goles";
        try 
        {
            if (Integer.parseInt(field.getText().trim()) > 0) 
            {
                return true;
            }
        } 
        catch (NumberFormatException ex) 
        {
            // Se informa abajo, igual que cuando el número no es positivo.
        }
        JOptionPane.showMessageDialog(this,
                "Ingresa un número entero positivo de " + name + ".",
                "Dato no válido", JOptionPane.ERROR_MESSAGE);
        field.requestFocusInWindow();
        return false;
    }
}
