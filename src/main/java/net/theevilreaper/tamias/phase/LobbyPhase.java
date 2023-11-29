package net.theevilreaper.tamias.phase;

import de.icevizion.xerus.api.phase.TickDirection;
import de.icevizion.xerus.api.phase.TimedPhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.time.temporal.ChronoUnit;

import static net.theevilreaper.tamias.config.GameConfig.*;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class LobbyPhase extends TimedPhase {

    private boolean forceStarted;

    public LobbyPhase() {
        super("Lobby", ChronoUnit.SECONDS, 1);
        this.setPaused(true);
        this.setCurrentTicks(LOBBY_PHASE_TIME);
        this.setTickDirection(TickDirection.DOWN);
        this.setEndTicks(-5);
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
    }

    private void setLevel() {
        this.setLevel(getCurrentTicks());
    }

    private void setLevel(int amount) {
        if (amount < 0) return;
        float currentExpCount = (float) this.getCurrentTicks() / (isForceStarted() ? FORCE_START_TIME : LOBBY_PHASE_TIME);
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.setLevel(amount);
            onlinePlayer.setExp(currentExpCount);
        }
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
