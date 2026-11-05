package edu.eci.DOPOSoccer.domain;

import org.junit.jupiter.api.Test;

import edu.eci.DOPOSoccer.domain.Game;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    public void testGameExists() {
        Game game = new Game();
        assertNotNull(game);
    }

}
