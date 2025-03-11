package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@ExtendWith(MicrotusExtension.class)
class LobbyDataIntegrationTest extends MapDataTestBase {

    private static BaseMap lobbyMap;

    @BeforeAll
    static void setup() {
        lobbyMap = new BaseMap();
    }

    @AfterEach
    void tearDown() {
        lobbyMap = null;
    }

    @Test
    void testLobbySetupDataCreation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        assertNotNull(instance);
        assertNotNull(testPlayer);

        SetupData.Builder builder = SetupData.builder();

        builder.baseMap(lobbyMap).player(testPlayer).mapEntry(testMapEntry).build();

        SetupData data = builder.build();

        assertNotNull(data);
        assertInstanceOf(LobbyData.class, data);
        assertEquals(testPlayer, data.getPlayer());
        assertEquals(testMapEntry, data.getMapEntry());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSwapAreaModeRaisesUnsupportedOperationException(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        SetupData lobbyData = SetupData
                .builder()
                .player(player)
                .mapEntry(testMapEntry)
                .baseMap(lobbyMap)
                .build();

        assertInstanceOf(LobbyData.class, lobbyData);

        assertThrowsExactly(
                UnsupportedOperationException.class,
                lobbyData::swapAreaMode,
                "A LobbyData can't swap the area mode"
        );

        env.destroyInstance(instance, true);
    }
}
