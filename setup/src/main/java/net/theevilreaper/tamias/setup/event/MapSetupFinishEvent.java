package net.theevilreaper.tamias.setup.event;

import net.minestom.server.event.Event;
import net.theevilreaper.tamias.setup.data.SetupData;
import org.jetbrains.annotations.NotNull;

public record MapSetupFinishEvent(@NotNull SetupData setupData) implements Event {
}
