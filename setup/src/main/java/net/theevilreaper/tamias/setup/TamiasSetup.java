package net.theevilreaper.tamias.setup;

import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.common.util.MapFilter;
import net.theevilreaper.tamias.setup.commands.SetupCommand;
import net.theevilreaper.tamias.setup.data.SetupDataService;
import net.theevilreaper.tamias.setup.event.MapSetupFinishEvent;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;
import net.theevilreaper.tamias.setup.inventory.MapSetupInventory;
import net.theevilreaper.tamias.setup.listener.PlayerChatListener;
import net.theevilreaper.tamias.setup.listener.PlayerConfigurationListener;
import net.theevilreaper.tamias.setup.listener.PlayerDisconnectListener;
import net.theevilreaper.tamias.setup.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.setup.listener.item.PlayerDropItemListener;
import net.theevilreaper.tamias.setup.listener.item.PlayerPickupItemListener;
import net.theevilreaper.tamias.setup.listener.item.PlayerUseItemListener;
import net.theevilreaper.tamias.setup.listener.entity.EntityAddToInstanceListener;
import net.theevilreaper.tamias.setup.listener.map.MapSetupFinishListener;
import net.theevilreaper.tamias.setup.listener.map.MapSetupSelectListener;
import net.theevilreaper.tamias.setup.map.SetupMapProvider;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class TamiasSetup {

    public static final Tag<Byte> SETUP_TAG = Tag.Transient("setup");
    public static final Tag<Boolean> DELETE_TAG = Tag.Boolean("delete").defaultValue(false);
    public static final Component SELECT_MAP_FIRST = Component.text("Please select a map first!", NamedTextColor.RED);

    private final FileHandler fileHandler;
    private final MapProvider mapProvider;
    private final SetupDataService setupDataService;
    private final SetupItems setupItems;
    private final MapSetupInventory mapSetupInventory;

    public TamiasSetup() {
        this.fileHandler = new GsonFileHandler(GsonUtil.GSON);
        this.mapProvider = new SetupMapProvider(MapFilter.ROOT_FOLDER, this.fileHandler);
        this.setupDataService = new SetupDataService();
        this.setupItems = new SetupItems();
        this.mapSetupInventory = new MapSetupInventory(this.mapProvider::getEntries);
    }

    public void initialize() {
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
        manager.addListener(PlayerDisconnectEvent.class, new PlayerDisconnectListener(setupDataService::removeSetupData));
        manager.addListener(AsyncPlayerConfigurationEvent.class, new PlayerConfigurationListener(instanceSupplier));
        manager.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(initialSpawnSupplier));
        manager.addListener(AddEntityToInstanceEvent.class, new EntityAddToInstanceListener(instanceSupplier, setupItems));
        manager.addListener(MapSetupSelectEvent.class, new MapSetupSelectListener(this.fileHandler, this.setupDataService));
        manager.addListener(MapSetupFinishEvent.class, new MapSetupFinishListener(this.mapProvider::saveMap, instanceSwitcher));
        manager.addListener(PlayerChatEvent.class, new PlayerChatListener(this.setupDataService));

        // Item listener
        manager.addListener(PlayerUseItemEvent.class, new PlayerUseItemListener(this::updateMapInventory, setupDataService::getSetupData));
        manager.addListener(ItemDropEvent.class, new PlayerDropItemListener());
        manager.addListener(PickupItemEvent.class, new PlayerPickupItemListener());
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
