package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class GamePlacementIntegrationTest {

    @Disabled("Should be revisited in the near future")
    @Test
    void testGamePlacement(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(5, 0, 5))
                        .facing(Direction.NORTH)
                        .build()
        );
        assertNotNull(gameArea);
        gameArea.calculatePositions();
        GamePlacement placement = new GamePlacement(instance, gameArea);
        assertNotNull(placement);
        assertInstanceOf(GamePlacement.class, placement);

        placement.applyPositions();

        Set<Point> testPositions = new HashSet<>();

        Vec start = gameArea.getGameAreaData().lowerCorner();
        Vec end = gameArea.getGameAreaData().upperCorner();

        // Check if the ground has been prepared by the system
        int startBlockX = Math.min(start.blockX(), end.blockX());
        int endBlockX = Math.max(start.blockX(), end.blockX());
        int startBlockZ = Math.min(start.blockZ(), end.blockZ());
        int endBlockZ = Math.max(start.blockZ(), end.blockZ());
        int blockY = start.blockY();

        // Calculate positions without applying them to the instance yet
        for (int x = startBlockX; x <= endBlockX; x++) {
            for (int z = startBlockZ; z <= endBlockZ; z++) {
                testPositions.add(new Vec(x, blockY, z));
            }
        }

        assertPostions(gameArea.getPositions(), testPositions);
        assertBlock(instance, Block.BARRIER, gameArea.getPositions());

        gameArea.calculateTntPositions(() -> 1);
       // gameArea.calculateTntPositions(() -> (int) (((double) testPositions.size() / 2) * 0.5));

        assertNotNull(gameArea.getTntPositions(), "The TNT positions should not be null");

        GroundData randomData = GroundDataRegistry.instance().getRandomData();

        placement.triggerPlacement(randomData);

        env.tickWhile(placement::isRunning, Duration.ofSeconds(60));

        assertBlock(instance, randomData.groundBlock(), gameArea.getPositions());

       // assertBlock(instance, Block.TNT, gameArea.getTntPositions());
//        assertBlock(instance, Block.TNT, gameArea.getTntPositions().stream().map(tnt -> tnt.add(0, -1,0)).collect(Collectors.toSet()));

        env.destroyInstance(instance);
    }

    @Test
    void testPreLoadChunksForAreaPlacementWorks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(20, 0, 20))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);

        assertDoesNotThrow(() -> gamePlacement.preloadChunks().get(10, java.util.concurrent.TimeUnit.SECONDS));

        env.destroyInstance(instance);
    }

    @Test
    void testFlattenOnlyKeepsGroundMarkerPositions(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(2, 0, 2))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();

        Vec markerPosition = new Vec(1, 0, 1);
        instance.setBlock(markerPosition, GameAreaHelper.GROUND_MARKER_BLOCK);
        instance.setBlock(markerPosition.add(0, 1, 0), Block.AIR);

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);

        gamePlacement.flatten();

        assertEquals(Set.of(markerPosition), gameArea.getPositions());

        GroundData randomData = GroundDataRegistry.instance().getRandomData();
        gamePlacement.triggerPlacement(randomData);
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));

        assertEquals(randomData.groundBlock(), instance.getBlock(markerPosition));
        assertEquals(Block.AIR, instance.getBlock(new Vec(0, 0, 0)));

        env.destroyInstance(instance);
    }

    @Test
    void testDropTntSkipsObstructedPositions(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(2, 0, 2))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();

        Vec clearPosition = new Vec(1, 0, 0);
        Vec obstructedPosition = new Vec(1, 0, 1);
        instance.setBlock(clearPosition, GameAreaHelper.GROUND_MARKER_BLOCK);
        instance.setBlock(clearPosition.add(0, 1, 0), Block.AIR);
        instance.setBlock(obstructedPosition, GameAreaHelper.GROUND_MARKER_BLOCK);
        instance.setBlock(obstructedPosition.add(0, 1, 0), Block.STONE);

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        gamePlacement.flatten();

        gamePlacement.triggerPlacement(GroundDataRegistry.instance().getRandomData());
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));

        gamePlacement.dropTnt(() -> 5);
        for (int i = 0; i < 20; i++) {
            env.tick();
        }

        assertEquals(Block.TNT, instance.getBlock(clearPosition.add(0, 1, 0)));
        assertEquals(Block.STONE, instance.getBlock(obstructedPosition.add(0, 1, 0)));

        gamePlacement.clear();
        assertEquals(Block.AIR, instance.getBlock(clearPosition.add(0, 1, 0)));

        env.destroyInstance(instance);
    }

    @Test
    void testDropTntSpreadsPositionsOut(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(9, 0, 9))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();

        for (Point pos : gameArea.getPositions()) {
            instance.setBlock(pos, GameAreaHelper.GROUND_MARKER_BLOCK);
            instance.setBlock(pos.add(0, 1, 0), Block.AIR);
        }

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        gamePlacement.flatten();

        gamePlacement.triggerPlacement(GroundDataRegistry.instance().getRandomData());
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));

        gamePlacement.dropTnt(() -> 9);
        for (int i = 0; i < 60; i++) {
            env.tick();
        }

        Set<Point> tntPositions = new HashSet<>();
        for (Point pos : gameArea.getPositions()) {
            Point above = pos.add(0, 1, 0);
            if (instance.getBlock(above) == Block.TNT) {
                tntPositions.add(above);
            }
        }

        assertEquals(9, tntPositions.size());

        double minDistance = Double.MAX_VALUE;
        for (Point a : tntPositions) {
            for (Point b : tntPositions) {
                if (a == b) continue;
                minDistance = Math.min(minDistance, a.distance(b));
            }
        }
        assertTrue(minDistance >= 2.0, "TNT positions should be spread apart, closest pair was " + minDistance + " blocks apart");

        env.destroyInstance(instance);
    }

    /**
     * Asserts that the given block is present at the specified positions.
     *
     * @param instance  the instance to check the blocks in
     * @param expected  the expected block
     * @param positions the positions to check
     */
    private void assertBlock(@NotNull Instance instance, @NotNull Block expected, @NotNull Set<Point> positions) {
        String blockName = expected.name();
        for (Point testPosition : positions) {
            Block block = instance.getBlock(testPosition);
            assertNotNull(block);
            assertEquals(expected, block, "The block at " + testPosition + " should be a " + blockName);
        }
    }

    /**
     * Asserts that the given positions are present in the area.
     *
     * @param expected the expected positions
     * @param given    the given positions
     * @param <T>      the type of the position
     */
    private <T extends Point> void assertPostions(@NotNull Set<T> expected, @NotNull Set<T> given) {
        assertEquals(expected.size(), given.size());

        for (T point : expected) {
            assertTrue(given.contains(point), "The position " + point + " should be in the area");
        }
    }
}
