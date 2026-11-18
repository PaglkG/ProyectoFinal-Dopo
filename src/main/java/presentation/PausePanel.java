package presentation;

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

    /** Prepara el marcador y las acciones de pausa; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareView() 
    {
    }

    /** Conecta continuar, reiniciar y volver al menú; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareController() 
    {
    }
}
