package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.area.holder.Placement;
import net.theevilreaper.tamias.common.event.GameAreaPrepareEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class GameAreaPrepareListener implements Consumer<GameAreaPrepareEvent> {

    private final Placement placement;

    public GameAreaPrepareListener(@NotNull Placement placement) {
        if (!(placement instanceof GamePlacement)) {
            throw new IllegalArgumentException("The placement must be an instance of GamePlacement");
        }
        this.placement = placement;
    }

    @Override
    public void accept(@NotNull GameAreaPrepareEvent event) {
        GamePlacement gamePlacement = (GamePlacement) placement;
        gamePlacement.flatten();
        gamePlacement.applyPositions();
    }
}
