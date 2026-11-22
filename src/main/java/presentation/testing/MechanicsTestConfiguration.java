package presentation.testing;

import java.util.List;
import java.util.Objects;

/**
 * Configuración completa usada para crear una sesión del banco de pruebas.
 */
public final class MechanicsTestConfiguration {

    private final MechanicsTestController.SessionMode sessionMode;
    private final MechanicsTestController.MatchMode matchMode;
    private final int limit;
    private final String sessionClass;
    private final String matchClass;
    private final List<String> ruleParameters;
    private final String rightPowerClass;
    private final String leftPowerClass;
    private final String botProfile;
    private final boolean playerOne;

    /** Crea la configuración completa del banco y copia sus parámetros de regla.
     * @param sessionMode modalidad de participantes
     * @param matchMode modalidad de partido
     * @param limit límite de goles o minutos según la modalidad
     * @param sessionClass nombre completo de la clase de sesión
     * @param matchClass nombre completo de la clase de regla
     * @param ruleParameters parámetros entregados a la regla
     * @param rightPowerClass clase del poder del participante derecho
     * @param leftPowerClass clase del poder del participante izquierdo o bot
     * @param botProfile perfil del bot cuando se juega contra la máquina
     * @param playerOne identidad humana elegida cuando aplica
     */
    public MechanicsTestConfiguration(MechanicsTestController.SessionMode sessionMode,
            MechanicsTestController.MatchMode matchMode, int limit, String sessionClass, String matchClass,
            List<String> ruleParameters, String rightPowerClass, String leftPowerClass,
            String botProfile, boolean playerOne) {
        this.sessionMode = sessionMode;
        this.matchMode = matchMode;
        this.limit = limit;
        this.sessionClass = sessionClass;
        this.matchClass = matchClass;
        this.ruleParameters = List.copyOf(ruleParameters);
        this.rightPowerClass = rightPowerClass;
        this.leftPowerClass = leftPowerClass;
        this.botProfile = botProfile;
        this.playerOne = playerOne;
    }

    /** Devuelve la modalidad de participantes seleccionada.
     * @return modalidad de sesión
     */
    public MechanicsTestController.SessionMode sessionMode() { return sessionMode; }
    /** Devuelve la modalidad de partido seleccionada.
     * @return modalidad de partido
     */
    public MechanicsTestController.MatchMode matchMode() { return matchMode; }
    /** Devuelve el límite de goles o minutos.
     * @return límite configurado
     */
    public int limit() { return limit; }
    /** Devuelve el nombre completo de la clase de sesión.
     * @return nombre de clase
     */
    public String sessionClass() { return sessionClass; }
    /** Devuelve el nombre completo de la clase de regla de partido.
     * @return nombre de clase
     */
    public String matchClass() { return matchClass; }
    /** Devuelve una copia inmutable de los parámetros de regla.
     * @return parámetros en el orden requerido por la regla
     */
    public List<String> ruleParameters() { return ruleParameters; }
    /** Devuelve el nombre de clase del poder derecho.
     * @return nombre completo de clase
     */
    public String rightPowerClass() { return rightPowerClass; }
    /** Devuelve el nombre de clase del poder izquierdo o del bot.
     * @return nombre completo de clase
     */
    public String leftPowerClass() { return leftPowerClass; }
    /** Devuelve el nombre simple del perfil del bot.
     * @return nombre del perfil
     */
    public String botProfile() { return botProfile; }
    /** Devuelve la identidad del jugador humano seleccionado.
     * @return {@code true} para el jugador 1; {@code false} para el jugador 2
     */
    public boolean playerOne() { return playerOne; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MechanicsTestConfiguration that)) return false;
        return limit == that.limit && playerOne == that.playerOne && sessionMode == that.sessionMode
                && matchMode == that.matchMode && Objects.equals(sessionClass, that.sessionClass)
                && Objects.equals(matchClass, that.matchClass) && Objects.equals(ruleParameters, that.ruleParameters)
                && Objects.equals(rightPowerClass, that.rightPowerClass)
                && Objects.equals(leftPowerClass, that.leftPowerClass) && Objects.equals(botProfile, that.botProfile);
    }

    @Override public int hashCode() {
        return Objects.hash(sessionMode, matchMode, limit, sessionClass, matchClass, ruleParameters,
                rightPowerClass, leftPowerClass, botProfile, playerOne);
    }

    @Override public String toString() {
        return "MechanicsTestConfiguration[sessionMode=" + sessionMode + ", matchMode=" + matchMode
                + ", limit=" + limit + ", sessionClass=" + sessionClass + ", matchClass=" + matchClass
                + ", ruleParameters=" + ruleParameters + ", rightPowerClass=" + rightPowerClass
                + ", leftPowerClass=" + leftPowerClass + ", botProfile=" + botProfile
                + ", playerOne=" + playerOne + "]";
    }

    /** Crea la configuración inicial con las clases estándar del proyecto. */
    /** Crea la configuración inicial con las clases estándar del proyecto.
     * @return configuración predeterminada jugador contra jugador y por goles
     */
    public static MechanicsTestConfiguration defaults() {
        return new MechanicsTestConfiguration(MechanicsTestController.SessionMode.PLAYER_VS_PLAYER,
                MechanicsTestController.MatchMode.GOALS, 5, "domain.PlayerVsPlayer", "domain.ByGoals",
                List.of("150", "450", "5"), "domain.BlueAura", "domain.Lightning", "Clumsy", true);
    }
}
