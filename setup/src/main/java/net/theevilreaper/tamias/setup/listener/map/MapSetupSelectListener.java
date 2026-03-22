package net.theevilreaper.tamias.setup.listener.map;

import net.onelitefeather.guira.SetupDataService;
import net.onelitefeather.guira.data.SetupData;
import net.theevilreaper.aves.map.MapEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.data.GameData;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.data.LobbyData;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;
import net.theevilreaper.tamias.setup.util.SetupTags;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;

import static net.theevilreaper.tamias.setup.map.SetupMapProvider.FILE_HANDLER;

public final class MapSetupSelectListener implements Consumer<MapSetupSelectEvent> {

    private final SetupDataService setupDataService;

    public MapSetupSelectListener(SetupDataService setupDataService) {
        this.setupDataService = setupDataService;
    }

    @Override
    public void accept(MapSetupSelectEvent event) {
        Player player = event.getPlayer();

        Optional<SetupData> setupData = this.setupDataService.get(player.getUuid());

        if (setupData.isPresent()) {
            player.sendMessage("You already have a map selected");
        }

        InstanceSetupData data;
        MapEntry mapEntry = event.getMapEntry();
        if (event.isLobbyMode()) {
            data = new LobbyData(player.getUuid(), mapEntry);
        } else {
            data = new GameData(player.getUuid(), mapEntry);
        }

        Component message = Messages.withPrefix(Component.text("You selected the map: ", NamedTextColor.GRAY))
                .append(Component.text(mapEntry.getDirectoryRoot().getFileName().toString(), NamedTextColor.AQUA));
        player.sendMessage(message);

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlying(true);
        player.setFlying(true);

        this.setupDataService.add(player.getUuid(), data);

        player.setTag(SetupTags.SETUP_TAG, 1);
        getTeleportTask(() -> data.teleport(player)).schedule();
    }

    /**
     * Creates a task that teleports the player after a delay of 3 seconds.
     *
     * @param runnable the runnable to execute after the delay, which should contain the teleportation logic
     * @return a Task.Builder that can be scheduled to execute the teleportation after the specified delay
     */
    private Task.Builder getTeleportTask(Runnable runnable) {
        return MinecraftServer.getSchedulerManager().buildTask(runnable).delay(3, ChronoUnit.SECONDS);
    }
}
