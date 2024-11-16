package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;

/**
 * The class provides some helper methods to set the items for the player.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class Items {

    private final ItemStack.Builder shootItem;
    private final ItemStack bombItem;

    /**
     * Constructs a new instance from this class.
     */
    public Items() {
        this.shootItem = ItemStack.builder(Material.IRON_HOE)
                .set(Tags.ITEM_TAG, (byte) 0);
        this.bombItem = ItemStack.builder(Material.TNT)
                .set(Tags.ITEM_TAG, (byte) 1).build();
    }

    /**
     * Sets the shoot item for the given player
     *
     * @param player the player to set the item
     */
    public void setShootItem(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(shootItem.build());
    }

    /**
     * Sets the bomb item for the given player
     *
     * @param player the player to set the item
     */
    public void setBombItem(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(this.bombItem);
    }
}
