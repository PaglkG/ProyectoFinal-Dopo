package presentation;

import java.util.List;
import java.util.Objects;

/**
 * Configuración inmutable reunida por {@code PersonalizationPanel.createMatchConfiguration()}.
 * La ventana usa sus parámetros al iniciar la sesión y la incluye en
 * {@link MatchSnapshot}. Preparación, cancha y resultados usan sus datos de
 * países, personajes y modalidad al refrescarse.
 */
public final class MatchConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /** Parámetros ordenados de la regla del partido. */
    private final List<String> parametersRules;
    /** Poderes elegidos y perfil de bot, cuando aplica. */
    private final List<String> parametersPowers;
    /** Nombre de la clase de sesión. */
    private final String typeGame;
    /** Nombre de la clase de regla del partido. */
    private final String typeMatch;
    /** Identidad humana seleccionada. */
    private final boolean playerOne;
    /** Indica si el oponente es un bot. */
    private final boolean againstBot;
    /** País elegido para el primer participante. */
    private final String playerOneCountryCode;
    /** País elegido para el segundo participante. */
    private final String playerTwoCountryCode;
    /** Recurso de cabeza elegido para el primer participante. */
    private final String playerOneHeadImage;
    /** Recurso de cabeza elegido para el segundo participante. */
    private final String playerTwoHeadImage;

    /** Crea una configuración y copia sus listas para mantenerlas inmutables.
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
    public MatchConfiguration(List<String> parametersRules, List<String> parametersPowers,
            String typeGame, String typeMatch, boolean playerOne, boolean againstBot,
            String playerOneCountryCode, String playerTwoCountryCode,
            String playerOneHeadImage, String playerTwoHeadImage) {
        this.parametersRules = List.copyOf(parametersRules);
        this.parametersPowers = List.copyOf(parametersPowers);
        this.typeGame = typeGame;
        this.typeMatch = typeMatch;
        this.playerOne = playerOne;
        this.againstBot = againstBot;
        this.playerOneCountryCode = playerOneCountryCode;
        this.playerTwoCountryCode = playerTwoCountryCode;
        this.playerOneHeadImage = playerOneHeadImage;
        this.playerTwoHeadImage = playerTwoHeadImage;
    }

    /** Devuelve los parámetros ordenados de la regla del partido.
     * @return lista inmutable de parámetros
     */
    public List<String> parametersRules() { return parametersRules; }
    /** Devuelve los nombres de clase de poderes y perfil del bot.
     * @return lista inmutable de parámetros de poderes
     */
    public List<String> parametersPowers() { return parametersPowers; }
    /** Devuelve el nombre simple de la modalidad de sesión.
     * @return nombre de clase
     */
    public String typeGame() { return typeGame; }
    /** Devuelve el nombre simple de la modalidad de partido.
     * @return nombre de clase
     */
    public String typeMatch() { return typeMatch; }
    /** Indica la identidad humana elegida para el primer jugador.
     * @return {@code true} para jugador uno
     */
    public boolean playerOne() { return playerOne; }
    /** Indica si la partida enfrenta al jugador con un bot.
     * @return {@code true} si juega contra un bot
     */
    public boolean againstBot() { return againstBot; }
    /** Devuelve el código de país del primer participante.
     * @return código de país
     */
    public String playerOneCountryCode() { return playerOneCountryCode; }
    /** Devuelve el código de país del segundo participante.
     * @return código de país
     */
    public String playerTwoCountryCode() { return playerTwoCountryCode; }
    /** Devuelve el recurso de cabeza del primer participante.
     * @return ruta del recurso
     */
    public String playerOneHeadImage() { return playerOneHeadImage; }
    /** Devuelve el recurso de cabeza del segundo participante.
     * @return ruta del recurso
     */
    public String playerTwoHeadImage() { return playerTwoHeadImage; }

    /** Devuelve el país del personaje que ocupa el lado solicitado.
     * @param right indica el lado derecho
     * @return código del país
     */
    String countryCodeForSide(boolean right) {
        return right != againstBot ? playerOneCountryCode : playerTwoCountryCode;
    }

    /** Devuelve el recurso de cabeza del personaje que ocupa el lado solicitado.
     * @param right indica el lado derecho
     * @return nombre del recurso de imagen
     */
    String headImageForSide(boolean right) {
        return right != againstBot ? playerOneHeadImage : playerTwoHeadImage;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MatchConfiguration that)) return false;
        return playerOne == that.playerOne && againstBot == that.againstBot
                && Objects.equals(parametersRules, that.parametersRules)
                && Objects.equals(parametersPowers, that.parametersPowers)
                && Objects.equals(typeGame, that.typeGame)
                && Objects.equals(typeMatch, that.typeMatch)
                && Objects.equals(playerOneCountryCode, that.playerOneCountryCode)
                && Objects.equals(playerTwoCountryCode, that.playerTwoCountryCode)
                && Objects.equals(playerOneHeadImage, that.playerOneHeadImage)
                && Objects.equals(playerTwoHeadImage, that.playerTwoHeadImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parametersRules, parametersPowers, typeGame, typeMatch, playerOne,
                againstBot, playerOneCountryCode, playerTwoCountryCode,
                playerOneHeadImage, playerTwoHeadImage);
    }

    @Override
    public String toString() {
        return "MatchConfiguration[parametersRules=" + parametersRules
                + ", parametersPowers=" + parametersPowers + ", typeGame=" + typeGame
                + ", typeMatch=" + typeMatch + ", playerOne=" + playerOne
                + ", againstBot=" + againstBot + ", playerOneCountryCode=" + playerOneCountryCode
                + ", playerTwoCountryCode=" + playerTwoCountryCode + ", playerOneHeadImage="
                + playerOneHeadImage + ", playerTwoHeadImage=" + playerTwoHeadImage + "]";
    }
}
