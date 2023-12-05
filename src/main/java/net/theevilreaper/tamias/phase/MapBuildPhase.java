package net.theevilreaper.tamias.phase;

import de.icevizion.xerus.api.phase.GamePhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.listener.game.PlayerStoppedMovement;

/**
 * Tbe phase implementation handles each logic which should be executed during the period where the map builds up.
 * Its use only the {@link GamePhase} abstraction because the build process is not limited to a strict time duration.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class MapBuildPhase extends GamePhase {

    private final EventNode<PlayerEvent> eventNode;

    public MapBuildPhase() {
        super("MapBuild");
        this.eventNode = EventNode.type(this.getName(), EventFilter.PLAYER);
        eventNode.addListener(PlayerMoveEvent.class, new PlayerStoppedMovement());
    }

    @Override
    protected void onStart() {
        MinecraftServer.getGlobalEventHandler().addChild(this.eventNode);
    }

    public void stop() {
        MinecraftServer.getGlobalEventHandler().removeChild(this.eventNode);
    }
}
