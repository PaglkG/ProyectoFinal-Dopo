package presentation;
 
import javax.swing.JPanel;
 
/**
 * Clase base abstracta de todos los paneles del juego.
 * Cada pantalla define cómo se construye su vista y cómo se conectan sus eventos.
 * Las subclases enlazan los datos de entrada con sus componentes en
 * {@link #prepareView()} y conectan las acciones en {@link #prepareController()}.
 * Cada pantalla concreta documenta su propio punto de refresco.
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
     * Asocia los eventos de los componentes con sus acciones.
     */
    public abstract void prepareController();
}
