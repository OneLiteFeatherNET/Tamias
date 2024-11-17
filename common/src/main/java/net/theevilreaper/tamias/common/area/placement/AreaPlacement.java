package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface AreaPlacement permits AreaBasePlacement {

    boolean isRunning();

    void place();

    @Nullable Task getTask();
}
