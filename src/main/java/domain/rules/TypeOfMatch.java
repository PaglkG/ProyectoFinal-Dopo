package domain.rules;
import java.util.List;

/**
 * Define el contrato para comprobar si continúa una modalidad de partido.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public abstract class TypeOfMatch {

    /** Crea un tipo de partido. */
    public TypeOfMatch() {
    }

    /**
     * Comprueba las condiciones asociadas a este tipo de partido.
     *
     * @param parameters valores que requiere la modalidad para comprobarla
     * @return resultado de la comprobación
     */
    public abstract boolean check(List<String> parameters);
}
