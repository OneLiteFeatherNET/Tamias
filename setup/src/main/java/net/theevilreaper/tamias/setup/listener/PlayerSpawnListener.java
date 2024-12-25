package net.theevilreaper.tamias.setup.listener;

import de.icevizion.aves.util.functional.PlayerConsumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.theevilreaper.tamias.common.util.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerSpawnListener implements Consumer<PlayerSpawnEvent> {

    private final PlayerConsumer spawnSupplier;

    public PlayerSpawnListener(@NotNull PlayerConsumer spawnSupplier) {
        this.spawnSupplier = spawnSupplier;
    }

    @Override
    public void accept(@NotNull PlayerSpawnEvent event) {
        Player player = event.getPlayer();

        if (event.isFirstSpawn()) {
            Component joinMessage = Messages.withPrefix(Component.text(player.getUsername(), NamedTextColor.AQUA))
                    .append(Component.space())
                    .append(Component.text("joined the server", NamedTextColor.GRAY));
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(joinMessage);
            this.spawnSupplier.accept(player);
        }
    }
}
