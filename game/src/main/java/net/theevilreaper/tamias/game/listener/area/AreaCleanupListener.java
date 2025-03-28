package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.Placement;
import net.theevilreaper.tamias.common.event.AreaCleanupEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class AreaCleanupListener implements Consumer<AreaCleanupEvent> {

    private final Placement spawnPlacement;
    private final Placement gamePlacement;

    public AreaCleanupListener(@NotNull Placement spawnPlacement, @NotNull Placement gamePlacement) {
        this.spawnPlacement = spawnPlacement;
        this.gamePlacement = gamePlacement;
    }

    @Override
    public void accept(@NotNull AreaCleanupEvent event) {
        if (event.isSpawn()) {
            this.spawnPlacement.clear();
            return;
        }

        this.gamePlacement.clear();
    }
}
