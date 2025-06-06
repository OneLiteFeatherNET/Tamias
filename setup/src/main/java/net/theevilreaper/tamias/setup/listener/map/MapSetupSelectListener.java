package net.theevilreaper.tamias.setup.listener.map;

import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.SetupData;
import net.theevilreaper.tamias.setup.data.SetupDataService;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public final class MapSetupSelectListener implements Consumer<MapSetupSelectEvent> {

    private final FileHandler fileHandler;
    private final SetupDataService setupDataService;

    public MapSetupSelectListener(@NotNull FileHandler fileHandler, @NotNull SetupDataService setupDataService) {
        this.fileHandler = fileHandler;
        this.setupDataService = setupDataService;
    }

    @Override
    public void accept(@NotNull MapSetupSelectEvent event) {
        Player player = event.getPlayer();

        SetupData setupData = this.setupDataService.getSetupData(player);

        if (setupData != null && setupData.hasMap()) {
            // If this condition is reached the setup is fucked up
            player.sendMessage("You already have a map selected");
        }

        SetupData.Builder builder = setupData == null ? SetupData.builder().player(player) : SetupData.builder(setupData);

        MapEntry mapEntry = event.getMapEntry();
        Component message = Messages.withPrefix(Component.text("You selected the map: ", NamedTextColor.GRAY))
                .append(Component.text(event.getMapEntry().getDirectoryRoot().getFileName().toString(), NamedTextColor.AQUA));
        player.sendMessage(message);
        BaseMap baseMap = loadData(mapEntry, event.isLobbyMode());
        builder.mapEntry(mapEntry).baseMap(baseMap);

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlying(true);
        player.setFlying(true);

        SetupData updatedData = builder.build();
        this.setupDataService.addSetupData(player, updatedData);

        if (!updatedData.loadMap()) {
            player.sendMessage(Messages.withPrefix(Component.text("An error occurred while loading the map", NamedTextColor.RED)));
            return;
        }

        player.setTag(TamiasSetup.SETUP_TAG, (byte) 1);
        getTeleportTask(updatedData::teleport).schedule();
    }

    private @NotNull Task.Builder getTeleportTask(@NotNull Runnable runnable) {
        return MinecraftServer.getSchedulerManager().buildTask(runnable).delay(3, ChronoUnit.SECONDS);
    }

    private @NotNull BaseMap loadData(@NotNull MapEntry mapEntry, boolean lobbyMode) {

        if (!mapEntry.hasMapFile()) {
            return lobbyMode ? new BaseMap() : new GameMap();
        }

        Class<? extends BaseMap> mapClass = lobbyMode ? BaseMap.class : GameMap.class;

        return this.fileHandler.load(mapEntry.getMapFile(), mapClass).get();
    }
}
