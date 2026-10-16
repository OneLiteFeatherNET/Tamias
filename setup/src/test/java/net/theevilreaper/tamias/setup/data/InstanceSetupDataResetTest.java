package net.theevilreaper.tamias.setup.data;

import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.MapDataTestBase;
import net.theevilreaper.tamias.setup.util.SetupTags;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MicrotusExtension.class)
class InstanceSetupDataResetTest extends MapDataTestBase {

    @Test
    void testResetRemovesSetupTag(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());
        player.setTag(SetupTags.SETUP_TAG, (byte) 1);
        InstanceSetupData setupData = SetupDataFactory.create(player, testMapEntry, false);

        setupData.reset();

        assertFalse(player.hasTag(SetupTags.SETUP_TAG), "the setup tag must be removed so a new setup can be started");
    }
}
