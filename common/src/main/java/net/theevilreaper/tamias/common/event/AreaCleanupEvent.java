package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public record AreaCleanupEvent(boolean lobby) implements Event {
}
