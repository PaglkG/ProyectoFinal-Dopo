package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import domain.GameException;

class StatusEffectTest {

    @Test
    void stunBlocksMovementUntilThreeSecondsHaveElapsed() {
        StatusEffect effect = new StatusEffect(StatusEffect.STUNNED, StatusEffect.STUN_DURATION_SECONDS);

        assertTrue(effect.blocksMovement());
        assertEquals(0, effect.getSpeedFactor());
        assertEquals(0, effect.getJumpFactor());

        for (int tick = 0; tick < 180; tick++) effect.update();

        assertTrue(effect.isExpired());
        assertFalse(effect.blocksMovement());
        assertEquals(1, effect.getSpeedFactor());
        assertEquals(1, effect.getJumpFactor());
    }

    @Test
    void refreshRestoresTheConfiguredDuration() {
        StatusEffect effect = new StatusEffect(StatusEffect.STUNNED, StatusEffect.STUN_DURATION_SECONDS);
        for (int tick = 0; tick < 30; tick++) effect.update();

        effect.refresh();

        assertEquals(StatusEffect.STUN_DURATION_SECONDS, effect.getRemaining());
        assertTrue(effect.blocksMovement());
    }

    @Test
    void invalidDurationUsesTheProjectException() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new StatusEffect(StatusEffect.STUNNED, 0));

        assertEquals(GameException.INVALID_STATUS_EFFECT, exception.getMessage());
    }
}
