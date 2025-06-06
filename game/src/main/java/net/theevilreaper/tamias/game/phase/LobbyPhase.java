package net.theevilreaper.tamias.game.phase;

import net.theevilreaper.aves.map.MapProvider;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.game.map.GameMapProvider;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.IntConsumer;

import static net.minestom.server.MinecraftServer.getConnectionManager;
import static net.theevilreaper.tamias.common.config.GameConfig.FORCE_START_TIME;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class LobbyPhase extends TimedPhase {

    private static final Sound PLING = Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BELL, Sound.Source.MASTER, 1.0f, 1.0f);
    private final MapProvider mapProvider;
    private final VoidConsumer roundUpdateTrigger;
    private final int minPlayers;
    private final int maxPlayers;
    private final int lobbyPhaseTime;
    private final IntConsumer timeUpdater;
    private boolean forceStarted;

    public LobbyPhase(
            @NotNull MapProvider mapProvider,
            @NotNull IntConsumer timeUpdater,
            @NotNull VoidConsumer roundUpdateTrigger,
            int minPlayers,
            int maxPlayers,
            int lobbyPhaseTime
    ) {
        super("Lobby", ChronoUnit.SECONDS, 1);
        this.setPaused(true);
        this.setCurrentTicks(lobbyPhaseTime);
        this.setTickDirection(TickDirection.DOWN);
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.mapProvider = mapProvider;
        this.lobbyPhaseTime = lobbyPhaseTime;
        this.timeUpdater = timeUpdater;
        this.roundUpdateTrigger = roundUpdateTrigger;
    }

    @Override
    public void start() {
        super.start();
        setLevel();
    }

    @Override
    protected void onFinish() {
        this.roundUpdateTrigger.apply();
    }

    @Override
    public void onUpdate() {
        setLevel();
        this.timeUpdater.accept(getCurrentTicks());

        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;
        switch (getCurrentTicks()) {
            case 30, 20, 3, 1 -> broadcastTime();
            case 10 -> {
                this.broadcastTime();
                gameMapProvider.loadGameChunks();
            }
            case 5 -> {
                this.broadcastTime();
                gameMapProvider.triggerSpawnPlacement();
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
        for (Player onlinePlayer : getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(GameMessages.getLobbyTime(getCurrentTicks()));
            onlinePlayer.playSound(PLING, onlinePlayer.getPosition());
        }
    }

    private void setLevel(int amount) {
        if (amount < 0) return;
        float currentExpCount = (float) this.getCurrentTicks() / getLobbyOrForceTime();
        for (Player onlinePlayer : getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.setLevel(amount);
            onlinePlayer.setExp(currentExpCount);
        }
    }

    /**
     * Updates some data to display the current time.
     *
     * @param player the player who should receive the update
     */
    public void updatePlayerValues(@NotNull Player player) {
        player.setLevel(getCurrentTicks());
        float currentExpCount = (float) this.getCurrentTicks() / getLobbyOrForceTime();
        player.setExp(currentExpCount);
    }

    /**
     * Updates the state which indicates, if the phase is force started or not.
     *
     * @param forceStarted true if the phase is force started otherwise false
     */
    public void setForceStarted(boolean forceStarted) {
        if (forceStarted) {
            this.setCurrentTicks(FORCE_START_TIME);
        }
        this.forceStarted = forceStarted;
    }

    public void checkStartCondition() {
        if (isPaused() && getConnectionManager().getOnlinePlayers().size() >= this.minPlayers) {
            this.setPaused(false);
        }
    }

    public void checkStopCondition() {
        if (getConnectionManager().getOnlinePlayers().size() - 1 <= this.maxPlayers) {
            this.setPaused(true);
            this.setCurrentTicks(this.lobbyPhaseTime);
            setLevel();
        }
    }

    private int getLobbyOrForceTime() {
        return isForceStarted() ? FORCE_START_TIME : this.lobbyPhaseTime;
    }

    public boolean isForceStarted() {
        return forceStarted;
    }
}
