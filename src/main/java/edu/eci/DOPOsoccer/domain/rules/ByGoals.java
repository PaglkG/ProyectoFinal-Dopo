package edu.eci.DOPOSoccer.domain.rules;
import java.util.ArrayList;
import java.util.List;
import edu.eci.DOPOSoccer.domain.exception.GameException;
/**
 * Tipo de partido que termina al alcanzar un límite de goles.
 * @author Pablo Andres Gualdron Lindo
 * @author Samuel Ahumada Florez
 */
public class ByGoals extends TypeOfMatch {

    private int goals;
    /** Crea una modalidad por goles. */
    public ByGoals(List<String> parametersRules) {
    }

    @Override 
    public boolean check(List<String> parameters) {
        return false;
    }
}
