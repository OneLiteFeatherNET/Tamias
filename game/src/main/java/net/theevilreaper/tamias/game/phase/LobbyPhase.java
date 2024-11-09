package net.theevilreaper.tamias.game.phase;

import de.icevizion.xerus.api.phase.TickDirection;
import de.icevizion.xerus.api.phase.TimedPhase;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.game.util.Messages;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static net.theevilreaper.tamias.game.config.GameConfig.FORCE_START_TIME;
import static net.theevilreaper.tamias.game.config.GameConfig.LOBBY_PHASE_TIME;
import static net.theevilreaper.tamias.game.config.GameConfig.MIN_PLAYERS;


/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class LobbyPhase extends TimedPhase {

    private static final Sound PLING = Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BELL, Sound.Source.MASTER, 1.0f, 1.0f);
    private final MapProvider provider;
    private boolean forceStarted;

    public LobbyPhase(@NotNull MapProvider provider) {
        super("Lobby", ChronoUnit.SECONDS, 1);
        this.setPaused(true);
        this.setCurrentTicks(LOBBY_PHASE_TIME);
        this.setTickDirection(TickDirection.DOWN);
        this.setEndTicks(-5);
        this.provider = provider;
    }


    @Override
    public void start() {
        super.start();
        setLevel();
    }

    @Override
    protected void onFinish() {

    }

    @Override
    public void onUpdate() {
        setLevel();

        switch (getCurrentTicks()) {
            case 30, 3, 1 -> broadcastTime();
            case 20 -> {
                broadcastTime();
                this.provider.loadGameMap();
            }
            case 10 -> {
                this.broadcastTime();
                this.provider.loadGameChunks();
            }
            case 2 -> {
                this.broadcastTime();
                this.provider.getSpawnArea().spawnBlocks();
            }
            case 0 -> {
                this.provider.teleportPlayers(new ArrayList<>(MinecraftServer.getConnectionManager().getOnlinePlayers()));
            }
            default -> {
                // Nothing to do here
            }
        }

    }

    private void setLevel() {
        this.setLevel(getCurrentTicks());
    }

    private void broadcastTime() {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(Messages.getLobbyTime(getCurrentTicks()));
            onlinePlayer.playSound(PLING, onlinePlayer.getPosition());
        }
    }

    private void setLevel(int amount) {
        if (amount < 0) return;
        float currentExpCount = (float) this.getCurrentTicks() / (isForceStarted() ? FORCE_START_TIME : LOBBY_PHASE_TIME);
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.setLevel(amount);
            onlinePlayer.setExp(currentExpCount);
        }
    }

    public void updatePlayerValues(@NotNull Player player) {
        player.setLevel(getCurrentTicks());
        float currentExpCount = (float) this.getCurrentTicks() / (isForceStarted() ? FORCE_START_TIME : LOBBY_PHASE_TIME);
        player.setExp(currentExpCount);
    }

    public void setForceStarted(boolean forceStarted) {
        if (forceStarted) {
            this.setCurrentTicks(FORCE_START_TIME);
        }
        this.forceStarted = forceStarted;
    }

    public void checkStartCondition() {
        if (isPaused() && MinecraftServer.getConnectionManager().getOnlinePlayers().size() >= MIN_PLAYERS) {
            this.setPaused(false);
        }
    }

    public void checkStopCondition() {
        if (MinecraftServer.getConnectionManager().getOnlinePlayers().size() - 1 <= MIN_PLAYERS) {
            this.setPaused(true);
            this.setCurrentTicks(LOBBY_PHASE_TIME);
            setLevel();
        }
    }

    public boolean isForceStarted() {
        return forceStarted;
    }
}
