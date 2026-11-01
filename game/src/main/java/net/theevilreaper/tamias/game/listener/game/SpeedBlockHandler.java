package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Registers the {@link PlayerMoveEvent} listener for speed blocks only while it is needed.
 * Every move of every player would reach the listener, so it is only added when the ground of the round contains
 * speed blocks and removed again when the round ends.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SpeedBlockHandler {

    private final EventNode<Event> node;
    private final EventListener<PlayerMoveEvent> listener;
    private boolean active;

    /**
     * Creates a new handler for the given node.
     *
     * @param node     the node which gets the listener
     * @param listener the logic which is called on a move
     */
    public SpeedBlockHandler(EventNode<Event> node, Consumer<PlayerMoveEvent> listener) {
        this.node = node;
        this.listener = EventListener.of(PlayerMoveEvent.class, listener);
    }

    /**
     * Adds the listener to the node if the given ground contains speed blocks.
     *
     * @param groundData the ground of the current round
     */
    public synchronized void enable(@Nullable GroundData groundData) {
        if (this.active || groundData == null || !groundData.hasSpeedBlocks()) return;
        this.node.addListener(this.listener);
        this.active = true;
    }

    /**
     * Removes the listener from the node, nothing happens if it isn't registered.
     */
    public synchronized void disable() {
        if (!this.active) return;
        this.node.removeListener(this.listener);
        this.active = false;
    }

    /**
     * Returns whether the listener is registered.
     *
     * @return true if the listener is registered
     */
    public synchronized boolean isActive() {
        return this.active;
    }
}
