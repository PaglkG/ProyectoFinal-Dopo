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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import presentation.DOPOSoccerGUI.PresentationState;

/**
 * Pantalla para personalizar la partida: modalidad, perfil de la máquina,
 * tipo de partido, tamaño de la cancha y personaje de cada jugador.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> los desplegables de modalidad, regla y bot
 *       toman sus opciones de {@code DOPOSoccerGUI.PresentationState}.
 *       El recurso de opciones define los perfiles con poder fijo y el campo de cada regla.
 *       El catálogo del dominio determina cuáles de esas opciones están disponibles.</li>
 *   <li><strong>Modificación:</strong> el usuario modifica los controles.
 *       Al iniciar, {@code prepareControllerStart()} valida el límite,
 *       {@code createMatchConfiguration()} reúne las selecciones y
 *       {@code NavigationController.startMatch()} las entrega a la ventana.</li>
 *   <li><strong>Actualización:</strong> {@code updateMode()} ajusta el selector
 *       de bot, {@code updateMatchType()} cambia el campo de límite y
 *       {@code updateCountry()} refresca la imagen y el texto del personaje.</li>
 * </ul>
 *
 * <p><strong>Para añadir una opción dinámica:</strong> registra su clase en
 * el catálogo de dominio correspondiente y agrega su etiqueta/recurso visual
 * a {@code options.properties}. Este panel combina ambos orígenes; actualiza
 * {@code prepareView...} para el control, {@code update...} para visibilidad
 * o dependencias y {@code createMatchConfiguration()} para incluir la selección.</p>
 */
public class PersonalizationPanel extends BasePanel 
{
    // Opciones de cada lista desplegable
    private static final String[] FIELD_SIZES = {"800 x 450", "1024 x 576", "1280 x 720"};
    /** Perfiles con poder registrado, en el orden del recurso de opciones. */
    private java.util.List<CharacterProfile> availableProfiles = java.util.List.of();

    /** Identificador de la tarjeta para una regla sin formulario configurado. */
    private static final String UNAVAILABLE_CARD = "";

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

    /** Controlador que coordina los cambios de pantalla. */
    private NavigationController navigationController;
    /** Selector del modo manual o contra un bot. */
    private JComboBox<String> modeComboBox;
    /** Selector del perfil de comportamiento del bot. */
    private JComboBox<String> botProfileComboBox;
    /** Selector de la regla de finalización del partido. */
    private JComboBox<String> matchTypeComboBox;
    /** Campos de límite por regla, con su edición conservada al alternar la selección. */
    private final Map<String, JTextField> matchLimitFields = new LinkedHashMap<>();
    /** Contenedor que presenta el límite de la modalidad elegida. */
    private JPanel matchLimitPanel;
    /** Selector de las dimensiones de cancha. */
    private JComboBox<String> fieldSizeComboBox;
    /** Selector del país asociado al jugador 1. */
    private JComboBox<String> player1CountryComboBox;
    /** Vista previa gráfica del personaje del jugador 1. */
    private JLabel player1PreviewLabel;
    /** Descripción visible del personaje y del poder del jugador 1. */
    private JTextArea player1DescriptionTextArea;
    /** Rótulo del segundo selector de personaje. */
    private JLabel player2TitleLabel;
    /** Selector del país asociado al segundo participante. */
    private JComboBox<String> player2CountryComboBox;
    /** Vista previa gráfica del personaje del segundo participante. */
    private JLabel player2PreviewLabel;
    /** Descripción visible del personaje y del poder del segundo participante. */
    private JTextArea player2DescriptionTextArea;
    /** Acción que abre la tienda de mejoras. */
    private JButton storeButton;
    /** Acción que confirma la configuración del partido. */
    private JButton startButton;
    /** Acción para regresar al menú principal. */
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
        modeComboBox = createGameOptionsComboBox(PresentationState.getSessionTypes());
        modeComboBox.setSelectedItem(PresentationOptions.get("default.mode", ""));
        botProfileComboBox = createGameOptionsComboBox(PresentationState.getBotTypes());
        botProfileComboBox.setSelectedItem(PresentationOptions.get("default.bot", ""));

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
        // Los identificadores vienen del dominio y los rótulos se resuelven en el renderer.
        matchTypeComboBox = createGameOptionsComboBox(PresentationState.getMatchTypes());
        matchTypeComboBox.setSelectedItem(PresentationOptions.get("default.rule", ""));
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

    /** Convierte el catálogo publicado en el modelo del selector. El renderer
     * muestra un rótulo y el valor seleccionado conserva el identificador. */
    private JComboBox<String> createGameOptionsComboBox(java.util.List<String> options) {
        JComboBox<String> comboBox = new JComboBox<>(options.toArray(String[]::new));
        comboBox.setRenderer(new GameOptionRenderer());
        return comboBox;
    }

