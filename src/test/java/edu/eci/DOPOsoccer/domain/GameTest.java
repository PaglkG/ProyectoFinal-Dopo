package edu.eci.DOPOsoccer.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    //Confirmamos que se crea la clase principal del dominio 
    @Test
    public void testGameExists() {
        Game game = new Game();
        assertNotNull(game);
    }
    
}