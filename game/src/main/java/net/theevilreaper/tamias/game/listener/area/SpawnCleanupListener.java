package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.event.SpawnCleanupEvent;

import java.util.function.Consumer;

public final class SpawnCleanupListener implements Consumer<SpawnCleanupEvent> {

    private final SpawnPlacement spawnPlacement;

    public SpawnCleanupListener(SpawnPlacement spawnPlacement) {
        this.spawnPlacement = spawnPlacement;
    }

    @Override
    public void accept(SpawnCleanupEvent event) {
        this.spawnPlacement.clear();
    }
}
