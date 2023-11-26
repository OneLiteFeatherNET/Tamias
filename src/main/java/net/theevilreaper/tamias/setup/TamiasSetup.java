package net.theevilreaper.tamias.setup;

import com.google.gson.Gson;
import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.BaseMap;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.tag.Tag;
import net.theevilreaper.tamias.map.GameMap;
import net.theevilreaper.tamias.setup.commands.SetupCommand;
import net.theevilreaper.tamias.setup.inventory.MapSelectionInventory;
import net.theevilreaper.tamias.setup.listener.PlayerUseItemListener;
import net.theevilreaper.tamias.setup.state.SaveState;
import net.theevilreaper.tamias.util.ComponentHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TamiasSetup {

    protected static final Tag<Byte> SETUP_TAG = Tag.Byte("setupItems");
    private static final Component INV_TITLE = Component.text("Select a map");
    private final FileHandler fileHandler;
    private Map<Path, BaseMap> activeMaps;
    private final MapSelectionInventory mapSelectionInventory;

    public TamiasSetup(@NotNull Gson gson, @NotNull List<Path> pathList) {
        this.fileHandler = new GsonFileHandler(gson);
        this.mapSelectionInventory = new MapSelectionInventory(INV_TITLE, pathList, this::createMapObject);
        this.registerListener();
    }

    private void registerListener() {
        var manager = MinecraftServer.getGlobalEventHandler();
        manager.addListener(PlayerUseItemEvent.class, new PlayerUseItemListener(SETUP_TAG, this.mapSelectionInventory::open));
    }

    private void registerCommands() {
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new SetupCommand(commandSender -> {
            return null;
        }));
    }

    public void load() {

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
