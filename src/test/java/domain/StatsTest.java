package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Pruebas de los valores iniciales de las estadísticas. */
class StatsTest {

    @Test
    void startsAllUpgradeableStatisticsAtOneAndPointsAtZero() throws ReflectiveOperationException {
        Stats stats = new Stats();

        @SuppressWarnings("unchecked")
        Map<String, Integer> action = (Map<String, Integer>) readField(stats, "action");

        assertEquals(Map.of("speed", 1, "goal", 1, "kick", 1, "jump", 1), action);
        assertEquals(0, readField(stats, "points"));
    }

    private static Object readField(Stats stats, String fieldName) throws ReflectiveOperationException {
        Field field = Stats.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(stats);
    }
}
