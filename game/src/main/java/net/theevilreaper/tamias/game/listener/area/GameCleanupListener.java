package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class GameCleanupListener implements Consumer<GameCleanupEvent> {

    private final GamePlacement gamePlacement;
    private final BooleanSupplier lastRound;

    /**
     * Creates a new {@link GameCleanupListener} which clears the game area after a round.
     *
     * @param gamePlacement the placement of the game area to clear
     * @param lastRound     checks if the finished round was the last one, the ground stays after the last round
     */
    public GameCleanupListener(GamePlacement gamePlacement, BooleanSupplier lastRound) {
        this.gamePlacement = gamePlacement;
        this.lastRound = lastRound;
    }

    @Override
    public void accept(GameCleanupEvent event) {
        if (this.lastRound.getAsBoolean()) return;
        this.gamePlacement.clear();
    }
}
