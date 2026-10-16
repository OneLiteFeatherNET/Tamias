package net.theevilreaper.tamias.setup.listener.map;

import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.onelitefeather.guira.SetupDataService;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import net.theevilreaper.tamias.setup.data.GameData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class SetupFinishListenerTest extends MapDataTestBase {

    @Test
    void testSetupFinishHandling(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());

        SetupDataService dataService = SetupDataService.create();
        FakeSetupData setupData = new FakeSetupData(player, testMapEntry);
        dataService.add(player.getUuid(), setupData);

        AtomicBoolean switched = new AtomicBoolean(false);
        SetupFinishListener listener = new SetupFinishListener(p -> switched.set(true), dataService::remove);

        listener.accept(new SetupFinishEvent(setupData));

        assertTrue(setupData.saved, "save() must be called on finish");
        assertTrue(setupData.reset, "reset() must be called on finish");
        assertTrue(switched.get(), "the player must be switched back to another instance");
        assertTrue(dataService.get(player.getUuid()).isEmpty(), "the stale setup data entry must be removed from the service");
    }

    private static final class FakeSetupData extends GameData {

        private boolean saved;
        private boolean reset;

        private FakeSetupData(Player player, MapEntry mapEntry) {
            super(player, mapEntry);
        }

        @Override
        public void save() {
            this.saved = true;
        }

        @Override
        public void reset() {
            this.reset = true;
        }
    }
}
