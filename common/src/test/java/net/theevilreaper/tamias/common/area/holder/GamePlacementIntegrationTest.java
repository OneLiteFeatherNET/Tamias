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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        Set<Vec> testPositions = new HashSet<>();

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
    void testPrepareKeepsTheGroundOnASecondCall(@NotNull Env env) {
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

        gamePlacement.prepare();
        gamePlacement.prepare();

        assertTrue(gamePlacement.isPrepared());
        assertEquals(Set.of(markerPosition), gameArea.getPositions());
        assertEquals(Block.BARRIER, instance.getBlock(markerPosition));

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

        CompletableFuture<Void> clearing = gamePlacement.clearAsync();
        env.tickWhile(() -> !clearing.isDone(), Duration.ofSeconds(5));
        assertEquals(Block.AIR, instance.getBlock(clearPosition.add(0, 1, 0)));

        env.destroyInstance(instance);
    }

    @Test
    void testClearRemovesGroundAndTnt(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(3, 0, 3))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();
        for (Point position : gameArea.getPositions()) {
            prepareGround(instance, position);
        }

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        gamePlacement.flatten();
        gamePlacement.triggerPlacement(GroundDataRegistry.instance().getRandomData());
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));
        gamePlacement.dropTnt(() -> 4);
        for (int i = 0; i < 40; i++) {
            env.tick();
        }
        Set<Vec> tntPositions = Set.copyOf(gameArea.getTntPositions());
        assertEquals(4, tntPositions.size());

        CompletableFuture<Void> clearing = gamePlacement.clearAsync();
        env.tickWhile(() -> !clearing.isDone(), Duration.ofSeconds(5));

        assertBlock(instance, Block.AIR, gameArea.getPositions());
        for (Vec tntPosition : tntPositions) {
            assertEquals(Block.AIR, instance.getBlock(tntPosition));
        }
        assertTrue(gameArea.getTntPositions().isEmpty());

        env.destroyInstance(instance);
    }

    @Test
    void testDropTntPlacesTheRequestedAmount(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(9);
        gameArea.getPositions().forEach(pos -> prepareGround(instance, pos));

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        gamePlacement.flatten();

        gamePlacement.triggerPlacement(GroundDataRegistry.instance().getRandomData());
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));

        gamePlacement.dropTnt(() -> 9);
        for (int i = 0; i < 80; i++) {
            env.tick();
        }

        Set<Point> tntPositions = findTnt(instance, gameArea);
        assertEquals(9, tntPositions.size());
        assertEquals(tntPositions, new HashSet<Point>(gameArea.getTntPositions()), "The dropped TNT blocks are the respawn tickets");

        env.destroyInstance(instance);
    }

    @Test
    void testDropTntFollowsTheGroundPlacement(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(29);
        gameArea.getPositions().forEach(pos -> prepareGround(instance, pos));

        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        gamePlacement.flatten();

        GroundData groundData = new GroundData(Block.STONE, null);
        gamePlacement.triggerPlacement(groundData);
        gamePlacement.dropTnt(() -> GameAreaHelper.MAX_TNT_AMOUNT);

        boolean groundUnfinishedWhileTntLanded = false;
        for (int i = 0; i < 200; i++) {
            env.tick();
            Set<Point> tntPositions = findTnt(instance, gameArea);
            for (Point tnt : tntPositions) {
                assertEquals(Block.STONE, instance.getBlock(tnt.sub(0, 1, 0)), "TNT at " + tnt + " landed without ground");
            }
            assertEquals(tntPositions, new HashSet<Point>(gameArea.getTntPositions()), "Only landed TNT is a respawn ticket");
            if (!tntPositions.isEmpty() && gamePlacement.isRunning()) {
                groundUnfinishedWhileTntLanded = true;
            }
        }

        assertTrue(groundUnfinishedWhileTntLanded, "The TNT should drop while the ground is still placed");
        assertEquals(GameAreaHelper.MAX_TNT_AMOUNT, findTnt(instance, gameArea).size());

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

    /**
     * Places the ground and drops the TNT with one forced supply drop.
     */
    private static Set<Point> dropWithSupply(Env env, Instance instance, GameArea gameArea, GamePlacement gamePlacement, GroundData groundData) {
        return dropWithSupply(env, instance, gameArea, gamePlacement, groundData, () -> {});
    }

    private static Set<Point> dropWithSupply(
            Env env,
            Instance instance,
            GameArea gameArea,
            GamePlacement gamePlacement,
            GroundData groundData,
            Runnable beforeDrop
    ) {
        gameArea.getPositions().forEach(pos -> prepareHighGround(instance, pos));
        gamePlacement.flatten();
        gamePlacement.triggerPlacement(groundData);
        env.tickWhile(gamePlacement::isRunning, Duration.ofSeconds(10));
        beforeDrop.run();

        CompletableFuture<Void> landed = gamePlacement.dropTnt(() -> 15, 1, new Random(4));
        env.tickWhile(() -> !landed.isDone(), Duration.ofSeconds(10));
        assertTrue(landed.isDone());
        return findTnt(instance, gameArea);
    }

    private static Set<Point> findBlocks(Instance instance, GameArea gameArea, Block block) {
        Set<Point> positions = new HashSet<>();
        for (Vec position : gameArea.getPositions()) {
            if (instance.getBlock(position) == block) positions.add(position);
        }
        return positions;
    }

    @Test
    void testRingIsPlacedWithTheImpactBlock(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(14);
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);

        Set<Point> tnt = dropWithSupply(env, instance, gameArea, gamePlacement, new GroundData(Block.STONE, null, Block.GOLD_BLOCK));
        Set<Point> ring = findBlocks(instance, gameArea, Block.GOLD_BLOCK);

        assertEquals(15, tnt.size());
        assertFalse(ring.isEmpty(), "The supply drop places a ring");
        boolean aroundOneTnt = tnt.stream().anyMatch(center -> ring.stream().allMatch(position -> {
            double dx = position.x() - center.x();
            double dz = position.z() - center.z();
            double distance = dx * dx + dz * dz;
            return distance >= 1.5 * 1.5 && distance < 2.5 * 2.5;
        }));
        assertTrue(aroundOneTnt, "The ring lies around the impact of the supply drop");

        env.destroyInstance(instance);
    }

    @Test
    void testRingKeepsSpecialPositions(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(14);
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        // Every position counts as special once the ground is placed, so the ring must not change any of them
        dropWithSupply(env, instance, gameArea, gamePlacement, new GroundData(Block.STONE, null, Block.GOLD_BLOCK),
                () -> gameArea.getSpecialPositions().addAll(gameArea.getPositions()));

        assertTrue(findBlocks(instance, gameArea, Block.GOLD_BLOCK).isEmpty());
        env.destroyInstance(instance);
    }

    @Test
    void testDropWithoutImpactBlockPlacesNoRing(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(14);
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);

        dropWithSupply(env, instance, gameArea, gamePlacement, new GroundData(Block.STONE, null));

        assertEquals(gameArea.getPositions().size(), findBlocks(instance, gameArea, Block.STONE).size());
        env.destroyInstance(instance);
    }

    @Test
    void testClearRemovesTheRing(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = createArea(14);
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);
        dropWithSupply(env, instance, gameArea, gamePlacement, new GroundData(Block.STONE, null, Block.GOLD_BLOCK));
        List<Vec> areaPositions = List.copyOf(gameArea.getPositions());

        CompletableFuture<Void> cleared = gamePlacement.clearAsync();
        env.tickWhile(() -> !cleared.isDone(), Duration.ofSeconds(5));

        assertTrue(areaPositions.stream().noneMatch(position -> instance.getBlock(position) == Block.GOLD_BLOCK));
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
     * Marks the given position as ground and clears the drop path of a supply drop above it.
     */
    private static void prepareHighGround(Instance instance, Point position) {
        instance.setBlock(position, GameAreaHelper.GROUND_MARKER_BLOCK);
        for (int y = 1; y <= GameAreaHelper.SUPPLY_DROP_HEIGHT + 1; y++) {
            instance.setBlock(position.add(0, y, 0), Block.AIR);
        }
    }

    private static Set<Point> findTnt(Instance instance, GameArea gameArea) {
        Set<Point> tntPositions = new HashSet<>();
        for (Point pos : gameArea.getPositions()) {
            Point above = pos.add(0, 1, 0);
            if (instance.getBlock(above) == Block.TNT) {
                tntPositions.add(above);
            }
        }
        return tntPositions;
    }

    /**
     * Asserts that the given block is present at the specified positions.
     *
     * @param instance  the instance to check the blocks in
     * @param expected  the expected block
     * @param positions the positions to check
     */
    private void assertBlock(@NotNull Instance instance, @NotNull Block expected, @NotNull Collection<? extends Point> positions) {
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

    @Test
    void testStopPlacementsStopsRunningGroundWithoutRemovingBlocks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(40, 0, 40))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();
        GamePlacement placement = new GamePlacement(instance, gameArea);
        placement.applyPositions();

        placement.triggerPlacement(GroundDataRegistry.instance().getRandomData());
        for (int i = 0; i < 3; i++) {
            env.tick();
        }
        assertTrue(placement.isRunning(), "The ground must still be placing before the stop");

        placement.stopPlacements();
        assertFalse(placement.isRunning(), "The ground placement must be stopped");

        long placedAfterStop = countPlacedGround(instance, gameArea);
        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertEquals(placedAfterStop, countPlacedGround(instance, gameArea), "No ground may be placed after the stop");
        assertTrue(placedAfterStop > 0, "The already placed ground must stay in the instance");
        assertTrue(placedAfterStop < gameArea.getPositions().size(), "The ground must not have been finished");
        env.destroyInstance(instance);
    }

    private static long countPlacedGround(Instance instance, GameArea gameArea) {
        return gameArea.getPositions().stream()
                .filter(position -> instance.getBlock(position) != Block.BARRIER)
                .count();
    }
}
