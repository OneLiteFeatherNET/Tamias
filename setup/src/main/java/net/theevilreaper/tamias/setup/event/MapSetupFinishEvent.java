package net.theevilreaper.tamias.setup.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.theevilreaper.tamias.setup.data.SetupData;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link MapSetupFinishEvent} is called when a {@link Player} finishs a map setup.
 * A cancel logic is required by the implementation of the cancel trigger.
 * In relation to that this event is not cancellable.
 *
 * @param setupData the data of the setup
 */
public record MapSetupFinishEvent(@NotNull SetupData setupData) implements Event {
}
