package net.theevilreaper.tamias.setup.inventory.slot;

import net.theevilreaper.aves.inventory.InventorySlot;
import net.theevilreaper.aves.inventory.slot.ISlot;
import net.theevilreaper.aves.inventory.util.InventoryConstants;
import net.theevilreaper.aves.util.Components;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.setup.inventory.DataType;
import net.theevilreaper.tamias.setup.util.FormatHelper;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("java:S3252")
public final class SpawnItemSlot extends PositionBaseSlot {

    private static final Component SPAWN_NAME = Component.text("Spawn");
    private static final Component BOMBER_NAME = Component.text("Bomber");
    private static final Component SURVIVOR_NAME = Component.text("Survivor");
    private static final ISlot EMPTY_SLOT = new InventorySlot(
            ItemStack.builder(Material.BARRIER)
                    .customName(SPAWN_NAME)
                    .lore(Component.empty(), Component.text("No data available", NamedTextColor.RED), Component.empty()),
            InventoryConstants.CANCEL_CLICK
    );

    private final ItemStack stack;

    public static @NotNull ISlot empty() {
        return EMPTY_SLOT;
    }

    /**
     * Creates a new SpawnItemSlot with the given point.
     *
     * @param point the point to teleport the player to
     * @return the created SpawnItemSlot instance
     */
    public static @NotNull SpawnItemSlot asSpawn(@NotNull Point point, @NotNull PlayerConsumer rightClickAction) {
        return new SpawnItemSlot(point, DataType.SPAWN.getMaterial(), SPAWN_NAME, rightClickAction);
    }

    /**
     * Creates a new SpawnItemSlot with the given point.
     *
     * @param point the point to teleport the player to
     * @return the created SpawnItemSlot instance
     */
    public static @NotNull SpawnItemSlot asBomber(@NotNull Point point, @NotNull PlayerConsumer rightClickAction) {
        return new SpawnItemSlot(point, DataType.BOMBER.getMaterial(), BOMBER_NAME, rightClickAction);
    }

    /**
     * Creates a new SpawnItemSlot with the given point.
     *
     * @param point the point to teleport the player to
     * @return the created SpawnItemSlot instance
     */
    public static @NotNull SpawnItemSlot asSurvivor(@NotNull Point point, @NotNull PlayerConsumer rightClickAction) {
        return new SpawnItemSlot(point, DataType.SURVIVOR.getMaterial(), SURVIVOR_NAME, rightClickAction);
    }

    /**
     * Creates a new SpawnItemSlot with the given point.
     *
     * @param point       the point to teleport the player to
     * @param material    the material of the item
     * @param displayName the display name of the item
     */
    SpawnItemSlot(@NotNull Point point, @NotNull Material material, @NotNull Component displayName, @NotNull PlayerConsumer rightClickAction) {
        super(point, rightClickAction);
        this.stack = ItemStack.builder(material)
                .customName(displayName)
                .lore(Components.pointToLore(MiniMessage.miniMessage(), point, FormatHelper.DECIMAL_FORMAT))
                .build();
        setClick(this::handleClick);
    }

    @Override
    public ItemStack getItem() {
        return this.stack;
    }
}
