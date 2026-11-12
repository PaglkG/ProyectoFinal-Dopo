package

import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * Pantalla de pausa que se muestra sobre el partido.
 */
public class PausePanel extends BasePanel 
{
    private JLabel scoreLabel;
    private JLabel titleLabel;
    private JButton resumeButton;
    private JButton restartButton;
    private JButton mainMenuButton;

    /**
     * Crea el panel de pausa.
     */
    public PausePanel() 
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
