package presentation.navigation;

/**
 * Define el ciclo de vida común de las pantallas para que el administrador pueda
 * mostrarlas, ocultarlas y liberar sus recursos de forma uniforme.
 *
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public interface Screen {

    /** Declara las acciones que se realizarán cuando la pantalla sea mostrada. */
    void onShow();

    /** Declara las acciones que se realizarán cuando la pantalla sea ocultada. */
    void onHide();

    /** Declara la liberación de los recursos asociados a la pantalla. */
    void dispose();
}
