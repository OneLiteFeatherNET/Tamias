package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;

import java.util.function.Consumer;

public final class AreaSpawnListener implements Consumer<AreaSpawnTriggerEvent> {

    private final SpawnPlacement spawnPlacement;

    public AreaSpawnListener(SpawnPlacement spawnPlacement) {
        this.spawnPlacement = spawnPlacement;
    }

    @Override
    public void accept(AreaSpawnTriggerEvent event) {
        this.spawnPlacement.triggerPlacement(event.data());
    }
}
