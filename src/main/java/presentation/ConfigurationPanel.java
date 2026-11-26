package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Pantalla de configuración de los controles de teclado de cada jugador.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> {@code prepareViewKeyGrid()} crea los
 *       botones a partir de las asignaciones iniciales. Los eventos de teclado
 *       llegan a {@code captureKey()} desde {@code prepareControllerKeyButton()}.</li>
 *   <li><strong>Modificación:</strong> {@code assignKey()} comprueba si otra
 *       acción usa esa tecla y registra el cambio en el texto del botón.
 *       {@code prepareControllerSave()} regresa al menú. La persistencia de
 *       las asignaciones queda pendiente.</li>
 *   <li><strong>Actualización:</strong> {@code startKeyCapture()} muestra la
 *       espera, {@code assignKey()} muestra la tecla elegida y
 *       {@code cancelKeyCapture()} restaura el texto anterior.</li>
 * </ul>
 */
public class ConfigurationPanel extends BasePanel 
{
    // Acciones y teclas por defecto (mismo orden en las dos listas de teclas)
    private static final String[] ACTION_NAMES = {"Derecha", "Izquierda", "Salto", "Patear", "Poder"};
    private static final int[] PLAYER1_DEFAULT_KEYS = {
        KeyEvent.VK_D, KeyEvent.VK_A, KeyEvent.VK_W, KeyEvent.VK_SPACE, KeyEvent.VK_SHIFT};
    private static final int[] PLAYER2_DEFAULT_KEYS = {
        KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_0, KeyEvent.VK_CONTROL};

    // Medidas del panel
    private static final int OUTER_MARGIN = 25;
    private static final int CONTENT_ARC = 50;
    private static final int CONTENT_PADDING_TOP = 18;
    private static final int CONTENT_PADDING_SIDES = 40;
    private static final int CONTENT_PADDING_BOTTOM = 18;
    private static final int SECTION_GAP = 14;
    private static final int TITLE_GAP = 8;

    // Medidas de la tabla de teclas y de los botones
    private static final int KEY_BUTTON_HEIGHT = 28;
    private static final int KEY_GRID_HGAP = 12;
    private static final int KEY_GRID_VGAP = 6;
    private static final int BOTTOM_BUTTON_WIDTH = 130;
    private static final int BOTTOM_BUTTON_HEIGHT = 44;

    // Textos y propiedades usadas al cambiar una tecla
    private static final String WAITING_TEXT = "Presiona una tecla...";
    private static final String WAITING_PROPERTY = "waitingForKey";
    private static final String PREVIOUS_TEXT_PROPERTY = "previousKeyText";

    /** Controlador que coordina los cambios de pantalla. */
    private NavigationController navigationController;
    /** Nombres de las acciones de las filas de configuración. */
    private JLabel[] actionLabels;
    /** Controles para editar las teclas asignadas al jugador 1. */
    private JButton[] player1KeyButtons;
    /** Controles para editar las teclas asignadas al jugador 2. */
    private JButton[] player2KeyButtons;
    /** Acción que aplica las asignaciones de teclas editadas. */
    private JButton saveButton;
    /** Acción para regresar a la pantalla anterior. */
    private JButton backButton;
 
    /**
     * Crea el panel de configuración.
     *
     * @param navigationController controlador que cambia de pantalla
     */
    public ConfigurationPanel(NavigationController navigationController) 
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
        prepareViewControls(content);
        prepareViewButtons(content);
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

    
    // ----- Sección de controles -----

