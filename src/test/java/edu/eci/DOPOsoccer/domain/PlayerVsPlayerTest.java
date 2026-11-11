package edu.eci.DOPOSoccer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.rules.ByGoals;

/** Pruebas de la composición de participantes de una sesión entre dos jugadores. */
class PlayerVsPlayerTest {

    @Test
    void constructorCreatesOneHumanPlayerOnEachSide() throws Exception {
        PlayerVsPlayer session = new PlayerVsPlayer(true, ByGoals.class.getName(), List.of("5"));

        assertEquals(1, session.participantsRight.size());
        assertEquals(1, session.participantsLeft.size());
        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(Player.class, session.participantsLeft.get(0));
    }

    @Test
    void resetConfigurationResetsPlayerOneWhenSelected() throws Exception {
        PlayerVsPlayer session = new PlayerVsPlayer(true, ByGoals.class.getName(), List.of("5"));
        ConfigurationTrackingPlayer player1 = new ConfigurationTrackingPlayer(1);
        ConfigurationTrackingPlayer player2 = new ConfigurationTrackingPlayer(2);
        session.participantsRight.set(0, player1);
        session.participantsLeft.set(0, player2);

        session.resetConfiguration(true);

        assertEquals(1, player1.resetCount);
        assertEquals(0, player2.resetCount);
    }

    @Test
    void resetConfigurationResetsPlayerTwoWhenSelected() throws Exception {
        PlayerVsPlayer session = new PlayerVsPlayer(true, ByGoals.class.getName(), List.of("5"));
        ConfigurationTrackingPlayer player1 = new ConfigurationTrackingPlayer(1);
        ConfigurationTrackingPlayer player2 = new ConfigurationTrackingPlayer(2);
        session.participantsRight.set(0, player1);
        session.participantsLeft.set(0, player2);

        session.resetConfiguration(false);

        assertEquals(0, player1.resetCount);
        assertEquals(1, player2.resetCount);
    }

    private static class ConfigurationTrackingPlayer extends Player {
        private int resetCount;

        ConfigurationTrackingPlayer(int user) {
            super(user);
        }

        @Override
        public void resetConfiguration() {
            resetCount++;
        }
    }
}
