package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnAreaTest {

    @ParameterizedTest(name = "Test invalid spawn area argument usage with face {0}")
    @ValueSource(strings = {"DOWN", "UP"})
    void testInvalidSpawnAreaArgumentUsage(String face) {
        Optional<Direction> directionOptional = Arrays.stream(Direction.values()).filter(value -> value.name().equals(face)).findFirst();
        assertTrue(directionOptional.isPresent());

        Direction direction = directionOptional.get();
        SpawnLayer spawnLayer = new SpawnLayer(Pos.ZERO, direction);
        assertThrows(
                IllegalArgumentException.class,
                () -> new SpawnArea(spawnLayer, 10),
                "The direction must be horizontal"
        );
    }

    @Test
    void testInvalidPositionUsage() {
        SpawnLayer spawnLayer = new SpawnLayer(Pos.ZERO, Direction.UP);
        assertThrows(
                IllegalArgumentException.class,
                () -> new SpawnArea(spawnLayer, 0),
                "The direction must be horizontal"
        );
    }
}
