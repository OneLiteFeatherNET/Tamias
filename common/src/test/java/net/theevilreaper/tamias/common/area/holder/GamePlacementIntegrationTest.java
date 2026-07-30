package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.placement.CircleAreaPlacement;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
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
        CircleAreaPlacement groundPlacement = new CircleAreaPlacement(
                instance,
                gameArea.getPositions().stream().map(Vec.class::cast).toList(),
                new ArrayList<>()
        );
        GamePlacement placement = new GamePlacement(instance, gameArea, groundPlacement);
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
        CircleAreaPlacement groundPlacement = new CircleAreaPlacement(instance, new ArrayList<>(), new ArrayList<>());
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea, groundPlacement);

        assertDoesNotThrow(() -> gamePlacement.preloadChunks().get(10, java.util.concurrent.TimeUnit.SECONDS));

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
