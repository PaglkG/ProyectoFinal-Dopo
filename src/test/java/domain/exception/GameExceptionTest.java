package domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** Comprueba que {@link GameException} conserva los mensajes y causas recibidos. */
class GameExceptionTest {

    @Test
    void constructorPreservesTheSpanishMessage() {
        GameException exception = new GameException(GameException.NO_FOUND_SESSION);

        assertEquals("no se encontro la session de juego", exception.getMessage());
    }

    @Test
    void constructorPreservesTheMessageAndCause() {
        Throwable cause = new ReflectiveOperationException("reflection failure");
        GameException exception = new GameException(GameException.NO_INSTANTIATE_CLASS, cause);

        assertEquals("no se puede instancia la clase", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
