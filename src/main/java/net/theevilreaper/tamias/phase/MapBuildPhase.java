package net.theevilreaper.tamias.phase;

import de.icevizion.xerus.api.phase.GamePhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.area.GameArea;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class MapBuildPhase extends GamePhase {

    private final EventNode<PlayerEvent> eventNode;
    private final GameArea gameArea;

    public MapBuildPhase(GameArea gameArea) {
        super("MapBuild");
        this.eventNode = EventNode.type("MapBuildPhase", EventFilter.PLAYER);
        eventNode.addListener(PlayerMoveEvent.class, this::handlePlayerMove);
        this.gameArea = gameArea;
    }

    @Override
    protected void onStart() {
        MinecraftServer.getGlobalEventHandler().addChild(this.eventNode);
        Task task = gameArea.build();
    }

    public void stop() {
        MinecraftServer.getGlobalEventHandler().removeChild(this.eventNode);
    }

    private void handlePlayerMove(@NotNull PlayerMoveEvent moveEvent) {
        var currentPos = moveEvent.getPlayer().getPosition();
        var newPos = moveEvent.getNewPosition();

        if (currentPos.x() != newPos.x() || currentPos.z() != newPos.z()) {
            moveEvent.setCancelled(true);
        }
    }
}
