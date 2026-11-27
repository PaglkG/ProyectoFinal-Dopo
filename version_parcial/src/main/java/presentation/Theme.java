package presentation;

import java.awt.Color;

/**
 * Colores compartidos por todas las pantallas del juego.
 * Para cambiar la paleta basta con editar los valores de esta clase.
 * Los componentes reutilizables {@link RoundedButton} y {@link RoundedPanel}
 * reciben estos colores desde los paneles que construyen cada pantalla.
 */
final class Theme 
{
    static final Color BACKGROUND = new Color(0xFDF8EC);
    static final Color INNER_BACKGROUND = new Color(0xF5EFDF);
    static final Color DARK_GREEN = new Color(0x1F3B36);
    static final Color GREEN = new Color(0x2E8B67);
    static final Color SAND = new Color(0xFFF0D6);
    static final Color SAND_BORDER = new Color(0xF0D9A8);
    static final Color RED = new Color(0xFF6B6B);
    static final Color RED_BORDER = new Color(0xE05252);
    static final Color LABEL_COLOR = new Color(0x5B6B7A);

    /** La paleta se consulta mediante constantes compartidas. */
    private Theme() 
    {
    }
}
