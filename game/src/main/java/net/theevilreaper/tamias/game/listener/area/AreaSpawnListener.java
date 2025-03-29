package net.theevilreaper.tamias.game.listener.area;

import net.theevilreaper.tamias.common.area.holder.Placement;
import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class AreaSpawnListener implements Consumer<AreaSpawnTriggerEvent> {

    private final Placement spawnPlacement;

    public AreaSpawnListener(@NotNull Placement spawnPlacement) {
        if (!(spawnPlacement instanceof SpawnPlacement)) {
            throw new IllegalArgumentException("The spawnPlacement must be an instance of SpawnPlacement");
        }
        this.spawnPlacement = spawnPlacement;
    }

    @Override
    public void accept(@NotNull AreaSpawnTriggerEvent event) {
        this.spawnPlacement.triggerPlacement();
    }
}
