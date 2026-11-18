package presentation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Panel con esquinas redondeadas. Su color de fondo se dibuja como un
 * rectángulo redondeado en vez de un rectángulo normal.
 */
class RoundedPanel extends JPanel 
{
    private final int arc;

    /**
     * Crea el panel redondeado.
     *
     * @param color color de fondo
     * @param arc diámetro de las esquinas redondeadas
     */
    RoundedPanel(Color color, int arc) 
    {
        this.arc = arc;
        setBackground(color);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
