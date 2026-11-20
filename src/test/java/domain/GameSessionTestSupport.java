package domain;

import java.lang.reflect.Field;
import java.util.List;

final class GameSessionTestSupport {
    private GameSessionTestSupport() { }

    static void resetSingleton() throws ReflectiveOperationException {
        Field instance = GameSession.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    static <T extends GameSession> T create(Class<T> sessionType, boolean player,
            String matchType, List<String> rules, List<String> powers) throws GameException {
        return sessionType.cast(GameSession.getInstance(
                sessionType.getName(), player, matchType, rules, powers));
    }
}
