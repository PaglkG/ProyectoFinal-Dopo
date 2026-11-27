package presentation;
 
import javax.swing.JPanel;
 
/**
 * Clase base abstracta de todos los paneles del juego.
 * Cada pantalla define cómo se construye su vista y cómo se conectan sus eventos.
 * Las subclases enlazan los datos de entrada con sus componentes en
 * {@link #prepareView()} y conectan las acciones en {@link #prepareController()}.
 * Cada pantalla concreta documenta su propio punto de refresco.
 *
 * <p><strong>Para agregar una pantalla:</strong> crea un subtipo de esta base,
 * implementa {@link #prepareView()} y {@link #prepareController()}, y agrega
 * una constante de ruta en {@link NavigationController}. Después, registra el
 * panel y su clave en {@link DOPOSoccerGUI#prepareViewScreens()}.</p>
 */
public abstract class BasePanel extends JPanel 
{
 
    /**
     * Crea el panel base.
     */
    public BasePanel() 
    {
    }
 
    /**
     * Crea y organiza los componentes visuales del panel.
     */
    public abstract void prepareView();
 
    /**
     * Asocia los eventos de los componentes con sus acciones después de crearlos.
     * Los botones que navegan registran un {@code ActionListener} que llama al
     * controlador de navegación con la clave de la pantalla de destino.
     */
    public abstract void prepareController();
}
