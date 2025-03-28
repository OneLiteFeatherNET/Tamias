package net.theevilreaper.tamias.setup.event;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class MapSetupSelectEventIntegrationTest extends MapDataTestBase {

    @Test
    void testEventConstruction(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        MapSetupSelectEvent event = new MapSetupSelectEvent(player, testMapEntry, false);

        assertNotNull(event);
        assertEquals(player, event.getPlayer());
        assertEquals(testMapEntry, event.getMapEntry());
        assertFalse(event.isCancelled());

        event.setCancelled(true);

        assertTrue(event.isCancelled());

        env.destroyInstance(instance, true);
    }
}
