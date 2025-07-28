package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpawnLayerBuilderTest {

    @Test
    void testSpawnLayerBuilder() {
        SpawnLayer.Builder builder = SpawnLayer.builder();

        builder.direction(Direction.DOWN).pos(Pos.ZERO);

        assertEquals(Direction.DOWN, builder.getDirection());
        assertEquals(Pos.ZERO, builder.getPos());

        SpawnLayer spawnLayer = builder.build();

        assertNotNull(spawnLayer);
        assertEquals(Direction.DOWN, spawnLayer.direction());
        assertEquals(Pos.ZERO, spawnLayer.pos());
    }

    @Test
    void testSpawnLayerBuilderWithExistingLayer() {
        SpawnLayer existingLayer = new SpawnLayer(Pos.ZERO, Direction.DOWN);
        SpawnLayer.Builder builder = SpawnLayer.builder(existingLayer);

        assertEquals(Direction.DOWN, builder.getDirection());
        assertEquals(Pos.ZERO, builder.getPos());

        SpawnLayer spawnLayer = builder.build();

        assertNotNull(spawnLayer);
        assertEquals(Direction.DOWN, spawnLayer.direction());
        assertEquals(Pos.ZERO, spawnLayer.pos());
    }

    @Test
    void testSpawnLayerBuilderThrowsExceptionOnNullPos() {
        SpawnLayer.Builder builder = SpawnLayer.builder();

        assertThrowsExactly(
                IllegalStateException.class,
                builder::build,
                "Position is required for SpawnLayer"
        );
    }

    @Test
    void testSpawnLayerBuilderThrowsExceptionOnNullDirection() {
        SpawnLayer.Builder builder = SpawnLayer.builder().pos(Pos.ZERO);

        assertThrowsExactly(
                IllegalStateException.class,
                builder::build,
                "Direction is required for SpawnLayer"
        );
    }
}
