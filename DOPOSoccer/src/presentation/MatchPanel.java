package 

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Pantalla del partido: cancha, marcador, tiempo y estado de cada jugador.
 */
public class MatchPanel extends BasePanel 
{
    private JLabel scoreLabel;
    private JLabel timeLabel;
    private JButton pauseButton;
    private JProgressBar player1LifeBar;
    private JProgressBar player1PowerBar;
    private JProgressBar player2LifeBar;
    private JProgressBar player2PowerBar;

    /**
     * Crea el panel del partido.
     */
    public MatchPanel() 
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
