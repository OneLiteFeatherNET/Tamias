package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;

import java.util.function.Consumer;

public final class GameCleanupListener implements Consumer<GameCleanupEvent> {

    private final GamePlacement gamePlacement;

    public GameCleanupListener(GamePlacement gamePlacement) {
        this.gamePlacement = gamePlacement;
    }

    @Override
    public void accept(GameCleanupEvent event) {
        this.gamePlacement.clear();
    }
}
