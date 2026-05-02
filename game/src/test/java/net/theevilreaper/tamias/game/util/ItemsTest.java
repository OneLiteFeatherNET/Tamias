package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ItemsTest {

    @Test
    void testShootItemSet(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        assertNotNull(player);

        Items.setShootItem(player);
        ItemStack eye = player.getInventory().getItemStack(0x00);
        assertNotNull(eye);
        assertNotEquals(Material.AIR, eye.material());
        assertEquals(Material.IRON_HOE, eye.material());
        assertEquals(0x00, eye.getTag(Tags.ITEM_TAG).byteValue());

        env.destroyInstance(instance, true);
    }

    @Test
    void testBombItemSet(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        assertNotNull(player);

        Items.setBombItem(player);
        ItemStack bomb = player.getInventory().getItemStack(0x00);
        assertNotNull(bomb);
        assertNotEquals(Material.AIR, bomb.material());
        assertEquals(Material.TNT, bomb.material());
        assertEquals(0x01, bomb.getTag(Tags.ITEM_TAG).byteValue());

        env.destroyInstance(instance, true);
    }
}
