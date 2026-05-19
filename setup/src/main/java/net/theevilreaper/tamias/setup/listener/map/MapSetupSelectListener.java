package net.theevilreaper.tamias.setup.listener.map;

import net.onelitefeather.guira.SetupDataService;
import net.onelitefeather.guira.data.SetupData;
import net.theevilreaper.aves.file.FileHandler;
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
import net.theevilreaper.tamias.setup.data.SetupDataFactory;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public final class MapSetupSelectListener implements Consumer<MapSetupSelectEvent> {

    private final FileHandler fileHandler;
    private final SetupDataService setupDataService;

    public MapSetupSelectListener(FileHandler fileHandler, SetupDataService setupDataService) {
        this.fileHandler = fileHandler;
        this.setupDataService = setupDataService;
    }

    @Override
    public void accept(MapSetupSelectEvent event) {
        Player player = event.getPlayer();

        SetupData setupData = this.setupDataService.get(player.getUuid()).get();

        /*if (setupData != null && setupData.hasMap()) {
            // If this condition is reached the setup is fucked up
            player.sendMessage("You already have a map selected");
        }*/

        MapEntry mapEntry = event.getMapEntry();
        InstanceSetupData data = SetupDataFactory.create(player, mapEntry, event.isLobbyMode());

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

    private Task.Builder getTeleportTask(Runnable runnable) {
        return MinecraftServer.getSchedulerManager().buildTask(runnable).delay(3, ChronoUnit.SECONDS);
    }
}
