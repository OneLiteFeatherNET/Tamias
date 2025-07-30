package net.theevilreaper.tamias.game.phase;

import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.tamias.common.round.event.RoundPrepareEvent;
import net.theevilreaper.tamias.game.util.phase.LobbyPhaseData;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.game.map.GameMapProvider;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

import static net.minestom.server.MinecraftServer.getConnectionManager;
import static net.theevilreaper.tamias.common.config.GameConfig.FORCE_START_TIME;

/**
 * The {@link LobbyPhase} is the first phase which runs in the game.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class LobbyPhase extends TimedPhase {

    private static final Sound PLING = Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BELL, Sound.Source.MASTER, 1.0f, 1.0f);
    private final MapProvider mapProvider;
    private final LobbyPhaseData phaseData;
    private boolean forceStarted;

    /**
     * Creates a new instance to this phase with the given values.
     *
     * @param mapProvider the provider to access map data
     * @param phaseData   the data for the phase to get specific values from it
     */
    public LobbyPhase(
            @NotNull MapProvider mapProvider,
            @NotNull LobbyPhaseData phaseData
    ) {
        super("Lobby", ChronoUnit.SECONDS, 1);
        this.setPaused(true);
        this.setCurrentTicks(phaseData.lobbyTime());
        this.setTickDirection(TickDirection.DOWN);
        this.mapProvider = mapProvider;
        this.phaseData = phaseData;
    }

    @Override
    public void start() {
        super.start();
        setLevel();
    }

    @Override
    protected void onFinish() {
        EventDispatcher.call(new RoundPrepareEvent());
    }

    @Override
    public void onUpdate() {
        setLevel();
        this.phaseData.timeUpdater().accept(getCurrentTicks());
        switch (getCurrentTicks()) {
            case 30, 20, 3, 1 -> broadcastTime();
            case 10 -> {
                this.broadcastTime();
                ((GameMapProvider) this.mapProvider).loadGameChunks();
            }
            case 5 -> {
                this.broadcastTime();
                //TODO: Add placement back
            }
            default -> {
                // Nothing to do here
            }
        }
    }

    /**
     * Sets the level of all online players to the current ticks.
     */
    private void setLevel() {
        this.setLevel(getCurrentTicks());
    }

    /**
     * Broadcasts the current time to all online players.
     * Plays a sound for each player to indicate the time update.
     */
    private void broadcastTime() {
        for (Player onlinePlayer : getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.sendMessage(GameMessages.getLobbyTime(getCurrentTicks()));
            onlinePlayer.playSound(PLING, onlinePlayer.getPosition());
        }
    }

    /**
     * Sets the level of all online players to the given amount.
     *
     * @param amount the level to set
     */
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
     * @param forceStarted true, if the phase is force started otherwise false
     */
    public void setForceStarted(boolean forceStarted) {
        if (forceStarted) {
            this.setCurrentTicks(FORCE_START_TIME);
        }
        this.forceStarted = forceStarted;
    }

    /**
     * Checks if the start condition is met, which is when the number of online players
     * is greater than or equal to the minimum number of players.
     */
    public void checkStartCondition() {
        if (isPaused() && getConnectionManager().getOnlinePlayers().size() >= this.phaseData.minPlayers()) {
            this.setPaused(false);
        }
    }

    /**
     * Checks if the stop condition is met, which is when the number of online players
     * minus one (the host) is less than or equal to the maximum number of players.
     */
    public void checkStopCondition() {
        if (getConnectionManager().getOnlinePlayers().size() - 1 <= this.phaseData.maxPlayers()) {
            this.setPaused(true);
            this.setCurrentTicks(this.phaseData.lobbyTime());
            setLevel();
        }
    }

    /**
     * Returns the right time of the phase depending on if it is force started or not.
     *
     * @return the time of the phase
     */
    private int getLobbyOrForceTime() {
        return isForceStarted() ? FORCE_START_TIME : this.phaseData.lobbyTime();
    }

    /**
     * Returns an indication if the phase is force started or not.
     *
     * @return true if the phase is force started otherwise false
     */
    public boolean isForceStarted() {
        return forceStarted;
    }
}
