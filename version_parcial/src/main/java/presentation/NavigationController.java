package presentation;
 
/**
 * Controla la navegación entre las pantallas de la ventana principal.
 * Recibe las solicitudes de los botones en {@link #showScreen(String)} y la
 * configuración elegida en {@link #startMatch(MatchConfiguration)}.
 * Delega ambas solicitudes a {@link DOPOSoccerGUI}, que realiza el cambio de
 * pantalla y el refresco correspondiente.
 *
 * <p>Los botones llaman a {@code showScreen(...)} desde su {@code ActionListener}.
 * La constante del destino debe coincidir con la clave registrada en
 * {@code DOPOSoccerGUI.prepareViewScreens()}. La ventana usa esa clave en
 * {@code CardLayout.show(...)} para mostrar el panel seleccionado.</p>
 *
 * <p><strong>Para agregar un destino:</strong> declara su constante aquí,
 * crea y registra el panel en {@code DOPOSoccerGUI.prepareViewScreens()}, y
 * llama a {@link #showScreen(String)} desde el listener del botón que lo abre.</p>
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
    /** Identificador de la pantalla de resultados. */
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
        // La ventana coordina el cambio de pantalla con el refresco y el temporizador.
        doposoccerGUI.showScreen(screen);
    }

    /** Inicia la sesión configurada mediante la fachada y prepara las pantallas del partido.
     * @param configuration configuración seleccionada para la partida
     */
    public void startMatch(MatchConfiguration configuration) {
        // La ventana recibe la configuración completa que produjo el formulario.
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
