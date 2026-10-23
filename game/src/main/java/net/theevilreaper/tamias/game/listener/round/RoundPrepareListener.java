package net.theevilreaper.tamias.game.listener.round;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Teleports every online player onto the spawn area when a round is prepared. The players can't move until the round
 * is played, otherwise they could walk down onto the game area while the map is built.
 */
public final class RoundPrepareListener implements Consumer<RoundPrepareEvent> {

    private final SpawnArea spawnArea;
    private final Instance instance;

    public RoundPrepareListener(SpawnArea spawnArea, Instance instance) {
        this.spawnArea = spawnArea;
        this.instance = instance;
    }

    @Override
    public void accept(RoundPrepareEvent event) {
        Collection<Player> onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers();
        this.spawnArea.teleport(this.instance, new ArrayList<>(onlinePlayers), () -> false, AttributeHelper::disableMovement);
    }
}
