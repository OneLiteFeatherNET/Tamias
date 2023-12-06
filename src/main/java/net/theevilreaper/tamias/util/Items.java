package net.theevilreaper.tamias.util;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class Items {

    private final ItemStack.Builder shootItem;
    private final ItemStack bombItem;

    public Items() {
        this.shootItem = ItemStack.builder(Material.IRON_HOE)
                .meta(builder -> builder.setTag(Tags.ITEM_TAG, (byte)0));
        this.bombItem = ItemStack.builder(Material.TNT)
                .meta(builder -> builder.setTag(Tags.ITEM_TAG, (byte)1))
                .build();
    }

    public void setShootItem(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(shootItem.build());
    }

    public void setBombItem(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().addItemStack(this.bombItem);
    }
}
