package presentation;

import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * Pantalla de la tienda de mejoras: cada jugador gasta sus puntos
 * en subir sus estadísticas.
 */
public class StoreImprovementPanel extends BasePanel 
{
    private JLabel player1PointsLabel;
    private JLabel player2PointsLabel;
    private JLabel[] player1LevelLabels;
    private JLabel[] player1PriceLabels;
    private JButton[] player1UpgradeButtons;
    private JLabel[] player2LevelLabels;
    private JLabel[] player2PriceLabels;
    private JButton[] player2UpgradeButtons;
    private JButton backButton;

    /**
     * Crea el panel de la tienda de mejoras.
     */
    public StoreImprovementPanel()
    {
    }

    /** Prepara los puntos, niveles, precios y botones de mejora; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareView() 
    {
    }

    /** Conecta los botones de mejora y navegación; el cuerpo está pendiente de implementación. */
    @Override
    public void prepareController() 
    {
    }
}
