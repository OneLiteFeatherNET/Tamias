package net.theevilreaper.tamias.game.listener.area;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.event.GameAreaChunksReadyEvent;
import net.theevilreaper.tamias.common.map.event.MapPrepareEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public final class MapPrepareListener implements Consumer<MapPrepareEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapPrepareListener.class);

    private final GamePlacement gamePlacement;

    public MapPrepareListener(GamePlacement gamePlacement) {
        this.gamePlacement = gamePlacement;
    }

    @Override
    public void accept(MapPrepareEvent event) {
        this.gamePlacement.preloadChunks()
                .thenRunAsync(
                        () -> EventDispatcher.call(new GameAreaChunksReadyEvent()),
                        MinecraftServer.getSchedulerManager()
                )
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to preload game area chunks", throwable);
                    return null;
                });
    }
}
