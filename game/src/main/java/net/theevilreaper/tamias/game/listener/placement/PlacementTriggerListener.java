package net.theevilreaper.tamias.game.listener.placement;

import net.theevilreaper.tamias.common.area.holder.Placement;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import net.theevilreaper.tamias.game.event.placement.TriggerPlacementEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlacementTriggerListener implements Consumer<TriggerPlacementEvent> {

    private final Placement spawnPlacement;
    private final Placement gamePlacement;

    /**
     * Creates a new {@link PlacementTriggerListener} that handles placement trigger events.
     *
     * @param spawnPlacement the placement for the spawn area
     * @param gamePlacement  the placement for the game area
     */
    public PlacementTriggerListener(@NotNull Placement spawnPlacement, @NotNull Placement gamePlacement) {
        this.spawnPlacement = spawnPlacement;
        this.gamePlacement = gamePlacement;
    }

    @Override
    public void accept(@NotNull TriggerPlacementEvent event) {
        if (event.type() == TriggerPlacementEvent.Type.SPAWN) {
            this.spawnPlacement.triggerPlacement(GroundDataRegistry.DEFAULT_SPAWN_DATA);
            return;
        }
        GroundData randomData = GroundDataRegistry.instance().getRandomData();
        this.gamePlacement.triggerPlacement(randomData);
    }
}
