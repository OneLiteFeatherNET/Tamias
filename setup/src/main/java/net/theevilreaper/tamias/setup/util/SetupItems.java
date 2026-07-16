package net.theevilreaper.tamias.setup.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.theevilreaper.aves.hotbar.HotBarLayout;
import net.theevilreaper.tamias.common.util.Tags;

/**
 * The class holds all item references that are required in the setup process of a map.
 * Each item uses a {@link Tag<Byte>} to identify which functionality the item has.
 * This behavior is easier to use because it doesn't require additional references in the event class to check the item.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class SetupItems {

    public static final byte MAPS_FLAG = 0x00;
    public static final byte SAVE_MAP_FLAG = 0x01;
    public static final byte OVERVIEW_FLAG = 0x02;

    public static final ItemStack DECORATION = ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
            .customName(Component.empty())
            .build();

    private static final HotBarLayout generalLayout;
    private static final HotBarLayout mapLayout;

    static {
        generalLayout = new HotBarLayout();
        generalLayout.set(0x00, ItemStack.builder(Material.CHEST)
                .customName(Component.text("Maps", NamedTextColor.GREEN))
                .set(Tags.ITEM_TAG, MAPS_FLAG)
                .build()
        );
        mapLayout = new HotBarLayout();
        mapLayout.set(0x02, ItemStack.builder(Material.COMPASS)
                .customName(Component.text("View data", NamedTextColor.AQUA))
                .set(Tags.ITEM_TAG, OVERVIEW_FLAG)
                .build()
        );
        mapLayout.set(0x06, ItemStack.builder(Material.BELL)
                .customName(Component.text("Save map", NamedTextColor.RED))
                .set(Tags.ITEM_TAG, SAVE_MAP_FLAG)
                .build()
        );
    }

    /**
     * Sets the overview item for the given player.
     *
     * @param player the player to set the item
     */
    public static void setOverViewItem(Player player) {
        player.getInventory().clear();
        generalLayout.apply(player);
        player.setHeldItemSlot((byte) 0);
    }

    /**
     * Sets the save item for the given player.
     *
     * @param player the player to set the item
     */
    public static void setSaveItem(Player player) {
        player.getInventory().clear();
        mapLayout.apply(player);
        player.setHeldItemSlot((byte) 0);
    }

    private SetupItems() {
        // Nothing to do here
    }
}
