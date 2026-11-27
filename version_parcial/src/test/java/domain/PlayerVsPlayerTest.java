package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import domain.ByGoals;
import domain.BlueAura;
import domain.Lightning;

/** Pruebas de la composición de participantes de una sesión entre dos jugadores. */
class PlayerVsPlayerTest {

    @BeforeEach
    void resetSingleton() throws ReflectiveOperationException {
        GameSessionTestSupport.resetSingleton();
    }

    @Test
    void constructorCreatesOneHumanPlayerOnEachSide() throws Exception {
        PlayerVsPlayer session = GameSessionTestSupport.create(PlayerVsPlayer.class, true,
                ByGoals.class.getName(), List.of("150", "450", "5"), List.of("domain.BlueAura", "domain.Lightning"));

        assertEquals(1, session.participantsRight.size());
        assertEquals(1, session.participantsLeft.size());
        assertInstanceOf(Player.class, session.participantsRight.get(0));
        assertInstanceOf(Player.class, session.participantsLeft.get(0));
        assertInstanceOf(BlueAura.class, session.participantsRight.get(0).getSpecialPower());
        assertInstanceOf(Lightning.class, session.participantsLeft.get(0).getSpecialPower());
    }

}
