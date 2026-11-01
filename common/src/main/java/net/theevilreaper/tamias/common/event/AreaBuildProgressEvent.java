package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * The event is called after each step while the ground is building up.
 *
 * @param progress the share of placed ground blocks from 0 to 1
 * @author theEvilReaper
 * @version 1.0.0
 * @see AreaFinishBuildEvent
 * @since 1.0.0
 */
@ApiStatus.NonExtendable
public record AreaBuildProgressEvent(double progress) implements Event { }
