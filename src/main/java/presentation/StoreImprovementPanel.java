package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Pantalla de la tienda de mejoras: cada jugador gasta sus puntos
 * en subir sus estadísticas.
 */
public class StoreImprovementPanel extends BasePanel 
{
    // Estadísticas que se pueden mejorar
    private static final String[] STAT_NAMES = {"Velocidad", "Salto", "Patada", "Vida"};

    // Valores provisionales mientras el dominio no maneje los puntos y los niveles
    private static final int PLAYER_COUNT = 2;
    private static final int INITIAL_POINTS = 0;
    private static final int UPGRADE_PRICE = 500;
    private static final int MAX_LEVEL = 10;

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

    private NavigationController navigationController;
    private int[] points;
    private int[][] levels;
    private JLabel player1PointsLabel;
    private JLabel player2PointsLabel;
    private JLabel[] player1LevelLabels;
    private JLabel[] player1PriceLabels;
    private JButton[] player1UpgradeButtons;
    private JLabel[] player2LevelLabels;
    private JLabel[] player2PriceLabels;
    private JButton[] player2UpgradeButtons;
    private JButton backButton;

    /**
     * Crea el panel de la tienda de mejoras.
     *
     * @param navigationController controlador que cambia de pantalla
     */
    public StoreImprovementPanel(NavigationController navigationController)
    {
        this.navigationController = navigationController;
        points = new int[PLAYER_COUNT];
        levels = new int[PLAYER_COUNT][STAT_NAMES.length];
        for (int player = 0; player < PLAYER_COUNT; player++) 
        {
            points[player] = INITIAL_POINTS;
        }
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
        refreshPlayer(0);
        refreshPlayer(1);
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
        JLabel label = new JLabel("Precio: " + UPGRADE_PRICE, SwingConstants.CENTER);
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
        prepareControllerUpgrades(player1UpgradeButtons, 0);
        prepareControllerUpgrades(player2UpgradeButtons, 1);
        prepareControllerBack();
    }

    /**
     * Asocia cada botón Mejorar de un jugador con la compra de esa mejora.
     *
     * @param buttons botones Mejorar del jugador
     * @param player índice del jugador (0 o 1)
     */
    private void prepareControllerUpgrades(JButton[] buttons, int player) 
    {
        for (int stat = 0; stat < buttons.length; stat++) 
        {
            final int selectedStat = stat;
            buttons[stat].addActionListener(e -> upgrade(player, selectedStat));
        }
    }

    /**
     * Asocia el botón Volver con la pantalla de personalización.
     */
    private void prepareControllerBack() 
    {
        backButton.addActionListener(e ->
            navigationController.showScreen(NavigationController.PERSONALIZATION));
    }

    // ----- Acciones -----

    /**
     * Suma puntos a un jugador (por ejemplo, los ganados en un partido).
     *
     * @param player índice del jugador (0 o 1)
     * @param amount puntos que se suman
     */
    public void addPoints(int player, int amount) 
    {
        // Pendiente: reemplazar por los puntos que entregue el dominio.
        points[player] += amount;
        refreshPlayer(player);
    }

    /**
     * Compra una mejora si el jugador tiene puntos y la estadística no está al máximo.
     *
     * @param player índice del jugador (0 o 1)
     * @param stat índice de la estadística
     */
    private void upgrade(int player, int stat) 
    {
        // Pendiente: delegar la compra al dominio cuando maneje puntos y niveles.
        if (levels[player][stat] >= MAX_LEVEL) 
        {
            return;
        }
        if (points[player] < UPGRADE_PRICE) 
        {
            JOptionPane.showMessageDialog(this,
                    "No tienes puntos suficientes para mejorar " + STAT_NAMES[stat] + ".",
                    "Puntos insuficientes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        points[player] -= UPGRADE_PRICE;
        levels[player][stat]++;
        refreshPlayer(player);
    }

    /**
     * Actualiza en pantalla los puntos, los niveles y los precios de un jugador.
     *
     * @param player índice del jugador (0 o 1)
     */
    private void refreshPlayer(int player) 
    {
        JLabel pointsLabel = (player == 0) ? player1PointsLabel : player2PointsLabel;
        JLabel[] levelLabels = (player == 0) ? player1LevelLabels : player2LevelLabels;
        JLabel[] priceLabels = (player == 0) ? player1PriceLabels : player2PriceLabels;

        pointsLabel.setText(String.valueOf(points[player]));
        for (int stat = 0; stat < STAT_NAMES.length; stat++) 
        {
            levelLabels[stat].setText(buildLevelBar(levels[player][stat]));
            priceLabels[stat].setText(levels[player][stat] >= MAX_LEVEL
                    ? MAX_PRICE_TEXT : "Precio: " + UPGRADE_PRICE);
        }
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