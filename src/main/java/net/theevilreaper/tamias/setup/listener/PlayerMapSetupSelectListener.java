package net.theevilreaper.tamias.setup.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.setup.event.MapSelectionEvent;
import net.theevilreaper.tamias.util.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerMapSetupSelectListener implements Consumer<MapSelectionEvent> {

    private final Function<@NotNull Path, @Nullable UUID> pathToUUIDMapper;
    private final BiFunction<@NotNull Path, @NotNull UUID, @NotNull UUID> mapUpdater;

    public PlayerMapSetupSelectListener(
            @NotNull Function<@NotNull Path, @Nullable UUID> pathToUUIDMapper,
            @NotNull BiFunction<@NotNull Path, @NotNull UUID, @NotNull UUID> mapUpdater) {
        this.pathToUUIDMapper = pathToUUIDMapper;
        this.mapUpdater = mapUpdater;
    }

    @Override
    public void accept(@NotNull MapSelectionEvent event) {
        var player = event.getPlayer();

        var instanceId = this.pathToUUIDMapper.apply(event.getPath());
        if (instanceId == null) {
            player.sendMessage(Messages.withMini("<red>The map will be loaded now. This takes some time"));
            var instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            instance.enableAutoChunkLoad(true);
            instance.setChunkLoader(new AnvilLoader(event.getPath()));
            MinecraftServer.getInstanceManager().registerInstance(instance);
            var position = player.getPosition();
            var spawnPos = new Pos(0, 50, 0, position.yaw(), position.pitch());
            getTeleportTask(instance, player, spawnPos).schedule();
            this.mapUpdater.apply(event.getPath(), instance.getUniqueId());
            return;
        }

        player.sendMessage(Messages.withMini("<gray>Sending to loaded map"));
        Instance instance = MinecraftServer.getInstanceManager().getInstance(instanceId);
        player.setInstance(instance, new Pos(0, 50, 0));
    }

    private @NotNull Task.Builder getTeleportTask(@NotNull Instance instance, @NotNull Player player, @NotNull Pos pos) {
        return MinecraftServer.getSchedulerManager().buildTask(
                () -> {
                    player.setInstance(instance);
                    player.teleport(pos);
                }
        ).delay(3, ChronoUnit.SECONDS);
    }
}
