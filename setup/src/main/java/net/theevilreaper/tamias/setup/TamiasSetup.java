package net.theevilreaper.tamias.setup;

import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.onelitefeather.guira.SetupDataService;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.common.ListenerHandling;
import net.theevilreaper.tamias.setup.dialog.event.DialogRequestEvent;
import net.theevilreaper.tamias.setup.event.PlayerMapSelectEvent;
import net.theevilreaper.tamias.setup.event.PositionSetEvent;
import net.theevilreaper.tamias.setup.inventory.MapSetupInventory;
import net.theevilreaper.tamias.setup.listener.PlayerChatListener;
import net.theevilreaper.tamias.setup.listener.PlayerConfigurationListener;
import net.theevilreaper.tamias.setup.listener.PlayerDisconnectListener;
import net.theevilreaper.tamias.setup.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.setup.listener.PositionSetListener;
import net.theevilreaper.tamias.setup.listener.dialog.DialogPayloadListener;
import net.theevilreaper.tamias.setup.listener.dialog.DialogRequestListener;
import net.theevilreaper.tamias.setup.listener.item.PlayerUseItemListener;
import net.theevilreaper.tamias.setup.listener.entity.EntityAddToInstanceListener;
import net.theevilreaper.tamias.setup.listener.map.SetupFinishListener;
import net.theevilreaper.tamias.setup.listener.map.MapSetupSelectListener;
import net.theevilreaper.tamias.setup.map.SetupMapProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.function.Supplier;

public final class TamiasSetup implements ListenerHandling {

    private final SetupDataService setupDataService;
    private final MapProvider mapProvider;
    private final MapSetupInventory mapSetupInventory;

    public TamiasSetup() {
        this.mapProvider = new SetupMapProvider(Paths.get(""));
        this.setupDataService = SetupDataService.create();
        this.mapSetupInventory = new MapSetupInventory(this.mapProvider::getEntries);
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::terminate);
    }

    public void initialize() {
        this.registerCancelListener(MinecraftServer.getGlobalEventHandler());
        this.registerListener();
    }

    public void terminate() {
        // Nothing to do here
    }

    private void registerListener() {
        GlobalEventHandler manager = MinecraftServer.getGlobalEventHandler();
        Supplier<Instance> instanceSupplier = mapProvider.getActiveInstance();
        SetupMapProvider setupMapProvider = (SetupMapProvider) mapProvider;
        PlayerConsumer initialSpawnSupplier = player -> setupMapProvider.teleportToSpawn(player, false);
        PlayerConsumer instanceSwitcher = player -> setupMapProvider.teleportToSpawn(player, true);
        manager.addListener(PlayerDisconnectEvent.class, new PlayerDisconnectListener(setupDataService::remove));
        manager.addListener(AsyncPlayerConfigurationEvent.class, new PlayerConfigurationListener(instanceSupplier));
        manager.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(initialSpawnSupplier));
        manager.addListener(AddEntityToInstanceEvent.class, new EntityAddToInstanceListener(instanceSupplier));
        manager.addListener(PlayerMapSelectEvent.class, new MapSetupSelectListener(this.setupDataService));
        manager.addListener(SetupFinishEvent.class, new SetupFinishListener(instanceSwitcher));
        manager.addListener(PlayerChatEvent.class, new PlayerChatListener(this.setupDataService));

        // Item listener
        manager.addListener(PlayerUseItemEvent.class, new PlayerUseItemListener(this::updateMapInventory, setupDataService::get));

        // Dialog
        manager.addListener(PlayerCustomClickEvent.class, new DialogPayloadListener(this.setupDataService));
        manager.addListener(DialogRequestEvent.class, new DialogRequestListener());

        // Position listener
        manager.addListener(PositionSetEvent.class, new PositionSetListener(this.setupDataService));
    }

    /**
     * Open the map inventory for the given player.
     *
     * @param player the player to open the inventory
     */
    private void updateMapInventory(@NotNull Player player) {
        this.mapSetupInventory.open(player);
    }

}
