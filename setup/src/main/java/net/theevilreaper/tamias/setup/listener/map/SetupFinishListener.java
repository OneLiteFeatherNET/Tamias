package net.theevilreaper.tamias.setup.listener.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SetupFinishListener implements Consumer<SetupFinishEvent<InstanceSetupData<? extends BaseMap>>> {

    private final PlayerConsumer instanceSwitcher;

    public SetupFinishListener(@NotNull PlayerConsumer instanceSwitcher) {
        this.instanceSwitcher = instanceSwitcher;
    }

    @Override
    public void accept(@NotNull SetupFinishEvent<InstanceSetupData<? extends BaseMap>> event) {
        InstanceSetupData<? extends BaseMap> setupData = event.getData();

        setupData.save();
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(setupData.getId());
        this.instanceSwitcher.accept(player);
        setupData.reset();
    }
}
