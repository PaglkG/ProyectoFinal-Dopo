package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import presentation.DOPOSoccerGUI.PresentationState;
import presentation.MatchSnapshot.Participant;
/**
 * Pantalla de la tienda de mejoras: cada jugador gasta sus puntos
 * en subir sus estadísticas.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> {@code refreshFromState()} lee
 *       {@code PresentationState.getMatch()} una vez. {@code refreshPlayer()}
 *       selecciona el humano por identidad con {@code getHumanPlayer()} y
 *       obtiene su saldo con {@code getPoints()}.</li>
 *   <li><strong>Modificación:</strong> {@code prepareControllerUpgrades()}
 *       envía la identidad y la clave de la habilidad por el callback recibido
 *       en el constructor. {@code DOPOSoccerGUI.improveStatistic()} tramita
 *       la compra con {@code Game.improveStatistics()} y vuelve a publicar el estado.</li>
 *   <li><strong>Actualización:</strong> {@code refreshPlayer()} escribe el saldo,
 *       actualiza las barras, los precios y la habilitación de los botones.
 *       {@code displayedLevel()} consulta la velocidad y deriva el nivel de vida
 *       a partir de la vida máxima. Los niveles de salto y patada y el precio
 *       de la siguiente compra requieren datos adicionales en la captura.</li>
 * </ul>
 */
public class StoreImprovementPanel extends BasePanel 
{
    // Estadísticas que se pueden mejorar
    private static final String[] STAT_NAMES = {"Velocidad", "Salto", "Patada", "Vida"};
    private static final String[] STAT_KEYS = {"speed", "jump", "kick", "survival"};

    private static final int PLAYER_COUNT = 2;
    private static final int INITIAL_UPGRADE_PRICE = 500;
    private static final int MAX_LEVEL = 10;
    private static final String UNAVAILABLE = "—";

    // Símbolos de la barra de nivel
    private static final String FILLED_LEVEL = "●";
    private static final String EMPTY_LEVEL = "○";
    private static final String MAX_PRICE_TEXT = "Nivel máximo";

    // Medidas del panel
    private static final int OUTER_MARGIN = 25;
    private static final int CONTENT_ARC = 50;
    private static final int CONTENT_PADDING_TOP = 18;
    private static final int CONTENT_PADDING_SIDES = 30;
    private static final int CONTENT_PADDING_BOTTOM = 18;
    private static final int CARD_ARC = 24;
    private static final int COLUMNS_GAP = 30;
    private static final int ROW_GAP = 8;
    private static final int ROW_HEIGHT = 64;
    private static final int SECTION_GAP = 16;
    private static final int BACK_BUTTON_WIDTH = 280;
    private static final int BACK_BUTTON_HEIGHT = 44;

    /** Controlador que coordina los cambios de pantalla. */
    private final NavigationController navigationController;
    /** Acción de compra que recibe la identidad humana y el nombre de la habilidad. */
    private final BiConsumer<Boolean, String> upgradeAction;
    /** Saldo de puntos visible del jugador 1. */
    private JLabel player1PointsLabel;
    /** Saldo de puntos visible del jugador 2. */
    private JLabel player2PointsLabel;
    /** Niveles visibles de las mejoras del jugador 1. */
    private JLabel[] player1LevelLabels;
    /** Precios visibles de las mejoras del jugador 1. */
    private JLabel[] player1PriceLabels;
    /** Acciones de mejora para las habilidades del jugador 1. */
    private JButton[] player1UpgradeButtons;
    /** Niveles visibles de las mejoras del jugador 2. */
    private JLabel[] player2LevelLabels;
    /** Precios visibles de las mejoras del jugador 2. */
    private JLabel[] player2PriceLabels;
    /** Acciones de mejora para las habilidades del jugador 2. */
    private JButton[] player2UpgradeButtons;
    /** Acción para regresar a la pantalla anterior. */
    private JButton backButton;

    /**
     * Crea el panel de la tienda de mejoras.
     *
     * @param navigationController controlador que cambia de pantalla
     * @param upgradeAction acción de la fachada para solicitar una mejora por identidad
     */
    public StoreImprovementPanel(NavigationController navigationController,
            BiConsumer<Boolean, String> upgradeAction)
    {
        this.navigationController = navigationController;
        this.upgradeAction = upgradeAction;
        prepareView();
        prepareController();
    }

