package net.theevilreaper.tamias.setup.util;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroundBlocksTest {

    @Test
    void testFullBlocksAreAllowed() {
        assertTrue(GroundBlocks.isAllowed(Block.SAND));
        assertTrue(GroundBlocks.isAllowed(Block.ICE));
        assertTrue(GroundBlocks.isAllowed(Block.ORANGE_TERRACOTTA));
    }

    @Test
    void testReservedBlocksAreRejected() {
        assertFalse(GroundBlocks.isAllowed(GameAreaHelper.GROUND_MARKER_BLOCK));
        assertFalse(GroundBlocks.isAllowed(Block.TNT));
    }

    @Test
    void testNonGroundBlocksAreRejected() {
        assertFalse(GroundBlocks.isAllowed(Block.AIR));
        assertFalse(GroundBlocks.isAllowed(Block.WATER));
        assertFalse(GroundBlocks.isAllowed(Block.OAK_SLAB));
        assertFalse(GroundBlocks.isAllowed(Block.TORCH));
    }

    @Test
    void testFromItem() {
        assertEquals(Block.SAND, GroundBlocks.fromItem(ItemStack.of(Material.SAND)));
        assertNull(GroundBlocks.fromItem(ItemStack.of(Material.STICK)));
        assertNull(GroundBlocks.fromItem(ItemStack.of(Material.TNT)));
        assertNull(GroundBlocks.fromItem(ItemStack.AIR));
    }

    @Test
    void testDisplayData() {
        assertEquals(Material.SAND, GroundBlocks.displayMaterial(Block.SAND));
        assertEquals("sand", GroundBlocks.displayName(Block.SAND));
    }
}
