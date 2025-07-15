package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link AreaSpawnTriggerEvent} is triggered when the area spawn is triggered.
 * This event is used to trigger the placement of the area spawn.
 *
 * @param data the ground data
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public record AreaSpawnTriggerEvent(@NotNull GroundData data) implements Event {

    /**
     * Creates a new {@link AreaSpawnTriggerEvent} with the default spawn data.
     */
    public static @NotNull AreaSpawnTriggerEvent empty() {
        return new AreaSpawnTriggerEvent(GroundDataRegistry.DEFAULT_SPAWN_DATA);
    }

}
