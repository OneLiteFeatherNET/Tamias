package net.theevilreaper.tamias.setup;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * The class holds all item reference which are required in the setup process of a map.
 * Each item uses a {@link Tag<Byte>} to identify which functionality the item has.
 * This behaviour is easier to use because it doesn't require additional references in the event class to check the item.
 * @author theEvilReaper
 * @since 1.0.0
 * @version 1.0.0
 */
public final class SetupItems {

    private final ItemStack overview;
    private final ItemStack save;

    public SetupItems(@NotNull Tag<Byte> itemTag) {
        this.overview = ItemStack.builder(Material.CHEST)
                .displayName(Component.text("Maps", NamedTextColor.GREEN))
                .meta(builder -> builder.setTag(itemTag, (byte) 0))
                .build();
        this.save = ItemStack.builder(Material.BARRIER)
                .displayName(Component.text("Save map", NamedTextColor.RED))
                .meta(builder -> builder.setTag(itemTag, (byte) 1))
                .build();
    }

    public void setOverViewItem(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().setItemStack(0, this.overview);
        player.setHeldItemSlot((byte) 0);
    }

    public void setSaveItem(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().setItemStack(8, this.save);
    }
}
