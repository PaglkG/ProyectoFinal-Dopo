package presentation;
 
/**
 * Controla la navegación entre las pantallas de la ventana principal.
 */
public class NavigationController 
{
    /** Identificador de la pantalla principal. */
    public static final String MAIN_MENU = "MAIN_MENU";
    /** Identificador de la pantalla de configuración. */
    public static final String CONFIGURATION = "CONFIGURATION";
    /** Identificador de la pantalla de personalización. */
    public static final String PERSONALIZATION = "PERSONALIZATION";
    /** Identificador de la tienda de mejoras. */
    public static final String STORE_IMPROVEMENT = "STORE_IMPROVEMENT";
    /** Identificador de la pantalla de preparación. */
    public static final String PREPARATION = "PREPARATION";
    /** Identificador de la pantalla de partido. */
    public static final String MATCH = "MATCH";
    /** Identificador de la pantalla de pausa. */
    public static final String PAUSE = "PAUSE";

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

    /** Inicia la sesión configurada mediante la fachada y prepara las pantallas del partido.
     * @param configuration configuración seleccionada para la partida
     */
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
