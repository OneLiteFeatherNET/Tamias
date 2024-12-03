package net.theevilreaper.tamias.common.map;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameMapTest {

    @Test
    void testEmptyGameMapCreation() {
        GameMap gameMap = new GameMap();
        assertNotNull(gameMap);
        assertEquals(Pos.ZERO, gameMap.getSpawn());
        assertEquals("Not set", gameMap.getName());
        assertEquals(0, gameMap.getBuilders().length);
    }
}
