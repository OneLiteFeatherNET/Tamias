package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class SpawnAreaIntegrationTest {

    @ParameterizedTest(name = "Test invalid spawn area argument usage with face {0}")
    @ValueSource(strings = {"DOWN", "UP"})
    void testInvalidSpawnAreaArgumentUsage(String face, @NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Optional<Direction> directionOptional = Arrays.stream(Direction.values()).filter(value -> value.name().equals(face)).findFirst();
        assertTrue(directionOptional.isPresent());

        assertThrows(
                IllegalArgumentException.class,
                () -> new SpawnArea(instance, Pos.ZERO, directionOptional.get(), 10),
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

    @Disabled
    @ParameterizedTest(name = "Test spawn area block set usage with face {0}")
    @ValueSource(strings = {"NORTH", "SOUTH", "EAST", "WEST"})
    void testSpawnBlocks(String face, @NotNull Env env) {
        Instance instance = env.createFlatInstance();
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
            assertBlockRegion(Block.STONE, instance, pos);
        }

        pos = Pos.fromPoint(startPos);
        spawnArea.spawnBlocks();
        int currentPositions = 0;

        for (int i = 0; i < maxPositions; i++) {
            pos = updatedPos(pos, directionOptional.get());
            assertBlockRegion(Block.TNT, instance, pos);
            currentPositions = i + 1;
        }

        assertEquals(maxPositions, currentPositions);
        env.destroyInstance(instance);
    }

    @Disabled
    @Test
    void testSpawnBlockRemove(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
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
            assertBlockRegion(Block.STONE, instance, areStart);
            currentPositions = i + 1;
        }

        assertEquals(maxPositions, currentPositions);

        env.destroyInstance(instance);
    }

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

    private void assertBlockRegion(@NotNull Block expected, @NotNull Instance instance, @NotNull Pos startPos) {
        Block block = instance.getBlock(startPos);
        assertNotNull(block);
        assertEquals(expected, block);
    }
}