package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import domain.rules.ByGoals;

/** Pruebas de la composición de participantes de una sesión jugador contra bot. */
class PlayerVsBotTest {

    @Test
    void constructorCreatesOneHumanAndOneBot() throws Exception {
        PlayerVsBot session = new PlayerVsBot(true, ByGoals.class.getName(), List.of("5"));

        assertEquals(1, session.participantsRight.size());
        assertEquals(1, session.participantsLeft.size());
        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsLeft.get(0));
    }

    @Test
    void constructorCreatesSameParticipantKindsForEitherPlayerOption() throws Exception {
        PlayerVsBot session = new PlayerVsBot(false, ByGoals.class.getName(), List.of("5"));

        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(BotPlayer.class, session.participantsLeft.get(0));
    }
}
