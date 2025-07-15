package net.theevilreaper.tamias.setup;

import net.onelitefeather.guira.SetupDataService;
import net.onelitefeather.guira.event.SetupFinishEvent;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.theevilreaper.tamias.common.ListenerHandling;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.setup.commands.SetupCommand;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;
import net.theevilreaper.tamias.setup.inventory.MapSetupInventory;
import net.theevilreaper.tamias.setup.listener.PlayerChatListener;
import net.theevilreaper.tamias.setup.listener.PlayerConfigurationListener;
import net.theevilreaper.tamias.setup.listener.PlayerDisconnectListener;
import net.theevilreaper.tamias.setup.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.setup.listener.item.PlayerUseItemListener;
import net.theevilreaper.tamias.setup.listener.entity.EntityAddToInstanceListener;
import net.theevilreaper.tamias.setup.listener.map.SetupFinishListener;
import net.theevilreaper.tamias.setup.listener.map.MapSetupSelectListener;
import net.theevilreaper.tamias.setup.map.SetupMapProvider;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.function.Supplier;

public final class TamiasSetup implements ListenerHandling {

    public static final Tag<Byte> SETUP_TAG = Tag.Transient("setup");
    public static final Tag<Boolean> DELETE_TAG = Tag.Boolean("delete").defaultValue(false);

    private final SetupDataService setupDataService;
    private final FileHandler fileHandler;
    private final MapProvider mapProvider;
    private final SetupItems setupItems;
    private final MapSetupInventory mapSetupInventory;

    public TamiasSetup() {
        this.fileHandler = new GsonFileHandler(GsonUtil.GSON);
        this.mapProvider = new SetupMapProvider(Paths.get(""), this.fileHandler);
        this.setupDataService = SetupDataService.create();
        this.setupItems = new SetupItems();
        this.mapSetupInventory = new MapSetupInventory(this.mapProvider::getEntries);
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::terminate);
    }

    public void initialize() {
        this.registerCancelListener(MinecraftServer.getGlobalEventHandler());
        this.registerListener();
        this.registerCommands();
    }

    public void terminate() {
        // Nothing to do here
    }

    private void registerListener() {
        GlobalEventHandler manager = MinecraftServer.getGlobalEventHandler();
        Supplier<Instance> instanceSupplier = mapProvider.getActiveInstance();
        SetupMapProvider setupMapProvider = (SetupMapProvider) mapProvider;
        PlayerConsumer initialSpawnSupplier = player -> {
            setupMapProvider.teleportToSpawn(player, false);
            setupItems.setOverViewItem(player);
        };
        PlayerConsumer instanceSwitcher = player -> {
            setupMapProvider.teleportToSpawn(player, true);
            setupItems.setOverViewItem(player);
        };
        manager.addListener(PlayerDisconnectEvent.class, new PlayerDisconnectListener(setupDataService::remove));
        manager.addListener(AsyncPlayerConfigurationEvent.class, new PlayerConfigurationListener(instanceSupplier));
        manager.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(initialSpawnSupplier));
        manager.addListener(AddEntityToInstanceEvent.class, new EntityAddToInstanceListener(instanceSupplier, setupItems));
        manager.addListener(MapSetupSelectEvent.class, new MapSetupSelectListener(this.fileHandler, this.setupDataService));
        manager.addListener(SetupFinishEvent.class, new SetupFinishListener(instanceSwitcher));
        manager.addListener(PlayerChatEvent.class, new PlayerChatListener(this.setupDataService));

        // Item listener
        manager.addListener(PlayerUseItemEvent.class, new PlayerUseItemListener(this::updateMapInventory, setupDataService::get));
    }

    /**
     * Open the map inventory for the given player.
     *
     * @param player the player to open the inventory
     */
    private void updateMapInventory(@NotNull Player player) {
        this.mapSetupInventory.open(player);
    }

    /**
     * Register the commands for the setup extension.
     */
    private void registerCommands() {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new SetupCommand(this.setupDataService));
    }
}
