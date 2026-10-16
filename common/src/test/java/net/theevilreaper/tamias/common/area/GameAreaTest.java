package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
        gameArea.calculatePositions();

        assertNotNull(gameArea);
        assertFalse(gameArea.getPositions().isEmpty());

        assertNotEquals(0, gameArea.getPositions().size());
        assertNotEquals(GameAreaHelper.MAX_TNT_AMOUNT + 1, gameArea.getTntPositions().size());
        assertNotEquals(GameAreaHelper.MIN_TNT_AMOUNT - 1, gameArea.getTntPositions().size());
        assertTrue(gameArea.getSpecialPositions().isEmpty());

        assertNotNull(gameArea.getGameAreaData());
        assertEquals(areaData, gameArea.getGameAreaData());
    }

    @Test
    void testRandomPositionGet() {
        GameArea gameArea = new GameArea(areaData);
        gameArea.calculatePositions();
        gameArea.setTntPositions(List.of(new Vec(1, 1, 1), new Vec(2, 1, 2)));
        assertNotNull(gameArea);
        assertFalse(gameArea.getPositions().isEmpty());

        Pos randomPosition = gameArea.getRandomPosition();
        assertNotNull(randomPosition);
        assertNotEquals(new Pos(-1, -1, -1), randomPosition);
        int updatedPositionValue = POSITION_VALUE + 1;
        assertNotEquals(new Pos(updatedPositionValue, updatedPositionValue, updatedPositionValue), randomPosition);
    }

    @Test
    void testTntPositionsAreConsumedOnce() {
        GameArea gameArea = new GameArea(areaData);
        gameArea.setTntPositions(List.of(new Vec(1, 1, 1), new Vec(2, 1, 2)));

        Pos first = gameArea.getRandomPosition();
        Pos second = gameArea.getRandomPosition();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
        assertNull(gameArea.getRandomPosition());
    }

    @Test
    void testSetTntPositionsReplacesOldPositions() {
        GameArea gameArea = new GameArea(areaData);
        gameArea.setTntPositions(List.of(new Vec(1, 1, 1)));
        gameArea.setTntPositions(List.of(new Vec(2, 1, 2)));

        assertEquals(Set.of(new Vec(2, 1, 2)), gameArea.getTntPositions());
    }

    @Test
    void testGameAreaClear() {
        GameArea gameArea = new GameArea(areaData);
        assertNotNull(gameArea);
        gameArea.calculatePositions();
        gameArea.calculateSpecialBlockPositions(() -> 1);
        gameArea.setTntPositions(List.of(new Vec(1, 1, 1)));
        assertFalse(gameArea.getSpecialPositions().isEmpty());
        assertFalse(gameArea.getTntPositions().isEmpty());

        gameArea.reset();
        assertTrue(gameArea.getSpecialPositions().isEmpty());
        assertTrue(gameArea.getTntPositions().isEmpty());
    }

    @Test
    void testSpecialBlockCalculationReplacesOldPositions() {
        GameArea gameArea = new GameArea(areaData);
        gameArea.calculatePositions();

        gameArea.calculateSpecialBlockPositions(() -> 3);
        assertEquals(3, gameArea.getSpecialPositions().size());

        gameArea.calculateSpecialBlockPositions(() -> 2);
        assertEquals(2, gameArea.getSpecialPositions().size());

        int allPositions = gameArea.getPositions().size();
        gameArea.calculateSpecialBlockPositions(() -> allPositions + 5);
        assertEquals(allPositions, gameArea.getSpecialPositions().size());
    }

    @Test
    void testPositionCalculationTwice() {
        AreaData areaData = AreaData.builder()
                .lowerCorner(Vec.ZERO)
                .upperCorner(new Vec(1, 1, 1))
                .facing(Direction.DOWN)
                .build();

        GameArea gameArea = new GameArea(areaData);
        gameArea.calculatePositions();
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
        gameArea.calculatePositions();
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
