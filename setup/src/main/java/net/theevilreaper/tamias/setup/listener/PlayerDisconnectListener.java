package net.theevilreaper.tamias.setup.listener;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerDisconnectListener implements Consumer<PlayerDisconnectEvent> {

    private final Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> dataRemover;

    public PlayerDisconnectListener(@NotNull Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> dataRemover) {
        this.dataRemover = dataRemover;
    }

    @Override
    public void accept(@NotNull PlayerDisconnectEvent event) {
        Player player = event.getPlayer();
        Component joinMessage = Messages.withPrefix(Component.text(player.getUsername(), NamedTextColor.AQUA))
                .append(Component.space())
                .append(Component.text("left the server", NamedTextColor.GRAY));
        Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                .sendMessage(joinMessage);

        Optional<InstanceSetupData<? extends BaseMap>> removedData = dataRemover.apply(player.getUuid());
        removedData.ifPresent(InstanceSetupData::reset);
    }
}
