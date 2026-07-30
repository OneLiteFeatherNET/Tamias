package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.event.GameAreaChunksReadyEvent;

import java.util.function.Consumer;

public final class GameAreaPrepareListener implements Consumer<GameAreaChunksReadyEvent> {

    private final GamePlacement gamePlacement;

    public GameAreaPrepareListener(GamePlacement gamePlacement) {
        this.gamePlacement = gamePlacement;
    }

    @Override
    public void accept(GameAreaChunksReadyEvent event) {
        this.gamePlacement.flatten();
        this.gamePlacement.applyPositions();
    }
}
