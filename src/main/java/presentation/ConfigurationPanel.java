package presentation;
 
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
 
/**
 * Pantalla de configuración: sonido y controles de teclado de cada jugador.
 */
public class ConfigurationPanel extends BasePanel 
{
    private JCheckBox musicCheckBox;
    private JCheckBox effectsCheckBox;
    private JSlider volumeSlider;
    private JLabel[] actionLabels;
    private JButton[] player1KeyButtons;
    private JButton[] player2KeyButtons;
    private JButton saveButton;
    private JButton backButton;
 
    /**
     * Crea el panel de configuración.
     */
    public ConfigurationPanel() 
    {
    }
 
    /** Prepara controles de sonido, volumen y teclas; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareView() 
    {
    }
 
    /** Conecta guardado y navegación; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareController() 
    {
    }
}
