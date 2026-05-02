package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;

/**
 * The class provides some helper methods to set the items for the player.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class Items {

    private static final ItemStack SHOOT_ITEM;
    private static final ItemStack BOMB_ITEM;

    static {
        SHOOT_ITEM = ItemStack.builder(Material.IRON_HOE)
                .set(Tags.ITEM_TAG, (byte) 0)
                .build();
        BOMB_ITEM = ItemStack.builder(Material.TNT)
                .set(Tags.ITEM_TAG, (byte) 1).build();
    }

    /**
     * Sets the shoot item for the given player
     *
     * @param player the player to set the item
     */
    public static void setShootItem(Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(SHOOT_ITEM);
    }

    /**
     * Sets the bomb item for the given player
     *
     * @param player the player to set the item
     */
    public static void setBombItem(Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(BOMB_ITEM);
    }

    /**
     * Sets the item to the player based on the team id.
     *
     * @param player who should get the item
     * @param teamId the team id
     */
    public static void setItemToPlayer(Player player, int teamId) {
        if (teamId == GameConfig.SURVIVOR_ID) {
            setShootItem(player);
        } else {
            setBombItem(player);
        }
    }

    private Items() {
        throw new UnsupportedOperationException("Utility class");
    }
}
