package presentation.samuel;
 
import javax.swing.JButton;
import javax.swing.JLabel;
 
/**
 * Pantalla del menú principal: jugar, abrir una partida, configuración y salir.
 */
public class MainMenuPanel extends BasePanel 
{
    private JLabel titleLabel;
    private JButton playButton;
    private JButton openMatchButton;
    private JButton configurationButton;
    private JButton exitButton;
 
    /**
     * Crea el panel del menú principal.
     */
    public MainMenuPanel() 
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