    /**
     * Crea la sección "Controles": tabla de teclas de cada jugador.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewControls(JPanel content) 
    {
        content.add(createSectionTitle("CONTROLES"));
        content.add(Box.createVerticalStrut(TITLE_GAP));
        prepareViewKeyGrid(content);
        content.add(Box.createVerticalStrut(SECTION_GAP));
    }

    /**
     * Crea la tabla de teclas: una fila por acción y una columna por jugador.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewKeyGrid(JPanel content) 
    {
        actionLabels = createActionLabels();
        player1KeyButtons = createKeyButtons(PLAYER1_DEFAULT_KEYS);
        player2KeyButtons = createKeyButtons(PLAYER2_DEFAULT_KEYS);

        JPanel grid = new JPanel(new GridLayout(ACTION_NAMES.length + 1, 3, KEY_GRID_HGAP, KEY_GRID_VGAP));
        grid.setOpaque(false);
        grid.add(new JLabel());
        grid.add(createColumnTitle("Jugador 1"));
        grid.add(createColumnTitle("Jugador 2"));
        for (int i = 0; i < ACTION_NAMES.length; i++) 
        {
            grid.add(actionLabels[i]);
            grid.add(player1KeyButtons[i]);
            grid.add(player2KeyButtons[i]);
        }
        limitHeight(grid);
        content.add(grid);
    }

    /**
     * Crea las etiquetas con el nombre de cada acción.
     *
     * @return etiquetas en el orden de {@code ACTION_NAMES}
     */
    private JLabel[] createActionLabels() 
    {
        JLabel[] labels = new JLabel[ACTION_NAMES.length];
        for (int i = 0; i < ACTION_NAMES.length; i++) 
        {
            labels[i] = new JLabel(ACTION_NAMES[i]);
            labels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            labels[i].setForeground(Theme.DARK_GREEN);
        }
        return labels;
    }

    /**
     * Crea un botón por acción con la tecla indicada como texto.
     *
     * @param keyCodes códigos de tecla (de {@code KeyEvent}) en el orden de las acciones
     * @return botones de teclas de un jugador
     */
    private JButton[] createKeyButtons(int[] keyCodes) 
    {
        JButton[] buttons = new JButton[keyCodes.length];
        for (int i = 0; i < keyCodes.length; i++) 
        {
            buttons[i] = new RoundedButton(KeyEvent.getKeyText(keyCodes[i]),
                    Color.WHITE, Theme.DARK_GREEN, Theme.SAND_BORDER, 13);
            buttons[i].setPreferredSize(new Dimension(100, KEY_BUTTON_HEIGHT));
        }
        return buttons;
    }

