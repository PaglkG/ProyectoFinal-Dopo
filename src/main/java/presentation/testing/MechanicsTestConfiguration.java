package presentation.testing;

import java.util.List;
import java.util.Objects;

/**
 * Configuración completa usada para crear una sesión del banco de pruebas.
 * @param sessionMode modo de participantes
 * @param matchMode modalidad visible de la partida
 * @param limit límite del partido
 * @param sessionClass nombre completo de la clase de sesión
 * @param matchClass nombre completo de la clase de regla
 * @param ruleParameters parámetros que recibe la regla
 * @param rightPowerClass clase del poder del participante derecho
 * @param leftPowerClass clase del poder del participante izquierdo o bot
 * @param botProfile perfil requerido por una sesión jugador contra bot
 * @param playerOne identidad humana seleccionada cuando la sesión la utiliza
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

    public MechanicsTestController.SessionMode sessionMode() { return sessionMode; }
    public MechanicsTestController.MatchMode matchMode() { return matchMode; }
    public int limit() { return limit; }
    public String sessionClass() { return sessionClass; }
    public String matchClass() { return matchClass; }
    public List<String> ruleParameters() { return ruleParameters; }
    public String rightPowerClass() { return rightPowerClass; }
    public String leftPowerClass() { return leftPowerClass; }
    public String botProfile() { return botProfile; }
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

    /** Conserva los parámetros de regla como una lista inmutable. */
    /** Crea la configuración inicial con las clases estándar del proyecto. */
    public static MechanicsTestConfiguration defaults() {
        return new MechanicsTestConfiguration(MechanicsTestController.SessionMode.PLAYER_VS_PLAYER,
                MechanicsTestController.MatchMode.GOALS, 5, "domain.PlayerVsPlayer", "domain.ByGoals",
                List.of("150", "450", "5"), "domain.BlueAura", "domain.Lightning", "Clumsy", true);
    }
}
