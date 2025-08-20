package net.theevilreaper.tamias.game.listener.round;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.round.event.RoundPrepareEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class RoundPrepareListener implements Consumer<RoundPrepareEvent> {

    private final SpawnArea spawnArea;
    private final Instance instance;

    public RoundPrepareListener(@NotNull SpawnArea spawnArea, @NotNull Instance instance) {
        this.spawnArea = spawnArea;
        this.instance = instance;
    }

    @Override
    public void accept(@NotNull RoundPrepareEvent event) {
        Collection<@NotNull Player> onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers();
        spawnArea.teleport(instance, new ArrayList<>(onlinePlayers), player -> {});
    }
}