    /**
     * Crea el título de una columna de la tabla de teclas.
     *
     * @param text texto del título
     * @return etiqueta centrada
     */
    private JLabel createColumnTitle(String text) 
    {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Theme.DARK_GREEN);
        return label;
    }


    /**
     * Crea los botones Guardar y Volver en la parte inferior derecha.
     *
     * @param content contenedor de la pantalla
     */
    private void prepareViewButtons(JPanel content) 
    {
        saveButton = new RoundedButton("Guardar", Theme.GREEN, Color.WHITE, Theme.GREEN, 17);
        backButton = new RoundedButton("Volver", Theme.SAND, Theme.DARK_GREEN, Theme.SAND_BORDER, 17);
        saveButton.setPreferredSize(new Dimension(BOTTOM_BUTTON_WIDTH, BOTTOM_BUTTON_HEIGHT));
        backButton.setPreferredSize(new Dimension(BOTTOM_BUTTON_WIDTH, BOTTOM_BUTTON_HEIGHT));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, KEY_GRID_HGAP, 0));
        row.setOpaque(false);
        row.add(saveButton);
        row.add(backButton);
        limitHeight(row);

        content.add(Box.createVerticalGlue());
        content.add(row);
    }

    /**
     * Crea el título de una sección.
     *
     * @param text texto del título
     * @return etiqueta alineada a la izquierda
     */
    private JLabel createSectionTitle(String text) 
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(Theme.DARK_GREEN);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
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
        prepareControllerKeyButtons(player1KeyButtons);
        prepareControllerKeyButtons(player2KeyButtons);
        prepareControllerSave();
        prepareControllerBack();
    }

    /**
     * Asocia cada botón de tecla con el cambio de tecla.
     *
     * @param buttons botones de teclas de un jugador
     */
    private void prepareControllerKeyButtons(JButton[] buttons) 
    {
        for (JButton button : buttons) 
        {
            prepareControllerKeyButton(button);
        }
    }

    /**
     * Permite cambiar la tecla de un botón: al hacer clic espera una tecla,
     * la tecla Escape o salir del botón cancelan el cambio.
     *
     * @param button botón de tecla
     */
    private void prepareControllerKeyButton(JButton button) 
    {
        button.addActionListener(e -> startKeyCapture(button));
        button.addKeyListener(new KeyAdapter() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                captureKey(button, e);
            }
        });
        button.addFocusListener(new FocusAdapter() 
        {
            @Override
            public void focusLost(FocusEvent e) 
            {
                cancelKeyCapture(button);
            }
        });
    }

    /**
     * Asocia el botón Guardar con el regreso al menú principal.
     */
    private void prepareControllerSave() 
    {
        // Pendiente: decidir dónde se guardan las teclas.
        saveButton.addActionListener(e -> 
            navigationController.showScreen(NavigationController.MAIN_MENU));
    }

    /**
     * Asocia el botón Volver con el regreso al menú principal.
     */
    private void prepareControllerBack() 
    {
        backButton.addActionListener(e -> 
            navigationController.showScreen(NavigationController.MAIN_MENU));
    }

   
    /**
     * Pone el botón a esperar la nueva tecla.
     *
     * @param button botón de tecla
     */
    private void startKeyCapture(JButton button) 
    {
        if (isWaitingForKey(button)) 
        {
            return;
        }
        button.putClientProperty(PREVIOUS_TEXT_PROPERTY, button.getText());
        button.putClientProperty(WAITING_PROPERTY, Boolean.TRUE);
        button.setText(WAITING_TEXT);
        button.requestFocusInWindow();
    }

    /**
     * Procesa la tecla presionada mientras el botón espera una nueva tecla.
     *
     * @param button botón de tecla
     * @param event tecla presionada
     */
    private void captureKey(JButton button, KeyEvent event) 
    {
        if (!isWaitingForKey(button)) 
        {
            return;
        }
        event.consume();
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) 
        {
            cancelKeyCapture(button);
            return;
        }
        assignKey(button, KeyEvent.getKeyText(event.getKeyCode()));
    }

    /**
     * Asigna la tecla al botón, salvo que ya la use otra acción.
     *
     * @param button botón de tecla
     * @param keyText nombre de la tecla
     */
    private void assignKey(JButton button, String keyText) 
    {
        String owner = findKeyOwner(button, keyText);
        if (owner != null) 
        {
            cancelKeyCapture(button);
            JOptionPane.showMessageDialog(this,
                    "La tecla " + keyText + " ya está asignada a: " + owner + ".",
                    "Tecla repetida", JOptionPane.ERROR_MESSAGE);
            return;
        }
        button.setText(keyText);
        button.putClientProperty(WAITING_PROPERTY, Boolean.FALSE);
    }

    /**
     * Cancela el cambio de tecla y restaura el texto anterior.
     *
     * @param button botón de tecla
     */
    private void cancelKeyCapture(JButton button) 
    {
        if (!isWaitingForKey(button)) 
        {
            return;
        }
        button.setText((String) button.getClientProperty(PREVIOUS_TEXT_PROPERTY));
        button.putClientProperty(WAITING_PROPERTY, Boolean.FALSE);
    }

    /**
     * Indica si el botón está esperando una nueva tecla.
     *
     * @param button botón de tecla
     * @return {@code true} si espera una tecla
     */
    private boolean isWaitingForKey(JButton button) 
    {
        return Boolean.TRUE.equals(button.getClientProperty(WAITING_PROPERTY));
    }

    /**
     * Busca qué acción ya usa la tecla indicada, en cualquiera de los dos jugadores
     * (comparten el mismo teclado).
     *
     * @param current botón que se está cambiando
     * @param keyText nombre de la tecla
     * @return descripción de la acción que la usa, o {@code null} si está libre
     */
    private String findKeyOwner(JButton current, String keyText) 
    {
        String owner = findKeyOwner(player1KeyButtons, "Jugador 1", current, keyText);
        if (owner == null) 
        {
            owner = findKeyOwner(player2KeyButtons, "Jugador 2", current, keyText);
        }
        return owner;
    }

    /**
     * Busca la tecla entre los botones de un jugador.
     *
     * @param buttons botones del jugador
     * @param playerName nombre del jugador
     * @param current botón que se está cambiando (se ignora)
     * @param keyText nombre de la tecla
     * @return descripción de la acción que la usa, o {@code null} si no la usa
     */
    private String findKeyOwner(JButton[] buttons, String playerName, JButton current, String keyText) 
    {
        for (int i = 0; i < buttons.length; i++) 
        {
            if (buttons[i] != current && buttons[i].getText().equals(keyText)) 
            {
                return playerName + " (" + ACTION_NAMES[i] + ")";
            }
        }
        return null;
    }
}
