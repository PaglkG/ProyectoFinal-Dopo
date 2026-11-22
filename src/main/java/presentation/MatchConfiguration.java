package presentation;

import java.util.List;

/** Configuración inmutable que la presentación entrega a la fachada {@code Game}.
 * @param parametersRules parámetros ordenados de la regla de partido
 * @param parametersPowers nombres de clase de poderes y perfil del bot
 * @param typeGame nombre de clase simple del tipo de sesión
 * @param typeMatch nombre de clase simple del tipo de partido
 * @param playerOne identidad humana seleccionada para el primer jugador
 * @param againstBot indica si la modalidad enfrenta al jugador con un bot
 * @param playerOneCountryCode código de país del primer participante
 * @param playerTwoCountryCode código de país del segundo participante
 * @param playerOneHeadImage recurso de imagen de cabeza del primer participante
 * @param playerTwoHeadImage recurso de imagen de cabeza del segundo participante
 */
public record MatchConfiguration(
        List<String> parametersRules,
        List<String> parametersPowers,
        String typeGame,
        String typeMatch,
        boolean playerOne,
        boolean againstBot,
        String playerOneCountryCode,
        String playerTwoCountryCode,
        String playerOneHeadImage,
        String playerTwoHeadImage) {

    /** Copia las listas de parámetros para mantenerlas inmutables.
     * @param parametersRules parámetros ordenados de la regla
     * @param parametersPowers poderes y perfil, según la modalidad
     * @param typeGame nombre de la clase del tipo de sesión
     * @param typeMatch nombre de la clase de regla del partido
     * @param playerOne identidad humana seleccionada
     * @param againstBot indica si se enfrentará a un bot
     * @param playerOneCountryCode país del primer participante
     * @param playerTwoCountryCode país del segundo participante
     * @param playerOneHeadImage imagen de cabeza del primer participante
     * @param playerTwoHeadImage imagen de cabeza del segundo participante
     */
    public MatchConfiguration {
        parametersRules = List.copyOf(parametersRules);
        parametersPowers = List.copyOf(parametersPowers);
    }
}
