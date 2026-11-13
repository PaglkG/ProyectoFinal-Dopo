package presentation.samuel;
 
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
 
    @Override
    public void prepareView() 
    {
    }
 
    @Override
    public void prepareController() 
    {
    }
}
