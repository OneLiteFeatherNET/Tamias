package net.theevilreaper.tamias.common;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.theevilreaper.aves.map.provider.MapProvider;

import java.util.function.Consumer;

/**
 * The interface provides a default method to register some listeners to cancel specific events.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ListenerHandling {

    Consumer<CancellableEvent> CANCELLABLE_EVENT = event -> event.setCancelled(true);

    /**
     * Registers some {@link Event} listener to cancel specific default events.
     *
     * @param eventNode the event node to register the listeners
     */
    default void registerCancelListener(EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockBreakEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(PlayerBlockPlaceEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(ItemDropEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(PlayerSwapItemEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(PlayerBlockInteractEvent.class, CANCELLABLE_EVENT::accept);
    }

    /**
     * Closes the given {@link MapProvider} if it implements {@link AutoCloseable}.
     *
     * @param mapProvider the map provider to close
     */
    default void closeMapProvider(MapProvider mapProvider) {
        if (mapProvider instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception exception) {
                MinecraftServer.getExceptionManager().handleException(exception);
            }
        }
    }
}
