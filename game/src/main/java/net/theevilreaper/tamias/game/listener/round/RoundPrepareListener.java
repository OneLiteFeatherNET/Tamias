package net.theevilreaper.tamias.game.listener.round;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

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

        // Freeze right alongside the teleport - GroundBuildPhase.onStart() runs several ticks later
        // (the countdown phase this fires from still has to reach 0 and finish), which left a window
        // where players sat at spawn but could still walk off before the freeze kicked back in.
        for (Player player : onlinePlayers) {
            player.setTag(Tags.FROZEN, true);
        }

        this.spawnArea.teleport(this.instance, new ArrayList<>(onlinePlayers), () -> false);
    }
}
