package net.theevilreaper.tamias.game.listener.area;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.event.GameAreaChunksReadyEvent;
import net.theevilreaper.tamias.common.map.event.MapPrepareEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Prepares the game area once the lobby countdown reaches its preparation time.
 * The countdown can run again when players leave the lobby, but the area is only prepared once:
 * a second flatten would treat the placed barriers as foreign blocks and remove the whole ground.
 */
public final class MapPrepareListener implements Consumer<MapPrepareEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapPrepareListener.class);

    private final GamePlacement gamePlacement;
    private boolean prepared;

    public MapPrepareListener(GamePlacement gamePlacement) {
        this.gamePlacement = gamePlacement;
    }

    @Override
    public void accept(MapPrepareEvent event) {
        if (this.prepared) return;
        this.prepared = true;
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
