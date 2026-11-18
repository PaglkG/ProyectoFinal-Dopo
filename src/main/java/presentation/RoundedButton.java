package presentation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Botón con esquinas redondeadas y borde de color. Se aclara al pasar el
 * mouse y se oscurece al presionarlo.
 */
class RoundedButton extends JButton 
{
    private static final int ARC = 22;
    private final Color borderColor;

    /**
     * Crea el botón redondeado.
     *
     * @param text texto del botón
     * @param background color de fondo
     * @param foreground color del texto
     * @param borderColor color del borde
     * @param fontSize tamaño de la letra
     */
    RoundedButton(String text, Color background, Color foreground, Color borderColor, int fontSize) 
    {
        super(text);
        this.borderColor = borderColor;
        setBackground(background);
        setForeground(foreground);
        setFont(new Font("SansSerif", Font.BOLD, fontSize));
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = getBackground();
        if (getModel().isPressed()) 
        {
            fill = fill.darker();
        }
        else if (getModel().isRollover()) 
        {
            fill = fill.brighter();
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}