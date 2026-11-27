package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import domain.GameException;
import domain.ByGoals;
import domain.ByTime;
import domain.TypeOfMatch;

/** Pruebas para las rutas de error implementadas en el constructor de sesión. */
class GameSessionTest {

    @Test
    void constructorReportsAnUnknownMatchTypeClass() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, "missing.match.Type", List.of("150", "450")));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    @Test
    void constructorCreatesByGoalsWithItsListConstructor() {
        assertDoesNotThrow(() -> new TestGameSession(false, ByGoals.class.getName(), List.of("150", "450", "5")));
    }

    @Test
    void constructorReportsTheCustomExceptionWhenNoListConstructorExists() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, MatchTypeWithoutListConstructor.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    @Test
    void constructorReportsTheCustomExceptionWhenTheClassIsNotAMatchType() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, NonMatchType.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_FOUND_TYPE_GAME, exception.getMessage());
    }

    @Test
    void constructorCreatesByGoalsTypeAndInitializesSessionState() throws ReflectiveOperationException {
        GameSession session = new TestGameSession(false, ByGoals.class.getName(), List.of("150", "450", "5"));

        assertEquals(0.0, readDouble(session, "timeLeft"));
        assertEquals(80.0, readStaticDouble(GameSession.class, "GRAVITY"));
        assertNotNull(readField(session, "TypeOfMatch"));
        assertNotNull(readField(session, "field"));
        assertNotNull(readField(session, "participantsLeft"));
        assertNotNull(readField(session, "participantsRight"));
    }

    @Test
    void constructorCreatesByTimeType() throws ReflectiveOperationException {
        GameSession session = new TestGameSession(false, ByTime.class.getName(), List.of("150", "450", "10"));

        assertNotNull(readField(session, "TypeOfMatch"));
        assertEquals(ByTime.class, readField(session, "TypeOfMatch").getClass());
    }

    @Test
    void constructorReportsWhenMatchTypeClassDoesNotExist() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, "missing.match.Type", List.of("150", "450")));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    @Test
    void constructorReportsWhenMatchTypeHasNoListConstructor() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, MatchTypeWithoutListConstructor.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    @Test
    void constructorReportsWhenMatchTypeCannotBeInstantiated() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, AbstractMatchType.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void constructorReportsWhenMatchTypeConstructorIsNotAccessible() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, MatchTypeWithPrivateConstructor.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_CONSTRUCTOR_FOUND, exception.getMessage());
    }

    @Test
    void constructorReportsAnExceptionThrownByMatchTypeConstructor() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, MatchTypeWithFailingConstructor.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        InvocationTargetException invocationException =
                assertInstanceOf(InvocationTargetException.class, exception.getCause());
        assertInstanceOf(IllegalStateException.class, invocationException.getCause());
    }

    @Test
    void constructorReportsInvalidGoalParameters() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, ByGoals.class.getName(), List.of("150", "450", "-1")));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        InvocationTargetException invocationException =
                assertInstanceOf(InvocationTargetException.class, exception.getCause());
        GameException cause = assertInstanceOf(GameException.class, invocationException.getCause());
        assertEquals(GameException.GOALS_NOT_NEGATIVE, cause.getMessage());
    }

    @Test
    void constructorReportsInvalidTimeParameters() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, ByTime.class.getName(), List.of("150", "450", "-1")));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
        InvocationTargetException invocationException =
                assertInstanceOf(InvocationTargetException.class, exception.getCause());
        GameException cause = assertInstanceOf(GameException.class, invocationException.getCause());
        assertEquals(GameException.MINUTES_NOT_NEGATIVE, cause.getMessage());
    }

    @Test
    void constructorReportsMissingRuleParameters() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, ByTime.class.getName(), List.of("150", "450")));

        assertEquals(GameException.NO_INSTANTIATE_CLASS, exception.getMessage());
    }

    @Test
    void constructorReportsTheCustomSessionNotFoundExceptionForAnEmptyTypeName() {
        GameException exception = assertThrows(
                GameException.class,
                () -> new TestGameSession(false, "", List.of("150", "450", "5")));

        assertEquals(GameException.NO_FOUND_SESSION, exception.getMessage());
    }

    private static class TestGameSession extends GameSession {
        TestGameSession(boolean player, String matchType, List<String> parameters) throws GameException {
            super(player, matchType, parameters);
        }

    }

    private static Object readField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static double readDouble(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = GameSession.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(target);
    }

    private static double readStaticDouble(Class<?> target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(null);
    }

    /** Tipo de partido abstracto para comprobar el error de instanciación. */
    public abstract static class AbstractMatchType extends TypeOfMatch {

        public AbstractMatchType(List<String> parameters) {
        }
    }

    /** Clase con constructor de lista que no representa una modalidad de partido. */
    public static class NonMatchType {

        public NonMatchType(List<String> parameters) {
        }
    }

    /** Modalidad válida cuyo constructor no recibe la lista solicitada por la fábrica. */
    public static class MatchTypeWithoutListConstructor extends TypeOfMatch {

        public MatchTypeWithoutListConstructor() {
        }

        @Override
        public boolean check(List<String> parameters) {
            return false;
        }
    }

    /** Tipo de partido con constructor privado para comprobar el acceso. */
    public static class MatchTypeWithPrivateConstructor extends TypeOfMatch {

        private MatchTypeWithPrivateConstructor(List<String> parameters) {
        }

        @Override
        public boolean check(List<String> parameters) {
            return false;
        }
    }

    /** Tipo de partido cuyo constructor falla para comprobar la causa reflejada. */
    public static class MatchTypeWithFailingConstructor extends TypeOfMatch {

        public MatchTypeWithFailingConstructor(List<String> parameters) {
            throw new IllegalStateException("Constructor failure");
        }

        @Override
        public boolean check(List<String> parameters) {
            return false;
        }
    }

}
