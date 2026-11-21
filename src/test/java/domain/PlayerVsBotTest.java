package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import domain.ByGoals;
import domain.BlueAura;
import domain.Lightning;
import domain.Clumsy;
import domain.Expert;
import domain.Defensive;
import domain.Offensive;
import domain.GameException;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de la composición de participantes de una sesión jugador contra bot. */
class PlayerVsBotTest {

    @BeforeEach
    void resetSingleton() throws ReflectiveOperationException {
        GameSessionTestSupport.resetSingleton();
    }

    @Test
    void constructorCreatesOneHumanAndOneBot() throws Exception {
        PlayerVsBot session = createSession(true, "Clumsy", "1");


        assertEquals(1, session.participantsRight.size());
        assertEquals(1, session.participantsLeft.size());
        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsLeft.get(0));
        assertInstanceOf(BlueAura.class, session.participantsRight.get(0).getSpecialPower());
        assertInstanceOf(Lightning.class, session.participantsLeft.get(0).getSpecialPower());
        assertInstanceOf(Clumsy.class, session.participantsLeft.get(0));
    }

    @Test
    void constructorCreatesSameParticipantKindsForEitherPlayerOption() throws Exception {
        PlayerVsBot session = createSession(false, "Expert");

        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsLeft.get(0));
        assertInstanceOf(Expert.class, session.participantsLeft.get(0));
    }

    @Test
    void constructorCreatesOffensiveBotFromTheSelectedDifficulty() throws Exception {
        PlayerVsBot session = createSession("Offensive");

        assertInstanceOf(Offensive.class, session.participantsLeft.get(0));
    }

    @Test
    void humanReceivesFivePointsWhenWinningAgainstBotAndBotReceivesNone() throws Exception {
        PlayerVsBot session = createSession(true, "Clumsy", "1");

        session.goal("right");

        assertEquals(5, session.participantsRight.get(0).getStats().getPoints());
        assertEquals(0, session.participantsLeft.get(0).getStats().getPoints());
    }

    @Test
    void botWinDoesNotAwardPointsToEitherBotOrLosingHuman() throws Exception {
        PlayerVsBot session = createSession(true, "Clumsy");

        session.goal("left");

        assertEquals(0, session.participantsRight.get(0).getStats().getPoints());
        assertEquals(0, session.participantsLeft.get(0).getStats().getPoints());
    }

    @Test
    void constructorCreatesDefensiveBotFromTheSelectedDifficulty() throws Exception {
        PlayerVsBot session = createSession("Defensive");

        assertInstanceOf(Defensive.class, session.participantsLeft.get(0));
    }

    @Test
    void constructorRejectsAnUnknownBotDifficulty() {
        GameException exception = assertThrows(GameException.class, () -> createSession("Unknown"));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
    }

    private static PlayerVsBot createSession(String difficulty) throws GameException {
        return createSession(true, difficulty);
    }

    private static PlayerVsBot createSession(boolean player, String difficulty) throws GameException {
        return createSession(player, difficulty, "5");
    }

    private static PlayerVsBot createSession(boolean player, String difficulty, String limit) throws GameException {
        return GameSessionTestSupport.create(PlayerVsBot.class, player, ByGoals.class.getName(),
                List.of("150", "450", limit), List.of("domain.BlueAura", "domain.Lightning", difficulty));
    }
}
