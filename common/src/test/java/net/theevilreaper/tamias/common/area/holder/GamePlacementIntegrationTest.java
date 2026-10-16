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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class GamePlacementIntegrationTest {

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

        GroundData randomData = GroundDataRegistry.instance().getRandomData();

        placement.triggerPlacement(randomData);

        env.tickWhile(placement::isRunning, Duration.ofSeconds(60));

        assertBlock(instance, randomData.groundBlock(), gameArea.getPositions());

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
        prepareGround(instance, markerPosition);

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
        Vec overhangPosition = new Vec(1, 0, 2);
        prepareGround(instance, clearPosition);
        prepareGround(instance, obstructedPosition);
        instance.setBlock(obstructedPosition.add(0, 1, 0), Block.STONE);
        prepareGround(instance, overhangPosition);
        instance.setBlock(overhangPosition.add(0, GameAreaHelper.TNT_DROP_HEIGHT, 0), Block.STONE);

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
        assertEquals(Block.AIR, instance.getBlock(overhangPosition.add(0, 1, 0)), "A structure above blocks the drop path");

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

        gameArea.getPositions().forEach(pos -> prepareGround(instance, pos));

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
        assertEquals(tntPositions, new HashSet<Point>(gameArea.getTntPositions()), "The dropped TNT blocks are the respawn tickets");

        env.destroyInstance(instance);
    }

    @Test
    void testSpecialBlocksArePlaced(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(14);
        gameArea.getPositions().forEach(pos -> prepareGround(instance, pos));

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        gamePlacement.flatten();

        GroundData groundData = new GroundData(Block.STONE, List.of(Block.GOLD_BLOCK));
        gamePlacement.triggerPlacement(groundData);
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));

        long specialBlocks = gameArea.getPositions().stream()
                .filter(pos -> instance.getBlock(pos) == Block.GOLD_BLOCK)
                .count();
        // 225 positions, one special block for each started 100 positions
        assertEquals(3, specialBlocks);

        env.destroyInstance(instance);
    }

    private static GameArea createArea(int size) {
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(size, 0, size))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();
        return gameArea;
    }

    /**
     * Marks the given position as ground and clears the drop path above it.
     */
    private static void prepareGround(Instance instance, Point position) {
        instance.setBlock(position, GameAreaHelper.GROUND_MARKER_BLOCK);
        for (int y = 1; y <= GameAreaHelper.TNT_DROP_HEIGHT + 1; y++) {
            instance.setBlock(position.add(0, y, 0), Block.AIR);
        }
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
