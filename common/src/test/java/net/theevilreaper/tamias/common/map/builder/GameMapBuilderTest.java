package net.theevilreaper.tamias.common.map.builder;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.GameMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameMapBuilderTest {

    @Test
    void testGameMapBuilder() {
        GameMapBuilder builder = new GameMapBuilder();
        assertNotNull(builder);

        builder.bomberSpawn(new Pos(0, 64, 0))
                .name("Test Map")
                .spawn(new Pos(0, 64, 0))
                .builders("Author1", "Author2");

        assertEquals("Test Map", builder.getName());
        assertEquals(new Pos(0, 64, 0), builder.getSpawn());
        assertEquals(new Pos(0, 64, 0), builder.getBomberInitialSpawn());
        assertNotNull(builder.getBuilders());
        assertTrue(builder.getBuilders().contains("Author1"));
        assertTrue(builder.getBuilders().contains("Author2"));

        // Test area related methods
        builder.areaUpperCorner(Vec.ONE).areaLowerCorner(Vec.ZERO).areaFacing(Direction.EAST);

        assertEquals(Direction.EAST, builder.getAreaDataBuilder().facing());
        assertEquals(Vec.ZERO, builder.getAreaDataBuilder().lowerCorner());
        assertEquals(Vec.ONE, builder.getAreaDataBuilder().upperCorner());

        // Test spawn layer related methods
        builder.spawnLayerDirection(Direction.EAST).spawnLayerPos(Pos.ZERO);

        assertEquals(Direction.EAST, builder.getSpawnLayerBuilder().getDirection());
        assertEquals(Pos.ZERO, builder.getSpawnLayerBuilder().getPos());

        // Test map creation
        GameMap gameMap = builder.build();
        assertNotNull(gameMap);
        assertEquals("Test Map", gameMap.name());
        assertEquals(new Pos(0, 64, 0), gameMap.spawn());
        assertEquals(new Pos(0, 64, 0), gameMap.getBomberInitialSpawn());
        assertNotNull(gameMap.getGameAreaData());
        assertEquals(Direction.EAST, gameMap.getGameAreaData().facing());
        assertEquals(Vec.ZERO, gameMap.getGameAreaData().lowerCorner());
        assertEquals(Vec.ONE, gameMap.getGameAreaData().upperCorner());
        assertNotNull(gameMap.getSpawnData());
        assertEquals(Direction.EAST, gameMap.getSpawnData().direction());
        assertEquals(Pos.ZERO, gameMap.getSpawnData().pos());
        assertNotNull(gameMap.builders());
        assertEquals(new Pos(0, 64, 0), gameMap.getBomberInitialSpawn());
    }

    @Test
    void testGroundPresetHandling() {
        GroundData sand = new GroundData(Block.SAND, List.of(Block.RED_SAND));
        GroundData ice = new GroundData(Block.ICE, null);
        GroundData snow = new GroundData(Block.SNOW_BLOCK, null);

        GameMapBuilder builder = completeBuilder().addGroundPreset(sand).addGroundPreset(ice);
        assertEquals(List.of(sand, ice), builder.getGroundPresets());

        builder.setGroundPreset(1, snow);
        assertEquals(List.of(sand, snow), builder.getGroundPresets());

        builder.removeGroundPreset(0);
        assertEquals(List.of(snow), builder.getGroundPresets());
        assertThrows(UnsupportedOperationException.class, () -> builder.getGroundPresets().add(ice));

        GameMap gameMap = builder.build();
        assertTrue(gameMap.hasGroundPresets());
        assertEquals(List.of(snow), gameMap.getGroundPresets());

        GameMapBuilder copy = new GameMapBuilder(gameMap);
        assertEquals(List.of(snow), copy.getGroundPresets());
    }

    @Test
    void testMapWithoutGroundPresets() {
        GameMap gameMap = completeBuilder().build();
        assertFalse(gameMap.hasGroundPresets());
        assertTrue(gameMap.getGroundPresets().isEmpty());
    }

    private static GameMapBuilder completeBuilder() {
        return new GameMapBuilder()
                .bomberSpawn(Pos.ZERO)
                .spawnLayerPos(Pos.ZERO)
                .spawnLayerDirection(Direction.EAST)
                .areaLowerCorner(Vec.ZERO)
                .areaUpperCorner(Vec.ONE);
    }
}
