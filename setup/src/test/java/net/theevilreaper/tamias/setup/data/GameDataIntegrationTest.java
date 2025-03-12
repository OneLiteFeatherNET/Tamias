package net.theevilreaper.tamias.setup.data;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class GameDataIntegrationTest extends MapDataTestBase {

    private static GameMap gameMap;

    @BeforeAll
    static void setup() {
        gameMap = new GameMap();
    }

    @AfterAll
    static void tearDown() {
        gameMap = null;
    }

    @Test
    void testGameSetupDataCreation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        assertNotNull(instance);
        assertNotNull(testPlayer);

        SetupData.Builder builder = SetupData.builder();

        builder.baseMap(gameMap)
                .player(testPlayer)
                .mapEntry(testMapEntry)
                .build();

        SetupData data = builder.build();

        assertNotNull(data);
        assertInstanceOf(GameData.class, data);
        assertEquals(testPlayer, data.getPlayer());
        assertEquals(testMapEntry, data.getMapEntry());

        env.destroyInstance(instance, true);
    }

    @Test
    void testAreaModeSwap(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        SetupData data = SetupData.builder()
                .player(testPlayer)
                .baseMap(gameMap)
                .mapEntry(testMapEntry)
                .build();

        assertNotNull(data);
        assertInstanceOf(GameData.class, data);
        assertFalse(data.hasAreaMode());
        data.swapAreaMode();
        assertTrue(data.hasAreaMode());

        env.destroyInstance(instance, true);
    }

    @Test
    void testGameAreaVecSet(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        GameData data = (GameData) SetupData.builder()
                .player(testPlayer)
                .baseMap(new GameMap())
                .mapEntry(testMapEntry)
                .build();

        assertNotNull(data);
        assertInstanceOf(GameData.class, data);

        data.swapAreaMode();

        assertTrue(data.hasAreaMode());
        data.setLeftCorner(Vec.ZERO);
        data.setRightCorner(new Vec(10, 10, 10));
        data.setDirection(Direction.NORTH);

        // Disable are mode to set the data to the map reference
        data.swapAreaMode();
        assertFalse(data.hasAreaMode());

        GameMap mapData = ((GameMap) data.getBaseMap());
        assertNotNull(mapData);

        GameAreaData gameAreaData  = mapData.getGameAreaData();

        assertNotNull(gameAreaData);
        assertEquals(Vec.ZERO, gameAreaData.lowerCorner());
        assertEquals(new Vec(10, 10, 10), gameAreaData.upperCorner());
        assertEquals(Direction.NORTH, gameAreaData.facing());

        env.destroyInstance(instance, true);
    }
}
