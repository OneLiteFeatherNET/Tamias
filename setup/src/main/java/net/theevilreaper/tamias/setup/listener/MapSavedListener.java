package net.theevilreaper.tamias.setup.listener;

import net.theevilreaper.tamias.setup.event.MapSavedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class MapSavedListener implements Consumer<MapSavedEvent> {

    private final Consumer<UUID> removeFunction;

    public MapSavedListener(@NotNull Consumer<UUID> removeFunction) {
        this.removeFunction = removeFunction;
    }

    @Override
    public void accept(@NotNull MapSavedEvent event) {
        this.removeFunction.accept(event.getPlayer().getUuid());
    }
}
