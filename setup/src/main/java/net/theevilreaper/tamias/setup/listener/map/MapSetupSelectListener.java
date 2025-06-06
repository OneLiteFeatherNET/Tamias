package net.theevilreaper.tamias.setup.listener.map;

import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.GameData;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.data.LobbyData;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public final class MapSetupSelectListener implements Consumer<MapSetupSelectEvent> {

    private final FileHandler fileHandler;
    private final SetupDataService<InstanceSetupData<? extends BaseMap>> setupDataService;

    public MapSetupSelectListener(
            @NotNull FileHandler fileHandler,
            @NotNull SetupDataService<InstanceSetupData<? extends BaseMap>> setupDataService
    ) {
        this.fileHandler = fileHandler;
        this.setupDataService = setupDataService;
    }

    @Override
    public void accept(@NotNull MapSetupSelectEvent event) {
        Player player = event.getPlayer();

        InstanceSetupData<? extends BaseMap> setupData = this.setupDataService.get(player.getUuid()).get();

        if (setupData != null && setupData.hasMap()) {
            // If this condition is reached the setup is fucked up
            player.sendMessage("You already have a map selected");
        }

        InstanceSetupData<? extends BaseMap> data;
        MapEntry mapEntry = event.getMapEntry();
        if (event.isLobbyMode()) {
            data = new LobbyData(player.getUuid(), mapEntry, this.fileHandler);
        } else {
            data = new GameData(player.getUuid(), mapEntry, this.fileHandler);
        }

        Component message = Messages.withPrefix(Component.text("You selected the map: ", NamedTextColor.GRAY))
                .append(Component.text(mapEntry.getDirectoryRoot().getFileName().toString(), NamedTextColor.AQUA));
        player.sendMessage(message);

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlying(true);
        player.setFlying(true);

        this.setupDataService.add(player.getUuid(), data);

        player.setTag(TamiasSetup.SETUP_TAG, (byte) 1);
        getTeleportTask(() -> data.teleport(player)).schedule();
    }

    private @NotNull Task.Builder getTeleportTask(@NotNull Runnable runnable) {
        return MinecraftServer.getSchedulerManager().buildTask(runnable).delay(3, ChronoUnit.SECONDS);
    }
}