    /** Traduce los nombres de clase conocidos para mostrarlos en español. */
    private static final class GameOptionRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean selected, boolean focus) {
            String displayValue = value == null ? "" : PresentationOptions.label(value.toString());
            return super.getListCellRendererComponent(list, displayValue, index, selected, focus);
        }
    }

    /** Lee los identificadores publicados y crea una tarjeta por regla con
     * formulario en el recurso visual. Cada campo conserva su propio texto. */
    private void prepareViewMatchLimit()
    {
        matchLimitPanel = new JPanel(new CardLayout());
        matchLimitPanel.setOpaque(false);
        matchLimitFields.clear();
        // Conserva un campo por regla para que alternar selecciones no borre lo escrito.
        for (String rule : PresentationState.getMatchTypes()) {
            String label = PresentationOptions.get("option." + rule + ".limit.label", "");
            if (label.isEmpty()) continue;
            String initialValue = PresentationOptions.get("option." + rule + ".limit.default", "");
            JTextField field = new JTextField(initialValue);
            matchLimitFields.put(rule, field);
            matchLimitPanel.add(createLabeledField(label, field), rule);
        }
        matchLimitPanel.add(createLabeledField("LÍMITE", new JLabel("Regla sin formulario")), UNAVAILABLE_CARD);
    }

    /**
     * Crea la fila con el personaje de cada jugador.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewPlayers(JPanel content) 
    {
        // Un personaje aparece solo si su poder fijo está en el catálogo del dominio.
        var powers = PresentationState.getPowerTypes();
        availableProfiles = Arrays.stream(PresentationOptions.get("characters", "").split(","))
                .map(String::strip)
                .filter(code -> !code.isEmpty())
                .distinct()
                .map(CharacterProfile::new)
                .filter(profile -> powers.contains(profile.powerClassName()))
                .toList();
        String[] countries = availableProfiles.stream().map(CharacterProfile::country).toArray(String[]::new);
        player1CountryComboBox = new JComboBox<>(countries);
        player1CountryComboBox.setSelectedItem(new CharacterProfile(
                PresentationOptions.get("default.player1", "")).country());
        player1PreviewLabel = createPreviewLabel();
        player1DescriptionTextArea = createDescriptionTextArea();

        player2TitleLabel = createColumnTitle("");
        player2CountryComboBox = new JComboBox<>(countries);
        player2CountryComboBox.setSelectedItem(new CharacterProfile(
                PresentationOptions.get("default.player2", "")).country());
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
        // Menú de botones: inicio y regreso van en bottomRow; Tienda va en content.
        // Conecta la navegación o la validación del formulario en prepareController().
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
        // Refresca el selector del bot y el rótulo del segundo participante.
        modeComboBox.addActionListener(e -> updateMode());
    }

    /**
     * Asocia el tipo de partido con el límite que se muestra (tiempo o goles).
     */
    private void prepareControllerMatchType() 
    {
        // El nuevo tipo de partida muestra su campo de límite y actualiza el botón de inicio.
        matchTypeComboBox.addActionListener(e -> updateMatchType());
    }

    /**
     * Asocia cada selector de país con la vista previa y la descripción del personaje.
     */
    private void prepareControllerCountries() 
    {
        // Cada selector actualiza solo la imagen y la descripción de su columna.
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
        // La compra se tramita en la tienda y se vuelve aquí para configurar la partida.
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
            // Valida las opciones antes de reunir los datos que recibe la ventana.
            if (!canStartMatch()) return;
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
        // La navegación devuelve el formulario al menú principal.
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
        boolean againstBot = playsAgainstBot();
        botProfileComboBox.setEnabled(againstBot);
        player2TitleLabel.setText(againstBot ? "PAÍS DE LA MÁQUINA" : "PAÍS JUGADOR 2");
        updateStartAvailability();
    }

    /** Atiende un cambio del selector. La tarjeta muestra el campo de la
     * regla elegida y se vuelve a comprobar si puede habilitarse el inicio. */
    private void updateMatchType()
    {
        CardLayout layout = (CardLayout) matchLimitPanel.getLayout();
        String rule = (String) matchTypeComboBox.getSelectedItem();
        layout.show(matchLimitPanel, matchLimitFields.containsKey(rule) ? rule : UNAVAILABLE_CARD);
        updateStartAvailability();
    }

    /** Obtiene el formato de participantes configurado para el modo seleccionado. */
    private String selectedModeBotFlag() {
        return PresentationOptions.get("option." + modeComboBox.getSelectedItem() + ".bot", "");
    }

    /** Indica si el formato de participantes exige el perfil adicional del bot. */
    private boolean playsAgainstBot() {
        return "true".equals(selectedModeBotFlag());
    }

    /** Comprueba que las opciones seleccionadas tienen un formulario utilizable. */
    private boolean canStartMatch() {
        String botFlag = selectedModeBotFlag();
        return ("true".equals(botFlag) || "false".equals(botFlag))
                && (!playsAgainstBot() || botProfileComboBox.getSelectedItem() != null)
                && !availableProfiles.isEmpty()
                && matchLimitFields.containsKey(matchTypeComboBox.getSelectedItem());
    }

    /** Informa y deshabilita el inicio si todavía faltan opciones de configuración. */
    private void updateStartAvailability() {
        boolean available = canStartMatch();
        startButton.setEnabled(available);
        startButton.setToolTipText(available ? null : "Faltan opciones de configuración para iniciar.");
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
        if (index < 0 || index >= availableProfiles.size())
        {
            return;
        }
        CharacterProfile profile = availableProfiles.get(index);
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

    /** Lee los selectores y el límite validado al pulsar Comenzar. Convierte
     * las dimensiones a alto y ancho, ordena los poderes fijos de los perfiles
     * y añade el identificador del bot cuando la sesión lo requiere. */
    private MatchConfiguration createMatchConfiguration() {
        // El clic confirma la selección de personajes que aporta los dos poderes fijos.
        CharacterProfile playerOneProfile = selectedProfile(player1CountryComboBox);
        CharacterProfile playerTwoProfile = selectedProfile(player2CountryComboBox);
        String rule = (String) matchTypeComboBox.getSelectedItem();
        int limit = Integer.parseInt(matchLimitFields.get(rule).getText().trim());
        String[] size = ((String) fieldSizeComboBox.getSelectedItem()).split("\\s*x\\s*");
        // El selector muestra ancho por alto. Game recibe alto, ancho y límite.
        String height = size[1];
        String width = size[0];
        String typeMatch = "domain." + rule;
        boolean playerOne = true;
        boolean againstBot = playsAgainstBot();
        String typeGame = "domain." + modeComboBox.getSelectedItem();
        java.util.List<String> powers = new java.util.ArrayList<>();
        powers.add(playerOneProfile.powerClassName());
        powers.add(playerTwoProfile.powerClassName());
        if (againstBot) {
            // Solo una sesión con bot necesita un tercer valor en la lista de poderes.
            powers.add(selectedBotProfileClass());
        }
        return new MatchConfiguration(java.util.List.of(height, width, Integer.toString(limit)), powers,
                typeGame, typeMatch, playerOne, againstBot, playerOneProfile.countryCode(),
                playerTwoProfile.countryCode(), playerOneProfile.imageFile(), playerTwoProfile.imageFile());
    }

    /** Traduce el perfil visible al identificador que recibe la sesión de bot. */
    private String selectedBotProfileClass() {
        return (String) botProfileComboBox.getSelectedItem();
    }

    /** Obtiene los metadatos del personaje elegido en un desplegable. */
    private CharacterProfile selectedProfile(JComboBox<String> countryComboBox) {
        return availableProfiles.get(countryComboBox.getSelectedIndex());
    }

    /** Datos de selección y presentación de un personaje disponible. */
    private static final class CharacterProfile {
        private final String countryCode;

        /** Vincula un código a los metadatos del recurso. */
        private CharacterProfile(String countryCode) {
            this.countryCode = countryCode;
        }

        private String country() { return PresentationOptions.character(countryCode, "country", countryCode); }
        private String countryCode() { return countryCode; }
        private String animal() { return PresentationOptions.character(countryCode, "animal", ""); }
        private String imageFile() { return PresentationOptions.character(countryCode, "head", ""); }
        private String previewImageFile() { return PresentationOptions.character(countryCode, "preview", ""); }
        private String powerClassName() { return PresentationOptions.character(countryCode, "power", ""); }
        private String powerName() { return PresentationOptions.character(countryCode, "powerName", powerClassName()); }
        private String description() { return PresentationOptions.character(countryCode, "description", ""); }
    }

    /**
     * Verifica que el campo visible contenga un entero positivo.
     * Si no es así, avisa al jugador.
     *
     * @return {@code true} si el límite del partido es válido
     */
    private boolean isMatchLimitValid() 
    {
        JTextField field = matchLimitFields.get(matchTypeComboBox.getSelectedItem());
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
                "Ingresa un número entero positivo en el campo de límite de la partida.",
                "Dato no válido", JOptionPane.ERROR_MESSAGE);
        field.requestFocusInWindow();
        return false;
    }
}
