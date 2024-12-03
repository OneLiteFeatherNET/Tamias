package net.theevilreaper.tamias.common.map;

import com.google.gson.Gson;
import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("Test-Map", gameMap.getName());
        assertEquals(1, gameMap.getBuilders().length);
        assertEquals("OLF", gameMap.getBuilders()[0]);
    }
}