    @Override
    public void prepareView() 
    {
        prepareViewPanel();
        JPanel content = createContentPanel();
        prepareViewPlayers(content);
        prepareViewBackButton(content);
        add(content, BorderLayout.CENTER);
        refreshFromState();
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
     * Crea el contenedor redondeado donde se colocan las dos columnas y el botón.
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
     * Crea la columna de cada jugador y las coloca lado a lado.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewPlayers(JPanel content) 
    {
        player1PointsLabel = createPointsLabel();
        player1LevelLabels = new JLabel[STAT_NAMES.length];
        player1PriceLabels = new JLabel[STAT_NAMES.length];
        player1UpgradeButtons = new JButton[STAT_NAMES.length];

        player2PointsLabel = createPointsLabel();
        player2LevelLabels = new JLabel[STAT_NAMES.length];
        player2PriceLabels = new JLabel[STAT_NAMES.length];
        player2UpgradeButtons = new JButton[STAT_NAMES.length];

        JPanel row = new JPanel(new GridLayout(1, PLAYER_COUNT, COLUMNS_GAP, 0));
        row.setOpaque(false);
        row.add(createPlayerColumn("JUGADOR 1", player1PointsLabel,
                player1LevelLabels, player1PriceLabels, player1UpgradeButtons));
        row.add(createPlayerColumn("JUGADOR 2", player2PointsLabel,
                player2LevelLabels, player2PriceLabels, player2UpgradeButtons));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        content.add(row);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }

    /**
     * Crea el botón para volver a la personalización y lo centra.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewBackButton(JPanel content) 
    {
        backButton = new RoundedButton("Volver a personalización",
                Theme.GREEN, Color.WHITE, Theme.GREEN, 16);
        Dimension size = new Dimension(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
        backButton.setPreferredSize(size);
        backButton.setMaximumSize(size);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(backButton);
        content.add(Box.createVerticalGlue());
    }

    // ----- Componentes reutilizables -----

    /**
     * Crea la etiqueta donde se muestran los puntos actuales de un jugador.
     *
     * @return etiqueta de puntos
     */
    private JLabel createPointsLabel() 
    {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(Theme.DARK_GREEN);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Crea la columna de un jugador: título, puntos y una fila por estadística.
     * Los componentes de cada fila se guardan en los arreglos recibidos.
     *
     * @param title título de la columna
     * @param pointsLabel etiqueta de los puntos del jugador
     * @param levelLabels arreglo donde se guardan las barras de nivel
     * @param priceLabels arreglo donde se guardan las etiquetas de precio
     * @param upgradeButtons arreglo donde se guardan los botones Mejorar
     * @return columna del jugador
     */
    private JPanel createPlayerColumn(String title, JLabel pointsLabel, JLabel[] levelLabels,
                                      JLabel[] priceLabels, JButton[] upgradeButtons) 
    {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(createHeaderLabel(title, 20, Font.BOLD));
        column.add(createHeaderLabel("Puntos actuales", 14, Font.PLAIN));
        column.add(pointsLabel);
        column.add(Box.createVerticalStrut(ROW_GAP));

        for (int stat = 0; stat < STAT_NAMES.length; stat++) 
        {
            levelLabels[stat] = createLevelLabel();
            priceLabels[stat] = createPriceLabel();
            upgradeButtons[stat] = new RoundedButton("Mejorar", Theme.GREEN, Color.WHITE, Theme.GREEN, 12);
            column.add(createStatRow(STAT_NAMES[stat], levelLabels[stat],
                    priceLabels[stat], upgradeButtons[stat]));
            column.add(Box.createVerticalStrut(ROW_GAP));
        }
        return column;
    }

    /**
     * Crea una etiqueta de encabezado centrada.
     *
     * @param text texto de la etiqueta
     * @param size tamaño de la letra
     * @param style estilo de la letra (negrita o normal)
     * @return etiqueta centrada
     */
    private JLabel createHeaderLabel(String text, int size, int style) 
    {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(Theme.DARK_GREEN);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Crea la etiqueta que muestra el nivel de una estadística.
     *
     * @return etiqueta de nivel
     */
    private JLabel createLevelLabel() 
    {
        JLabel label = new JLabel(buildLevelBar(0));
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(Theme.GREEN);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Crea la etiqueta que muestra el precio de una mejora.
     *
     * @return etiqueta de precio
     */
    private JLabel createPriceLabel() 
    {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(Theme.LABEL_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Crea la tarjeta de una estadística: nombre y nivel a la izquierda,
     * botón y precio a la derecha.
     *
     * @param name nombre de la estadística
     * @param levelLabel etiqueta del nivel
     * @param priceLabel etiqueta del precio
     * @param upgradeButton botón para mejorar
     * @return tarjeta de la estadística
     */
    private JPanel createStatRow(String name, JLabel levelLabel, JLabel priceLabel, JButton upgradeButton) 
    {
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(Theme.DARK_GREEN);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Box.createVerticalGlue());
        left.add(nameLabel);
        left.add(levelLabel);
        left.add(Box.createVerticalGlue());

        upgradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(Box.createVerticalGlue());
        right.add(upgradeButton);
        right.add(priceLabel);
        right.add(Box.createVerticalGlue());

        RoundedPanel row = new RoundedPanel(Color.WHITE, CARD_ARC);
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setPreferredSize(new Dimension(100, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        return row;
    }

    @Override
    public void prepareController() 
    {
        prepareControllerUpgrades(player1UpgradeButtons, true);
        prepareControllerUpgrades(player2UpgradeButtons, false);
        prepareControllerBack();
    }

    /**
     * Vincula cada clic en Mejorar con la identidad humana de su columna y
     * la clave de {@code STAT_KEYS} de su fila. El callback entrega la
     * solicitud a la ventana, que publica otra captura tras la compra.
     *
     * @param buttons botones Mejorar del jugador
     * @param playerOne indica si los botones corresponden al jugador 1
     */
    private void prepareControllerUpgrades(JButton[] buttons, boolean playerOne)
    {
        for (int stat = 0; stat < buttons.length; stat++) 
        {
            String statistic = STAT_KEYS[stat];
            // El clic envía la identidad humana y la clave de esta fila a la ventana.
            buttons[stat].addActionListener(e -> upgradeAction.accept(playerOne, statistic));
        }
    }

    /**
     * Asocia el botón Volver con la pantalla de personalización.
     */
    private void prepareControllerBack() 
    {
        // La tienda devuelve el control a la pantalla desde donde se configura la partida.
        backButton.addActionListener(e ->
            navigationController.showScreen(NavigationController.PERSONALIZATION));
    }

    // ----- Acciones -----

    /** Lee la última captura al abrir la tienda o completar una compra.
     * Las dos columnas se actualizan con esa misma captura. */
    void refreshFromState() {
        // Reutiliza el mismo estado para que ambas columnas correspondan al mismo paso.
        MatchSnapshot match = PresentationState.getMatch().orElse(null);
        refreshPlayer(true, match);
        refreshPlayer(false, match);
    }

    /**
     * Refresca la columna del jugador humano identificado por {@code playerOne}.
     * Antes de iniciar, muestra cero puntos, nivel uno y el precio inicial.
     * Con una captura, toma el saldo del humano y muestra un guion cuando
     * el dato no está disponible. Cada fila actualiza su barra de nivel, su
     * precio visible y su botón. La compra se habilita al terminar la partida
     * si la identidad humana existe y la habilidad está por debajo del máximo.
     * @param playerOne {@code true} para el jugador 1, {@code false} para el jugador 2
     * @param match captura publicada, o {@code null} antes de iniciar la partida
     */
    private void refreshPlayer(boolean playerOne, MatchSnapshot match)
    {
        JLabel pointsLabel;
        JLabel[] levelLabels;
        JLabel[] priceLabels;
        JButton[] upgradeButtons;
        // La identidad elige qué etiquetas y botones se van a escribir.
        if (playerOne) {
            pointsLabel = player1PointsLabel;
            levelLabels = player1LevelLabels;
            priceLabels = player1PriceLabels;
            upgradeButtons = player1UpgradeButtons;
        } else {
            pointsLabel = player2PointsLabel;
            levelLabels = player2LevelLabels;
            priceLabels = player2PriceLabels;
            upgradeButtons = player2UpgradeButtons;
        }

        boolean beforeMatch = match == null;
        Participant humanPlayer = null;
        String pointsText;
        String defaultPriceText;
        // Sin captura se muestran los valores iniciales. Con captura, se busca al humano por ID.
        if (beforeMatch) {
            pointsText = "0";
            defaultPriceText = "Precio: " + INITIAL_UPGRADE_PRICE;
        } else {
            humanPlayer = match.getHumanPlayer(playerOne).orElse(null);
            // En una partida contra bot puede faltar la identidad humana de esta columna.
            pointsText = humanPlayer == null ? UNAVAILABLE : Integer.toString(humanPlayer.getPoints());
            defaultPriceText = "Precio: " + UNAVAILABLE;
        }
        pointsLabel.setText(pointsText);

        // La captura del partido terminado habilita las compras de las identidades presentes.
        boolean purchasesEnabled = humanPlayer != null && match.isFinished();
        // El índice de la clave coincide con la fila y su botón de compra.
        for (int row = 0; row < STAT_KEYS.length; row++) {
            String statistic = STAT_KEYS[row];
            int level = displayedLevel(statistic, humanPlayer, beforeMatch);
            boolean atMaxLevel = level >= MAX_LEVEL;

            String levelText = level == 0 ? UNAVAILABLE : buildLevelBar(level);
            // La etiqueta de nivel máximo reemplaza el precio provisional de esta fila.
            String priceText = atMaxLevel ? MAX_PRICE_TEXT : defaultPriceText;
            levelLabels[row].setText(levelText);
            priceLabels[row].setText(priceText);
            upgradeButtons[row].setEnabled(purchasesEnabled && !atMaxLevel);
        }
    }

    /** Devuelve el nivel visible, o cero para una habilidad sin consulta disponible. */
    private int displayedLevel(String statistic, Participant player, boolean beforeMatch) {
        if (beforeMatch) return 1;
        if (player == null) return 0;
        return switch (statistic) {
            case "speed" -> player.getSpeedLevel();
            case "survival" -> 1 + (player.getMaxLife() - 100) / 10;
            default -> 0;
        };
    }

    /**
     * Construye la barra de nivel con círculos llenos y vacíos.
     *
     * @param level nivel actual de la estadística
     * @return texto de la barra, por ejemplo "●●●○○○○○○○"
     */
    private String buildLevelBar(int level) 
    {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < MAX_LEVEL; i++) 
        {
            bar.append(i < level ? FILLED_LEVEL : EMPTY_LEVEL);
        }
        return bar.toString();
    }
}
