package 

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Pantalla para personalizar la partida: modalidad, perfil de la máquina,
 * tipo de partido, tamaño de la cancha y personaje de cada jugador.
 */
public class PersonalizationPanel extends BasePanel 
{
    private JComboBox<String> modeComboBox;
    private JComboBox<String> botProfileComboBox;
    private JComboBox<String> matchTypeComboBox;
    private JTextField minutesTextField;
    private JTextField goalsTextField;
    private JComboBox<String> fieldSizeComboBox;
    private JComboBox<String> player1CountryComboBox;
    private JLabel player1PreviewLabel;
    private JTextArea player1DescriptionTextArea;
    private JComboBox<String> player2CountryComboBox;
    private JLabel player2PreviewLabel;
    private JTextArea player2DescriptionTextArea;
    private JButton storeButton;
    private JButton startButton;
    private JButton backToMenuButton;

    /**
     * Crea el panel de personalización de la partida.
     */
    public PersonalizationPanel() 
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
