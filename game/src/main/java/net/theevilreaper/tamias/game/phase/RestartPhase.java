package net.theevilreaper.tamias.game.phase;

import de.icevizion.xerus.api.phase.TimedPhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

import static net.minestom.server.MinecraftServer.getConnectionManager;
import static net.theevilreaper.tamias.game.util.GameMessages.MINI_MESSAGE;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class RestartPhase extends TimedPhase {

    private static final Component KICK_MESSAGE = MINI_MESSAGE.deserialize("<red>The game is over. Thanks for playing it. <3");

    public RestartPhase() {
        super("Restart", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(15);
        this.setEndTicks(-1);
    }

    @Override
    protected void onFinish() {
        MinecraftServer.stopCleanly();
    }

    @Override
    public void onUpdate() {
        switch (getCurrentTicks()) {
            case 10, 3, 2, 1 -> broadcast(GameMessages.getLobbyTime(getCurrentTicks()));
            case 0 -> {
                for (Player onlinePlayer : getConnectionManager().getOnlinePlayers()) {
                    onlinePlayer.kick(KICK_MESSAGE);
                }
            }
            default -> {
                // Nothing to do here
            }
        }
    }

    private void broadcast(@NotNull Component component) {
        Audience.audience(getConnectionManager().getOnlinePlayers())
                .sendMessage(component);
    }
}
