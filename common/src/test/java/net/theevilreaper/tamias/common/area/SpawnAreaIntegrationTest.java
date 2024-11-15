package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.VoidGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class SpawnAreaIntegrationTest {

    @ParameterizedTest(name = "Test invalid spawn area argument usage with face {0}")
    @ValueSource(strings = {"DOWN", "UP"})
    void testInvalidSpawnAreaArgumentUsage(String face, @NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Optional<Direction> directionOptional = Arrays.stream(Direction.values()).filter(value -> value.name().equals(face)).findFirst();
        assertTrue(directionOptional.isPresent());

        Direction direction = directionOptional.get();
        assertThrows(
                IllegalArgumentException.class,
                () -> new SpawnArea(instance, Pos.ZERO, direction, 10),
                "The direction must be horizontal"
        );
        env.destroyInstance(instance);
    }

    @Test
    void testInvalidPositionUsage(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        assertThrows(
                IllegalArgumentException.class,
                () -> new SpawnArea(instance, Pos.ZERO, Direction.NORTH, 0),
                "The direction must be horizontal"
        );
        env.destroyInstance(instance);
    }

    @ParameterizedTest(name = "Test spawn area block set usage with face {0}")
    @ValueSource(strings = {"NORTH", "SOUTH", "EAST", "WEST"})
    void testSpawnBlocks(String face, @NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setGenerator(new VoidGenerator());
        Optional<Direction> directionOptional = Arrays.stream(Direction.values()).filter(value -> value.name().equals(face)).findFirst();
        assertTrue(directionOptional.isPresent());

        Pos startPos = Pos.ZERO.add(0, 1, 0);
        int maxPositions = 3;
        SpawnArea spawnArea = new SpawnArea(instance, startPos, directionOptional.get(), maxPositions);
        instance.loadChunk(Pos.ZERO).join();
        assertNotNull(spawnArea);


        Pos pos = Pos.fromPoint(startPos);

        // Default is stone
        for (int i = 0; i < maxPositions; i++) {
            pos = updatedPos(pos, directionOptional.get());
            assertBlockRegion(Block.AIR, instance, pos);
        }

        pos = Pos.fromPoint(startPos);
        spawnArea.spawnBlocks();
        int currentPositions = 0;

        // Check the start position
        assertBlockRegion(Block.TNT, instance, pos);

        for (int i = 1; i < maxPositions; i++) {
            pos = updatedPos(pos, directionOptional.get());
            assertBlockRegion(Block.TNT, instance, pos);
            currentPositions = i + 1;
        }

        assertEquals(maxPositions, currentPositions);
        env.destroyInstance(instance);
    }

    @Test
    void testSpawnBlockRemove(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setGenerator(new VoidGenerator());
        Direction northDir = Direction.NORTH;
        int maxPositions = 3;
        Pos startPos = Pos.ZERO.add(0, 1, 0);
        SpawnArea spawnArea = new SpawnArea(instance, startPos, northDir, maxPositions);
        assertNotNull(spawnArea);

        spawnArea.spawnBlocks();

        spawnArea.reset();

        int currentPositions = 0;

        Pos areStart = Pos.fromPoint(startPos);
        for (int i = 0; i < maxPositions; i++) {
            areStart = updatedPos(areStart, northDir);
            assertBlockRegion(Block.AIR, instance, areStart);
            currentPositions = i + 1;
        }

        assertEquals(maxPositions, currentPositions);

        env.destroyInstance(instance);
    }

    /**
     * Updates the position based on the given direction
     *
     * @param vec       the current position
     * @param direction the direction to move
     * @return the updated position
     */
    private @NotNull Pos updatedPos(@NotNull Pos vec, @NotNull Direction direction) {
        Pos pos = switch (direction) {
            case NORTH -> new Pos(1, 0, 0);
            case SOUTH -> new Pos(-1, 0, 0);
            case EAST -> new Pos(0, 0, 1);
            case WEST -> new Pos(0, 0, -1);
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
        return vec.add(pos);
    }

    /**
     * Asserts the block at the given position
     *
     * @param expected the expected block
     * @param instance the instance to check
     * @param startPos the position to check
     */
    private void assertBlockRegion(@NotNull Block expected, @NotNull Instance instance, @NotNull Pos startPos) {
        Chunk chunk = instance.getChunkAt(startPos);
        if (!ChunkUtils.isLoaded(chunk)) {
            instance.loadChunk(startPos).join();
        }
        Block block = instance.getBlock(startPos);
        assertNotNull(block);
        assertEquals(expected, block);
    }
}