package net.theevilreaper.tamias.setup.data;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.Direction;
import net.onelitefeather.falco.anvil.FalcoAnvilLoader;
import net.onelitefeather.guira.data.SetupData;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * Base implementation for setup data working with a temporary map instance.
 *
 * <p>Provides shared functionality for managing setup state, boss bar updates,
 * instance lifecycle handling and map-related access. Subclasses implement
 * setup-specific interaction and update logic.</p>
 *
 * @author theEvilReaper
 * @version 2.0.0
 * @since 2.1.0
 */
public abstract class InstanceSetupData implements SetupData {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InstanceSetupData.class);
    protected static final Pos SPAWN_POINT = new Pos(0, 100, 0);

    protected UUID uuid;
    protected MapEntry mapEntry;
    protected @Nullable InstanceContainer instance;
    protected @Nullable FalcoAnvilLoader chunkLoader;
    protected BossBar bossBar;
    protected @Nullable Component title;

    /**
     * Creates a new setup data container.
     *
     * @param uuid     unique identifier of this setup session
     * @param mapEntry map entry associated with the setup
     * @param color    boss bar color used for setup feedback
     */
    protected InstanceSetupData(UUID uuid, MapEntry mapEntry, BossBar.Color color) {
        this.uuid = uuid;
        this.mapEntry = mapEntry;
        this.bossBar = BossBar.bossBar(Component.empty(), 1, color, BossBar.Overlay.PROGRESS);
    }

    /**
     * Updates the boss bar title.
     *
     * <p>If no title is set, a fallback title is used.</p>
     */
    public void updateTitle() {
        if (this.title == null) {
            this.title = Component.text("Please set a title", NamedTextColor.RED);
        }
        this.bossBar.name(title);
    }

    /**
     * Displays setup-related information to the given player.
     *
     * @param player target player
     */
    public void teleport(Player player) {
        player.showBossBar(this.bossBar);
    }

    /**
     * Opens the setup inventory for the given player.
     *
     */
    public abstract void openInventory(InventoryTarget target);

    /**
     * Triggers an update of the setup state and related visual feedback.
     */
    public abstract void triggerUpdate(InventoryTarget target);

    /**
     * Handles the position change of a player during the setup.
     *
     * @param category the category of the map data being modified
     * @param player   who is involved in the position change
     */
    public abstract void setPosition(MapDataCategory category, Player player);

    /**
     * Handles a direction change for the given category.
     * The default implementation does nothing. Subclasses may override this to handle direction-based categories.
     *
     * @param category  the category the direction belongs to
     * @param player    the player who triggered the change
     * @param direction the new direction
     */
    public void setDirection(MapDataCategory category, Player player, Direction direction) {
        // Nothing to do in the default implementation
    }

    /**
     * Handles an item interaction by the given player during the setup process.
     * The default implementation opens the general inventory.
     * Subclasses may override this to handle additional tag values.
     *
     * @param player   the player who interacted with the item
     * @param tagValue the tag value of the used item
     * @see GameData#handleItemInteraction(Player, byte)
     */
    public void handleItemInteraction(Player player, byte tagValue) {
        openInventory(InventoryTarget.GENERAL);
    }

    public abstract void handleDataDelete(MapDataCategory category);

    public void handleDataContextDelete(MapDataCategory category, Point point) {
        // Nothing to do in the default implementation
    }

    /**
     * Checks whether a map file is available for this setup.
     *
     * @return true if a map file exists, otherwise false
     */
    public boolean hasMapFile() {
        return this.mapEntry.hasMapFile();
    }

    /**
     * Resets this setup, unregisters its instance and closes its chunk loader if present.
     */
    @Override
    public void reset() {
        if (instance == null) return;
        MinecraftServer.getInstanceManager().unregisterInstance(instance);

        if (this.chunkLoader == null) return;
        try {
            this.chunkLoader.close();
        } catch (IOException exception) {
            LOGGER.error("Failed to close FalcoAnvilLoader", exception);
        }
        this.chunkLoader = null;
    }

    /**
     * Returns the unique identifier of this setup.
     *
     * @return setup id
     */
    @Override
    public UUID getId() {
        return this.uuid;
    }

    /**
     * Returns the map builder used by this setup.
     *
     * @return associated map builder
     */
    public abstract BaseMapBuilder getMapBuilder();

    /**
     * Compares the coordinates of two points, ignoring type, yaw and pitch.
     *
     * @param a first point, may be null
     * @param b second point, may be null
     * @return true if both points are non-null and share the same x, y and z coordinates
     */
    protected static boolean samePosition(@Nullable Point a, @Nullable Point b) {
        if (a == null || b == null) return false;
        return a.x() == b.x() && a.y() == b.y() && a.z() == b.z();
    }

    public enum InventoryTarget {

        GENERAL,
        GAME,
        AREA,

    }
}