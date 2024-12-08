package net.theevilreaper.tamias.setup;

import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.MapEntry;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.setup.commands.SetupCommand;
import net.theevilreaper.tamias.setup.data.SetupDataService;
import net.theevilreaper.tamias.setup.event.MapSetupFinishEvent;
import net.theevilreaper.tamias.setup.event.MapSetupSelectEvent;
import net.theevilreaper.tamias.setup.inventory.InventoryProvider;
import net.theevilreaper.tamias.setup.listener.PlayerConfigurationListener;
import net.theevilreaper.tamias.setup.listener.PlayerDisconnectListener;
import net.theevilreaper.tamias.setup.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.setup.listener.PlayerUseItemListener;
import net.theevilreaper.tamias.setup.listener.entity.EntityAddToInstanceListener;
import net.theevilreaper.tamias.setup.listener.map.MapSetupFinishListener;
import net.theevilreaper.tamias.setup.listener.map.MapSetupSelectListener;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TamiasSetup extends Extension {

    private static final Pos FALLBACK_POS = new Pos(0, 100, 0);
    public static final Tag<Byte> SETUP_TAG = Tag.Byte("setup");
    public static final Tag<Boolean> DELETE_TAG = Tag.Boolean("delete").defaultValue(false);
    public static final Component SELECT_MAP_FIRST = Component.text("<red>Please select a map first!");
    private final InstanceContainer mainInstance;
    private final FileHandler fileHandler;
    private final MapProvider mapProvider;
    private final SetupDataService setupDataService;
    private InventoryProvider inventoryProvider;

    public TamiasSetup() {
        this.mainInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.mapProvider = new MapProvider(Paths.get("").resolve("maps"), TamiasSetup::defaultFilter);
        this.fileHandler = new GsonFileHandler(GsonUtil.GSON);
        this.setupDataService = new SetupDataService();
    }

    @Override
    public void initialize() {
        MinecraftServer.getInstanceManager().registerInstance(this.mapProvider.getActiveInstance().get());
        this.mapProvider.loadLobbyMap(this.mainInstance);
        this.inventoryProvider = new InventoryProvider(this.mapProvider::getMapEntries);
        this.registerListener();
        this.registerCommands();
    }

    @Override
    public void terminate() {
        // Nothing to do here
    }

    private void registerListener() {
        var manager = MinecraftServer.getGlobalEventHandler();
        SetupItems items = new SetupItems();
        Supplier<Pos> spawnPos = () -> this.mapProvider.getActiveMap().get().getSpawnOrDefault(FALLBACK_POS);
        manager.addListener(PlayerUseItemEvent.class, new PlayerUseItemListener(inventoryProvider::openMapSetupInventory, setupDataService::getSetupData));
        manager.addListener(PlayerDisconnectEvent.class, new PlayerDisconnectListener(setupDataService::getSetupData));
        manager.addListener(AsyncPlayerConfigurationEvent.class, new PlayerConfigurationListener(() -> this.mainInstance));
        manager.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(items::setOverViewItem, spawnPos));
        manager.addListener(AddEntityToInstanceEvent.class, new EntityAddToInstanceListener(mainInstance.getUniqueId(), items));
        manager.addListener(MapSetupSelectEvent.class, new MapSetupSelectListener(this.fileHandler, this.setupDataService));
        manager.addListener(MapSetupFinishEvent.class, new MapSetupFinishListener(this.mapProvider, this::instanceCallback));
    }

    /**
     * Callback for the instance creation.
     *
     * @param player the player to set the instance
     */
    private void instanceCallback(@NotNull Player player) {
        player.setInstance(this.mainInstance, this.mapProvider.getActiveMap().get().getSpawnOrDefault(FALLBACK_POS));
    }

    /**
     * Register the commands for the setup extension.
     */
    private void registerCommands() {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new SetupCommand(this.setupDataService));
    }

    /**
     * Default filter for the path stream.
     *
     * @param pathStream the stream of paths
     * @return a list with all available maps
     */
    private static @NotNull List<MapEntry> defaultFilter(@NotNull Stream<Path> pathStream) {
        return pathStream.map(MapEntry::of).toList();
    }
}
