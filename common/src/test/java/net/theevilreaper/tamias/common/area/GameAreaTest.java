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
        assertTrue(gameArea.getTntPositions().isEmpty());
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
    void testRandomPositionLooksToAreaCenter() {
        GameArea gameArea = new GameArea(areaData);

        // The area spans 0..10, so its middle is the centre of block 5
        gameArea.setTntPositions(List.of(new Vec(0, 1, 5)));
        assertEquals(-90f, gameArea.getRandomPosition().yaw(), 0.01f);

        gameArea.setTntPositions(List.of(new Vec(10, 1, 5)));
        assertEquals(90f, gameArea.getRandomPosition().yaw(), 0.01f);

        gameArea.setTntPositions(List.of(new Vec(5, 1, 0)));
        assertEquals(0f, gameArea.getRandomPosition().yaw(), 0.01f);

        gameArea.setTntPositions(List.of(new Vec(5, 1, 10)));
        Pos north = gameArea.getRandomPosition();
        assertEquals(180f, Math.abs(north.yaw()), 0.01f);
        assertEquals(0f, north.pitch(), 0.01f);
        assertEquals(new Vec(5, 1, 10), north.asVec());
    }

    @Test
    void testRandomPositionInAreaCenterLooksSouth() {
        GameArea gameArea = new GameArea(areaData);

        // Block 5 is the exact middle of the 0..10 area, so there is no direction to look at
        gameArea.setTntPositions(List.of(new Vec(5, 1, 5)));
        Pos center = gameArea.getRandomPosition();
        assertEquals(0f, center.yaw(), 0.01f);
        assertEquals(0f, center.pitch(), 0.01f);
        assertEquals(new Vec(5, 1, 5), center.asVec());
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
        gameArea.addTntPosition(new Vec(1, 1, 1));
        assertFalse(gameArea.getSpecialPositions().isEmpty());
        assertFalse(gameArea.getTntPositions().isEmpty());

        gameArea.reset();
        assertTrue(gameArea.getSpecialPositions().isEmpty());
        assertTrue(gameArea.getTntPositions().isEmpty());
    }

    @Test
    void testAddTntPositionKeepsOldPositions() {
        GameArea gameArea = new GameArea(areaData);
        gameArea.addTntPosition(new Vec(1, 1, 1));
        gameArea.addTntPosition(new Vec(2, 1, 2));

        assertEquals(Set.of(new Vec(1, 1, 1), new Vec(2, 1, 2)), gameArea.getTntPositions());
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
