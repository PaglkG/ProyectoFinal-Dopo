package presentation;
 
/**
 * Controla la navegación entre las pantallas de la ventana principal.
 */
public class NavigationController 
{
    public static final String MAIN_MENU = "MAIN_MENU";
    public static final String CONFIGURATION = "CONFIGURATION";
    public static final String PERSONALIZATION = "PERSONALIZATION";
    public static final String STORE_IMPROVEMENT = "STORE_IMPROVEMENT";
    public static final String PREPARATION = "PREPARATION";
    public static final String MATCH = "MATCH";
    public static final String PAUSE = "PAUSE";
    public static final String RESULTS = "RESULTS";

    private final DOPOSoccerGUI doposoccerGUI;

    /**
     * Crea el controlador de navegación.
     * @param doposoccerGUI ventana principal cuyas pantallas se controlan
     */
    public NavigationController(DOPOSoccerGUI doposoccerGUI) 
    {
        this.doposoccerGUI = doposoccerGUI;
    }

    /**
     * Muestra la pantalla indicada en la ventana principal.
     * @param screen nombre de la pantalla (una de las constantes de esta clase)
     */
    public void showScreen(String screen) 
    {
        doposoccerGUI.showScreen(screen);
    }

    /** Inicia la sesión configurada mediante la fachada y prepara las pantallas del partido. */
    public void startMatch(MatchConfiguration configuration) {
        doposoccerGUI.startMatch(configuration);
    }

    /**
     * Cierra el juego.
     */
    public void exit() 
    {
        doposoccerGUI.dispose();
    }
}
