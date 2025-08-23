package net.theevilreaper.tamias.game.listener;

import net.minestom.server.entity.Player;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.xerus.api.phase.Phase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minestom.server.MinecraftServer.getConnectionManager;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayerSpawnListener implements Consumer<PlayerSpawnEvent> {

    private final Supplier<Phase> phaseSupplier;
    private final PlayerConsumer spawnConsumer;
    private final PlayerConsumer scoreboardConsumer;

    /**
     * Constructs a new PlayerSpawnListener.
     *
     * @param phaseSupplier      a supplier for the current game phase
     * @param spawnConsumer      a consumer that handles player spawning logic
     * @param scoreboardConsumer a consumer that handles scoreboard updates for the player
     */
    public PlayerSpawnListener(
            @NotNull Supplier<Phase> phaseSupplier,
            @NotNull PlayerConsumer spawnConsumer,
            @NotNull PlayerConsumer scoreboardConsumer
    ) {
        this.phaseSupplier = phaseSupplier;
        this.spawnConsumer = spawnConsumer;
        this.scoreboardConsumer = scoreboardConsumer;
    }

    @Override
    public void accept(@NotNull PlayerSpawnEvent event) {
        Player player = event.getPlayer();
        player.setDisplayName(Component.text(player.getUsername()));

        Phase phase = phaseSupplier.get();

        if (event.isFirstSpawn() && phase instanceof LobbyPhase lobbyPhase) {
            Audience.audience(getConnectionManager().getOnlinePlayers()).sendMessage(GameMessages.getJoinMessage(player));
            lobbyPhase.updatePlayerValues(player);
            lobbyPhase.checkStartCondition();
            this.spawnConsumer.accept(player);
            this.scoreboardConsumer.accept(player);
        }
    }
}
