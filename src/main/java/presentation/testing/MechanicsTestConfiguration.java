package presentation.testing;

import java.util.List;

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
public record MechanicsTestConfiguration(
        MechanicsTestController.SessionMode sessionMode,
        MechanicsTestController.MatchMode matchMode,
        int limit,
        String sessionClass,
        String matchClass,
        List<String> ruleParameters,
        String rightPowerClass,
        String leftPowerClass,
        String botProfile,
        boolean playerOne) {

    /** Conserva los parámetros de regla como una lista inmutable. */
    public MechanicsTestConfiguration {
        ruleParameters = List.copyOf(ruleParameters);
    }

    /** Crea la configuración inicial con las clases estándar del proyecto. */
    public static MechanicsTestConfiguration defaults() {
        return new MechanicsTestConfiguration(MechanicsTestController.SessionMode.PLAYER_VS_PLAYER,
                MechanicsTestController.MatchMode.GOALS, 5, "domain.PlayerVsPlayer", "domain.ByGoals",
                List.of("5"), "domain.BlueAura", "domain.Lightning", "Clumsy", true);
    }
}
