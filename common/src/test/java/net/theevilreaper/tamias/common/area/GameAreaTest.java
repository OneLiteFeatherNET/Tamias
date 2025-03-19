package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameAreaTest {

    private static final int POSITION_VALUE = 10;
    private static AreaData areaData;

    @BeforeAll
    static void setUp() {
        areaData = AreaData.builder()
                .lowerCorner(Vec.ZERO)
                .upperCorner(new Vec(POSITION_VALUE, POSITION_VALUE, POSITION_VALUE))
                .facing(Direction.DOWN)
                .build();
    }

    @Test
    void testPositionAccess() {
        GameArea gameArea = new GameArea(areaData);
        assertNotNull(gameArea);
        assertFalse(gameArea.getPositions().isEmpty());

        assertNotEquals(0, gameArea.getPositions().size());
        assertNotEquals(GameAreaHelper.MAX_TNT_AMOUNT + 1, gameArea.getTntPositions().size());
        assertNotEquals(GameAreaHelper.MIN_TNT_AMOUNT - 1, gameArea.getTntPositions().size());
        assertFalse(gameArea.getSpecialPositions().isEmpty());

        assertNotNull(gameArea.getGameAreaData());
        assertEquals(areaData, gameArea.getGameAreaData());
    }

    @Test
    void testRandomPositionGet() {
        GameArea gameArea = new GameArea(areaData);
        assertNotNull(gameArea);
        assertFalse(gameArea.getPositions().isEmpty());

        Pos randomPosition = gameArea.getRandomPosition();
        assertNotNull(randomPosition);
        assertNotEquals(new Pos(-1, -1, -1), randomPosition);
        int updatedPositionValue = POSITION_VALUE + 1;
        assertNotEquals(new Pos(updatedPositionValue, updatedPositionValue, updatedPositionValue), randomPosition);
    }

    @Test
    void testGameAreaClear() {
        GameArea gameArea = new GameArea(areaData);
        assertNotNull(gameArea);
        assertFalse(gameArea.getSpecialPositions().isEmpty());
        assertFalse(gameArea.getTntPositions().isEmpty());

        gameArea.reset();
        assertTrue(gameArea.getSpecialPositions().isEmpty());
        assertTrue(gameArea.getTntPositions().isEmpty());
    }

    @Test
    void testPositionCalculationTwice() {
        AreaData areaData = AreaData.builder()
                .lowerCorner(Vec.ZERO)
                .upperCorner(new Vec(1, 1, 1))
                .facing(Direction.DOWN)
                .build();

        GameArea gameArea = new GameArea(areaData);

        assertFalse(gameArea.getPositions().isEmpty());

        assertThrowsExactly(
                IllegalStateException.class,
                gameArea::calculatePositions,
                "The calculation only can runs at once"
        );
    }

    @Test
    void testPositionCalculation() {
        GameArea gameArea = new GameArea(areaData);
        assertNotNull(gameArea);
        assertFalse(gameArea.getPositions().isEmpty());

        Vec start = Vec.ZERO;

        for (int x = 0; x < POSITION_VALUE; x++) {
            for (int z = 0; z < POSITION_VALUE; z++) {
                Vec position = start.add(x, 0, z);
                assertTrue(gameArea.getPositions().contains(position));
            }
        }
    }
}
