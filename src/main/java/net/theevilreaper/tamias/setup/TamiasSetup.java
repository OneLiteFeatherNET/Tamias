package net.theevilreaper.tamias.setup;

import com.google.gson.Gson;
import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.BaseMap;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.tag.Tag;
import net.theevilreaper.tamias.map.GameMap;
import net.theevilreaper.tamias.setup.commands.SetupCommand;
import net.theevilreaper.tamias.setup.inventory.MapSelectionInventory;
import net.theevilreaper.tamias.setup.listener.EntityAddToInstanceListener;
import net.theevilreaper.tamias.setup.listener.PlayerUseItemListener;
import net.theevilreaper.tamias.setup.state.SaveState;
import net.theevilreaper.tamias.util.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TamiasSetup {

    protected static final Tag<Byte> SETUP_TAG = Tag.Byte("setupItems");
    public static final Tag<String> MAP_PATH_TAG = Tag.String("mapPath");
    private static final Component INV_TITLE = Component.text("Select a map");
    public static final Component SELECT_MAP_FIRST = Messages.withMiniPrefix("<red>Please select a map first!");
    private final Instance mainInstance;
    private final FileHandler fileHandler;
    private final Map<Path, BaseMap> activeMaps;
    private final MapSelectionInventory mapSelectionInventory;

    public TamiasSetup(@NotNull Instance mainInstance, @NotNull Gson gson, @NotNull List<Path> pathList) {
        this.mainInstance = mainInstance;
        this.fileHandler = new GsonFileHandler(gson);
        this.activeMaps = new HashMap<>();
        this.mapSelectionInventory = new MapSelectionInventory(INV_TITLE, pathList, this::createMapObject);
        this.registerListener();
        this.registerCommands();
    }

    private void registerListener() {
        var manager = MinecraftServer.getGlobalEventHandler();
        var items = new SetupItems(SETUP_TAG);
        manager.addListener(PlayerUseItemEvent.class, new PlayerUseItemListener(SETUP_TAG, this.mapSelectionInventory::open));
        manager.addListener(AddEntityToInstanceEvent.class, new EntityAddToInstanceListener(mainInstance.getUniqueId(), items));
    }

    private void registerCommands() {
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new SetupCommand(this::handleNoMap));
    }

    public void load() {

    }

    private @Nullable BaseMap handleNoMap(@NotNull CommandSender sender) {
        if (!sender.hasTag(MAP_PATH_TAG)) {
            return null;
        }
        return this.activeMaps.get(sender.getTag(MAP_PATH_TAG));
    }

    private @NotNull SaveState saveAndCleanUp(@NotNull Path path) {
        var map = this.activeMaps.remove(path);
        if (map == null) {
            return SaveState.ABORT;
        }
        this.fileHandler.save(path, map.getClass());
        return SaveState.SUCCESSFULLY;
    }

    private @Nullable Void createMapObject(@NotNull ClickType clickType, @NotNull Path path) {
        if (!(clickType == ClickType.LEFT_CLICK || clickType == ClickType.RIGHT_CLICK)) return null;

        this.activeMaps.computeIfAbsent(path, path1 -> {
            if (clickType == ClickType.LEFT_CLICK) {
                return new BaseMap();
            }
            return new GameMap();
        });
        return null;
    }
}
