package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import net.theevilreaper.tamias.common.area.Area;

/**
 * The event will be fired to indicate that a {@link Area} needs to be cleaned up.
 *
 * @param isSpawn if the area is the spawn area or not
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record AreaCleanupEvent(boolean isSpawn) implements Event {
}
