package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.VoidGenerator;
import net.theevilreaper.tamias.common.area.holder.Placement;
import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class SpawnAreaIntegrationTest {

    private static GroundData spawnData;
    private static Direction[] directionValues;

    @BeforeAll
    static void init() {
        spawnData = new GroundData(Block.TNT, null);
        directionValues = Direction.values();
    }

    @ParameterizedTest(name = "Test spawn area block set usage with face {0}")
    @ValueSource(strings = {"NORTH", "SOUTH", "EAST", "WEST"})
    void testSpawnBlocks(String face, @NotNull Env env) {
        int maxPositions = 3;
        Instance instance = env.createFlatInstance();
        for (int i = 0; i < maxPositions; i++) {
            env.createPlayer(instance);
        }
        instance.setGenerator(new VoidGenerator());
        Optional<Direction> directionOptional = Arrays.stream(directionValues).filter(value -> value.name().equals(face)).findFirst();
        assertTrue(directionOptional.isPresent());

        Pos startPos = Pos.ZERO;
        SpawnArea spawnArea = new SpawnArea(new SpawnLayer(Pos.ZERO, directionOptional.get()), maxPositions);
        instance.loadChunk(Pos.ZERO).join();
        assertNotNull(spawnArea);

        Pos pos = Pos.fromPoint(startPos);

        // Default is stone
        for (int i = 0; i < maxPositions; i++) {
            pos = updatedPos(pos, directionOptional.get());
            assertBlockRegion(Block.STONE, instance, pos);
        }

        Placement placement = new SpawnPlacement(instance, spawnArea);

        pos = Pos.fromPoint(startPos);
        placement.triggerPlacement(spawnData);
        int currentPositions = 0;

        // Check the start position
        assertBlockRegion(Block.TNT, instance, pos);

        for (int i = 1; i < maxPositions; i++) {
            pos = updatedPos(pos, directionOptional.get());
            assertBlockRegion(Block.TNT, instance, pos);
            currentPositions = i + 1;
        }

        assertEquals(maxPositions, currentPositions);
        env.destroyInstance(instance, true);
    }

    @Test
    void testSpawnBlockRemove(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setGenerator(new VoidGenerator());
        Direction northDir = Direction.NORTH;
        int maxPositions = 3;
        Pos startPos = Pos.ZERO.add(0, 1, 0);
        SpawnArea spawnArea = new SpawnArea(new SpawnLayer(Pos.ZERO, northDir), maxPositions);
        assertNotNull(spawnArea);

        Placement placement = new SpawnPlacement(instance, spawnArea);

        placement.triggerPlacement(spawnData);
        placement.clear();

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
     * @param data       the current position
     * @param direction the direction to move
     * @return the updated position
     */
    private @NotNull Pos updatedPos(@NotNull Pos data, @NotNull Direction direction) {
        Vec vec = switch (direction) {
            case NORTH -> new Vec(0, 0, -1); // Negative Z
            case SOUTH -> new Vec(0, 0, 1);  // Positive Z
            case EAST -> new Vec(1, 0, 0);  // Positive X
            case WEST -> new Vec(-1, 0, 0); // Negative X
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
        return data.add(vec);
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