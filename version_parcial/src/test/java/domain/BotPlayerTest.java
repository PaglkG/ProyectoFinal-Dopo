package domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba el flujo común y su delegación en la política. */
public class BotPlayerTest {
    @Test
    void clumsyKeepsItsRandomActionForTheWholeDecisionInterval() {
        BotPlayer bot = new Clumsy();
        SoccerField field = new SoccerField();
        Set<String> first = bot.decideAction(field);

        assertTrue(Set.of(Set.of(), Set.of("moveLeft"), Set.of("moveRight")).contains(first));
        for (int step = 1; step < 18; step++) {
            assertEquals(first, bot.decideAction(field));
        }
    }

    @Test
    void templateUsesTheActionChosenByItsStrategy() {
        TestBot bot = new TestBot();

        assertEquals(Set.of("moveRight"), bot.decideAction(new SoccerField()));
    }

    @Test
    void stunnedBotDoesNotProposeAnAction() {
        TestBot bot = new TestBot();
        bot.takeDamage(bot.getLife());

        assertTrue(bot.decideAction(new SoccerField()).isEmpty());
    }

    private static final class TestBot extends BotPlayer {
        @Override
        public BotDecisionStrategy createDecisionStrategy() {
            return context -> "moveRight";
        }
    }
}
