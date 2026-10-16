package net.theevilreaper.tamias.setup.listener.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.onelitefeather.guira.data.SetupData;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class SetupFinishListener implements Consumer<SetupFinishEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupFinishListener.class);

    private final PlayerConsumer instanceSwitcher;
    private final Function<UUID, Optional<SetupData>> dataRemover;

    public SetupFinishListener(PlayerConsumer instanceSwitcher, Function<UUID, Optional<SetupData>> dataRemover) {
        this.instanceSwitcher = instanceSwitcher;
        this.dataRemover = dataRemover;
    }

    @Override
    public void accept(SetupFinishEvent event) {
        if (!(event.getData() instanceof InstanceSetupData instanceSetupData)) return;
        instanceSetupData.save();
        Player player = instanceSetupData.getPlayer();
        this.instanceSwitcher.accept(player);
        instanceSetupData.reset();
        this.dataRemover.apply(instanceSetupData.getId());
    }
}
