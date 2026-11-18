package presentation;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Pantalla de carga que se muestra mientras se prepara el partido.
 */
public class PreparationPanel extends BasePanel 
{
    private JLabel titleLabel;
    private JProgressBar progressBar;
    private JButton cancelButton;

    /**
     * Crea el panel de preparación del partido.
     */
    public PreparationPanel() 
    {
    }

    /** Prepara el título y el indicador de carga; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareView() 
    {
    }

    /** Conecta la acción para cancelar la preparación; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareController() 
    {
    }
}
