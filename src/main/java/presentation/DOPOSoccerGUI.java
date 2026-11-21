package presentation;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import domain.Game;

/**
 * Ventana principal de DOPOSoccer. Contiene todas las pantallas del juego.
 */

public class DOPOSoccerGUI extends JFrame 
{
    private static final String TITLE = "DOPO SOCCER";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private final NavigationController navigationController;
    private final Game game;
    private MatchConfiguration startedConfiguration;
    private MainMenuPanel mainMenuPanel;
    private ConfigurationPanel configurationPanel;
    private PersonalizationPanel personalizationPanel;
    private StoreImprovementPanel storeImprovementPanel;
    private PreparationPanel preparationPanel;
    private MatchPanel matchPanel;
    private PausePanel pausePanel;
 
    /**
     * Crea la ventana principal.
     */
    public DOPOSoccerGUI() 
    {
        super(TITLE);
        game = new Game();
        navigationController = new NavigationController(this);
        prepareView();
        prepareController();
    }

    /**
     * Crea y organiza los componentes visuales de la ventana.
     */
    private void prepareView() 
    {
        prepareViewLayout();
        prepareViewPanels();
        prepareViewScreens();
        prepareViewWindow();
    }

    /**
     * Define la distribución que permite mostrar una pantalla a la vez.
     */
    private void prepareViewLayout() 
    {
        getContentPane().setLayout(new CardLayout());
    }

    /**
     * Crea el panel de cada pantalla del juego.
     */
    private void prepareViewPanels() 
    {
        mainMenuPanel = new MainMenuPanel(navigationController);
        configurationPanel = new ConfigurationPanel(navigationController);
        personalizationPanel = new PersonalizationPanel(navigationController, game);
        storeImprovementPanel = new StoreImprovementPanel(navigationController);
        preparationPanel = new PreparationPanel(navigationController);
        matchPanel = new MatchPanel(game, navigationController);
        pausePanel = new PausePanel(navigationController);
    }

    /**
     * Registra cada panel con el nombre que usa el controlador de navegación.
     */
    private void prepareViewScreens() 
    {
        addScreen(mainMenuPanel, NavigationController.MAIN_MENU);
        addScreen(configurationPanel, NavigationController.CONFIGURATION);
        addScreen(personalizationPanel, NavigationController.PERSONALIZATION);
        addScreen(storeImprovementPanel, NavigationController.STORE_IMPROVEMENT);
        addScreen(preparationPanel, NavigationController.PREPARATION);
        addScreen(matchPanel, NavigationController.MATCH);
        addScreen(pausePanel, NavigationController.PAUSE);
    }

    /**
     * Configura el tamaño, la posición y las opciones de la ventana.
     */
    private void prepareViewWindow() 
    {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Agrega una pantalla a la ventana con el nombre indicado.
     *
     * @param panel panel de la pantalla
     * @param name nombre con el que se mostrará (constante de NavigationController)
     */
    private void addScreen(BasePanel panel, String name) 
    {
        getContentPane().add(panel, name);
    }

    /**
     * Asocia los eventos de la ventana con sus acciones.
     */
    private void prepareController() 
    {
        prepareControllerClose();
    }

    /**
     * Define qué ocurre al cerrar la ventana: termina la aplicación.
     */
    private void prepareControllerClose() 
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Muestra la pantalla indicada.
     *
     * @param screen nombre de la pantalla que se mostrará
     */
    public void showScreen(String screen) 
    {
        CardLayout layout = (CardLayout) getContentPane().getLayout();
        layout.show(getContentPane(), screen);
    }

    /** Crea la sesión desde la configuración de presentación y alista el partido. */
    public void startMatch(MatchConfiguration configuration) {
        if (startedConfiguration != null && !startedConfiguration.equals(configuration)) {
            throw new IllegalStateException("La sesión única ya conserva otra configuración. "
                    + "Reinicia la aplicación para crear una partida con nuevos parámetros.");
        }
        game.startSession(configuration.parametersRules(), configuration.parametersPowers(),
                configuration.typeGame(), configuration.typeMatch(), configuration.playerOne());
        startedConfiguration = configuration;
        preparationPanel.setCountryCodes(configuration.playerTwoCountryCode(),
                configuration.playerOneCountryCode());
        preparationPanel.setReady();
        matchPanel.setCharacterImages(
                "/images/outfits/heads/" + configuration.playerTwoHeadImage(),
                "/images/outfits/heads/" + configuration.playerOneHeadImage());
        matchPanel.setAgainstBot(configuration.againstBot());
        matchPanel.refreshMatchView();
        showScreen(NavigationController.PREPARATION);
    }

    /**
     * Inicia la aplicación.
     */
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> {
            DOPOSoccerGUI gui = new DOPOSoccerGUI();
            gui.setVisible(true);
        });
    }
}
