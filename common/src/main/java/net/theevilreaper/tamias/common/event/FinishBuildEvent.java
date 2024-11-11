package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * The event is called when the ground has been finished building.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see Event
 * @since 1.0.0
 */
@ApiStatus.NonExtendable
public record FinishBuildEvent() implements Event { }
