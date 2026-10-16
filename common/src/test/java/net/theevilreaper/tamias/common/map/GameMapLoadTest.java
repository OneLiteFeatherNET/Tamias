package net.theevilreaper.tamias.common.map;

import com.google.gson.Gson;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameMapLoadTest {

    private static final String MAP_NAME = "test_map.json";

    @Test
    void testGameMapLoad() {
        Gson gson = GsonUtil.GSON;
        FileHandler fileHandler = new GsonFileHandler(gson);
        assertNotNull(fileHandler);

        Optional<GameMap> mapOptional = fileHandler.load(Paths.get("src/test/resources", MAP_NAME), GameMap.class);
        assertTrue(mapOptional.isPresent());

        GameMap gameMap = mapOptional.get();

        assertNotNull(gameMap);
        assertEquals("Test-Map", gameMap.name());
        assertEquals(1, gameMap.builders().size());
        assertEquals("OLF", gameMap.builders().getFirst());
        assertFalse(gameMap.hasGroundPresets());
        assertTrue(gameMap.getGroundPresets().isEmpty());
    }

    @Test
    void testGroundPresetsRoundTrip() {
        GameMap gameMap = new GameMapBuilder()
                .bomberSpawn(Pos.ZERO)
                .spawnLayerPos(Pos.ZERO)
                .spawnLayerDirection(Direction.EAST)
                .areaLowerCorner(Vec.ZERO)
                .areaUpperCorner(Vec.ONE)
                .addGroundPreset(new GroundData(Block.SAND, List.of(Block.RED_SAND)))
                .addGroundPreset(new GroundData(Block.ICE, null))
                .build();

        String json = GsonUtil.GSON.toJson(gameMap);
        GameMap parsed = GsonUtil.GSON.fromJson(json, GameMap.class);

        assertTrue(parsed.hasGroundPresets());
        assertEquals(gameMap.getGroundPresets(), parsed.getGroundPresets());
    }

    @Test
    void testEmptyGroundPresetsAreNotSerialized() {
        String json = GsonUtil.GSON.toJson(loadTestMap());
        assertFalse(json.contains("groundPresets"));
    }

    private static GameMap loadTestMap() {
        Optional<GameMap> mapOptional = GsonUtil.FILE_HANDLER.load(Paths.get("src/test/resources", MAP_NAME), GameMap.class);
        assertTrue(mapOptional.isPresent());
        return mapOptional.get();
    }
}
