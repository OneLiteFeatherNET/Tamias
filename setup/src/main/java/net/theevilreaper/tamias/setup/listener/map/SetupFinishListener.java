package net.theevilreaper.tamias.setup.listener.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.onelitefeather.guira.data.SetupData;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.tamias.setup.util.SetupTags;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class SetupFinishListener implements Consumer<SetupFinishEvent> {

    private final PlayerConsumer instanceSwitcher;
    private final Function<UUID, Optional<SetupData>> dataRemover;

    public SetupFinishListener(PlayerConsumer instanceSwitcher, Function<UUID, Optional<SetupData>> dataRemover) {
        this.instanceSwitcher = instanceSwitcher;
        this.dataRemover = dataRemover;
    }

    @Override
    public void accept(SetupFinishEvent event) {
        SetupData setupData = event.getData();

        setupData.save();
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(setupData.getId());
        this.instanceSwitcher.accept(player);
        setupData.reset();

        this.dataRemover.apply(setupData.getId());
        if (player != null) {
            player.removeTag(SetupTags.SETUP_TAG);
        }
    }
}
