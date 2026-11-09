package edu.eci.DOPOSoccer.domain;
import edu.eci.DOPOSoccer.domain.Game;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;

/** Esqueleto de pruebas para Game */
public class GameTest {
    private Game game;

    @BeforeAll
    public void createGame() {
        game = new Game();
        assertEquals(Game.class, game.getClass());
    }

    
}
