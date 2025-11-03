package net.theevilreaper.tamias.setup.data;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

public class GameData extends InstanceSetupData {

    private final FileHandler fileHandler;
    private LobbyViewInventory inventory;
    private GameMapBuilder gameMapBuilder;
    private boolean areaMode;

    /**
     * Constructs a new GameData instance.
     *
     * @param uuid       the UUID of the player
     * @param mapEntry   the map entry associated with this game data
     * @param fileHandler the file handler for saving and loading game data
     */
    public GameData(@NotNull UUID uuid, @NotNull MapEntry mapEntry, @NotNull FileHandler fileHandler) {
        super(uuid, mapEntry, BossBar.Color.RED);
        this.fileHandler = fileHandler;
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);

        if (player == null) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " is not online.");
        }
    }

    /**
     * Swaps between area mode and normal mode.
     */
    public void swapAreaMode() {
        this.areaMode = !this.areaMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openInventory(@NotNull Player player) {
        player.openInventory(this.inventory.getInventory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerUpdate() {
        this.inventory.invalidateDataLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        if (!Files.exists(mapEntry.getMapFile())) {
            this.mapEntry.createFile();
        }
        this.fileHandler.save(mapEntry.getMapFile(), BaseMap.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        this.inventory.unregister();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData() {
        if (this.mapEntry != null) return;
        Optional<GameMap> mapData = fileHandler.load(mapEntry.getMapFile(), GameMap.class);
        mapData.ifPresentOrElse(gameMap ->
                        this.gameMapBuilder = new GameMapBuilder(gameMap),
                () -> this.gameMapBuilder = new GameMapBuilder()
        );
        this.inventory = new LobbyViewInventory(this.gameMapBuilder);

        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        AnvilLoader anvilLoader = new AnvilLoader(this.mapEntry.getDirectoryRoot());
        this.instance.setChunkLoader(anvilLoader);
        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
    }

    /**
     * Returns an indication if the area mode is active or not.
     *
     * @return true if area mode is active, false otherwise
     */
    public boolean hasAreaMode() {
        return areaMode;
    }

    /**
     * Returns the GameMapBuilder instance used for building the game map.
     *
     * @return the builder instance
     */
    public GameMapBuilder getGameMapBuilder() {
        return this.gameMapBuilder;
    }
}
