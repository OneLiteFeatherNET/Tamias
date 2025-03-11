package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.map.GameMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class SetupDataTest {

    private MapEntry testEntry;

    @BeforeEach
    void setUp() {
        Path testMap = Paths.get(getClass().getClassLoader().getResource("TamiasMap").getPath());
        if (!Files.exists(testMap)) {
            throw new IllegalStateException("Test map not found");
        }
        testEntry = MapEntry.of(testMap);
    }

    @AfterEach
    void tearDown() {
        testEntry = null;
    }

    @Test
    void testLobbySetupDataCreation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        assertNotNull(instance);
        assertNotNull(testPlayer);

        SetupData.Builder builder = SetupData.builder();

        builder.baseMap(new BaseMap())
                .player(testPlayer)
                .mapEntry(testEntry)
                .build();

        SetupData data = builder.build();

        assertNotNull(data);
        assertInstanceOf(LobbyData.class, data);
        assertEquals(testPlayer, data.getPlayer());
        assertEquals(testEntry, data.getMapEntry());

        env.destroyInstance(instance, true);
    }

    @Test
    void testGameSetupDataCreation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        assertNotNull(instance);
        assertNotNull(testPlayer);

        SetupData.Builder builder = SetupData.builder();

        builder.baseMap(new GameMap())
                .player(testPlayer)
                .mapEntry(testEntry)
                .build();

        SetupData data = builder.build();

        assertNotNull(data);
        assertInstanceOf(GameData.class, data);
        assertEquals(testPlayer, data.getPlayer());
        assertEquals(testEntry, data.getMapEntry());

        env.destroyInstance(instance, true);
    }

    @Test
    void testAreaModeSwap(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        SetupData data = SetupData.builder()
                .player(testPlayer)
                .baseMap(new GameMap())
                .mapEntry(testEntry)
                .build();

        assertNotNull(data);
        assertInstanceOf(GameData.class, data);
        assertFalse(data.hasAreaMode());
        data.swapAreaMode();
        assertTrue(data.hasAreaMode());

        env.destroyInstance(instance, true);
    }
}