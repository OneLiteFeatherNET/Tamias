package net.theevilreaper.tamias.setup.util;

import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static net.theevilreaper.tamias.setup.util.SetupItems.AREA_DATA_FLAG;
import static net.theevilreaper.tamias.setup.util.SetupItems.GAME_DATA_FLAG;
import static net.theevilreaper.tamias.setup.util.SetupItems.MAPS_FLAG;
import static net.theevilreaper.tamias.setup.util.SetupItems.OVERVIEW_FLAG;
import static net.theevilreaper.tamias.setup.util.SetupItems.SAVE_MAP_FLAG;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class SetupItemsTest {

    @Test
    void testOverViewItem(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());

        SetupItems.setOverViewItem(player);

        assertEquals(Material.CHEST, player.getInventory().getItemStack(0x00).material());
        assertEquals(MAPS_FLAG, player.getInventory().getItemStack(0x00).getTag(Tags.ITEM_TAG).byteValue());
        assertEquals(0x00, player.getHeldSlot());
    }

    @Test
    void testSaveMapItem(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());

        SetupItems.setSaveItem(player);

        assertEquals(Material.BELL, player.getInventory().getItemStack(0x07).material());
        assertEquals(SAVE_MAP_FLAG, player.getInventory().getItemStack(0x07).getTag(Tags.ITEM_TAG).byteValue());
        assertEquals(0x00, player.getHeldSlot());
    }

    @Test
    void testMapLayoutIsSymmetric(@NotNull Env env) {
        Player player = env.createPlayer(env.createFlatInstance());

        SetupItems.setSaveItem(player);

        Map<Integer, Byte> occupiedSlots = Map.of(0x01, OVERVIEW_FLAG, 0x03, GAME_DATA_FLAG, 0x05, AREA_DATA_FLAG, 0x07, SAVE_MAP_FLAG);
        occupiedSlots.forEach((slot, flag) ->
                assertEquals(flag, player.getInventory().getItemStack(slot).getTag(Tags.ITEM_TAG).byteValue()));

        for (int slot : new int[]{0x00, 0x02, 0x04, 0x06, 0x08}) {
            assertEquals(Material.AIR, player.getInventory().getItemStack(slot).material(), "slot " + slot + " must stay empty");
        }
    }
}
