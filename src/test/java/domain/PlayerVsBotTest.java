package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import domain.rules.ByGoals;
import domain.powers.AuraAzul;
import domain.powers.Rayo;
import domain.Clumsy;
import domain.Expert;
import domain.Defensive;
import domain.Offensive;
import domain.exception.GameException;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de la composición de participantes de una sesión jugador contra bot. */
class PlayerVsBotTest {

    @Test
    void constructorCreatesOneHumanAndOneBot() throws Exception {
        PlayerVsBot session = new PlayerVsBot(true, ByGoals.class.getName(), List.of("5"),
                List.of("domain.powers.AuraAzul", "domain.powers.Rayo", "Clumsy"));

        assertEquals(1, session.participantsRight.size());
        assertEquals(1, session.participantsLeft.size());
        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsLeft.get(0));
        assertInstanceOf(AuraAzul.class, session.participantsRight.get(0).getSpecialPower());
        assertInstanceOf(Rayo.class, session.participantsLeft.get(0).getSpecialPower());
        assertInstanceOf(Clumsy.class, session.participantsLeft.get(0));
    }

    @Test
    void constructorCreatesSameParticipantKindsForEitherPlayerOption() throws Exception {
        PlayerVsBot session = new PlayerVsBot(false, ByGoals.class.getName(), List.of("5"),
                List.of("domain.powers.AuraAzul", "domain.powers.Rayo", "Expert"));

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
        PlayerVsBot session = new PlayerVsBot(true, ByGoals.class.getName(), List.of("1"),
                List.of("domain.powers.AuraAzul", "domain.powers.Rayo", "Clumsy"));

        session.goal("right");

        assertEquals(5, session.participantsRight.get(0).getStats().getPoints());
        assertEquals(0, session.participantsLeft.get(0).getStats().getPoints());
    }

    @Test
    void botWinDoesNotAwardPointsToEitherBotOrLosingHuman() throws Exception {
        PlayerVsBot session = new PlayerVsBot(true, ByGoals.class.getName(), List.of("1"),
                List.of("domain.powers.AuraAzul", "domain.powers.Rayo", "Clumsy"));

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

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    private static PlayerVsBot createSession(String difficulty) throws GameException {
        return new PlayerVsBot(true, ByGoals.class.getName(), List.of("5"),
                List.of("domain.powers.AuraAzul", "domain.powers.Rayo", difficulty));
    }
}
