package net.theevilreaper.tamias.game.phase;

import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

import static net.minestom.server.MinecraftServer.getConnectionManager;

/**
 * The {@link RestartPhase} is a timed phase to handle the time before the server should be restarted.
 *
 * @author theEvilReaper
 * @version 1.0.1
 * @since 0.1.0
 **/
public final class RestartPhase extends TimedPhase {

    /**
     * Constructs a new {@link RestartPhase} with a duration of 15 seconds.
     */
    public RestartPhase() {
        super("Restart", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(15);
        this.setEndTicks(-1);
    }

    /**
     * Stops the server cleanly when the phase finishes.
     */
    @Override
    protected void onFinish() {
        MinecraftServer.stopCleanly();
    }

    /**
     * Called every tick to update the phase.
     * It broadcasts messages to players based on the current ticks.
     */
    @Override
    public void onUpdate() {
        switch (getCurrentTicks()) {
            case 10, 3, 2, 1 -> broadcast(GameMessages.getRestartTime(getCurrentTicks()));
            case 0 -> {
                for (Player onlinePlayer : getConnectionManager().getOnlinePlayers()) {
                    onlinePlayer.kick(GameMessages.GAME_END_KICK_MESSAGE);
                }
            }
            default -> {
                // Nothing to do here
            }
        }
    }

    /**
     * Broadcasts a message to all online players.
     *
     * @param component the message to broadcast
     */
    private void broadcast(@NotNull Component component) {
        Audience.audience(getConnectionManager().getOnlinePlayers())
                .sendMessage(component);
    }
}
