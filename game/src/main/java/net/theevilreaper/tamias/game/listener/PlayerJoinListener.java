package net.theevilreaper.tamias.game.listener;

import de.icevizion.xerus.api.phase.Phase;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayerJoinListener implements Consumer<AsyncPlayerConfigurationEvent> {

    private static final Component KICK_COMPONENT = GameMessages.withMini("<red>The game has already started! Unable to join!");
    private final Supplier<Integer> maxPlayers;
    private final Supplier<@Nullable Phase> phaseSupplier;
    private final Supplier<@NotNull Instance> instanceSupplier;

    public PlayerJoinListener(@NotNull Supplier<Integer> maxPlayers, @NotNull Supplier<Phase> phaseSupplier, @NotNull Supplier<Instance> instanceSupplier) {
        this.maxPlayers = maxPlayers;
        this.phaseSupplier = phaseSupplier;
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public void accept(@NotNull AsyncPlayerConfigurationEvent event) {
        Player player = event.getPlayer();
        if (MinecraftServer.getConnectionManager().getOnlinePlayers().size() >= maxPlayers.get()) {
            player.kick("NOPE");
            return;
        }

        Phase phase = phaseSupplier.get();
        if (phase == null) return;

        if (!(phase instanceof LobbyPhase)) {
            player.kick(KICK_COMPONENT);
            return;
        }

        event.setSpawningInstance(instanceSupplier.get());
    }
}
