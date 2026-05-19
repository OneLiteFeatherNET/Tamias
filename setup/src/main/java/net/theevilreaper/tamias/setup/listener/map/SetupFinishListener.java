package net.theevilreaper.tamias.setup.listener.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.onelitefeather.guira.data.SetupData;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.util.functional.PlayerConsumer;

import java.util.function.Consumer;

public class SetupFinishListener implements Consumer<SetupFinishEvent> {

    private final PlayerConsumer instanceSwitcher;

    public SetupFinishListener(PlayerConsumer instanceSwitcher) {
        this.instanceSwitcher = instanceSwitcher;
    }

    @Override
    public void accept(SetupFinishEvent event) {
        SetupData setupData = event.getData();

        setupData.save();
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(setupData.getId());
        this.instanceSwitcher.accept(player);
        setupData.reset();
    }
}
