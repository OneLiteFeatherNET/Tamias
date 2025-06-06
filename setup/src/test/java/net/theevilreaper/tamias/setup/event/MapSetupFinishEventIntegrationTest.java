package net.theevilreaper.tamias.setup.event;

import net.theevilreaper.aves.map.BaseMap;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import net.theevilreaper.tamias.setup.data.SetupData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class MapSetupFinishEventIntegrationTest extends MapDataTestBase {

    @Test
    void testEventConstruction(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        SetupData setupData = SetupData.builder()
                .player(player)
                .mapEntry(testMapEntry)
                .baseMap(new BaseMap())
                .build();

        MapSetupFinishEvent mapSetupFinishEvent = new MapSetupFinishEvent(setupData);

        assertNotNull(mapSetupFinishEvent);
        assertEquals(setupData, mapSetupFinishEvent.setupData());

        env.destroyInstance(instance, true);
    }
}
