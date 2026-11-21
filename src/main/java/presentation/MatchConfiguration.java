package presentation;

import java.util.List;

/** Configuración inmutable que la presentación entrega a la fachada Game. */
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

    public MatchConfiguration {
        parametersRules = List.copyOf(parametersRules);
        parametersPowers = List.copyOf(parametersPowers);
    }
}
