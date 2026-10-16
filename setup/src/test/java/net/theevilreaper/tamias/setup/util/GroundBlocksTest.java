package net.theevilreaper.tamias.setup.util;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroundBlocksTest {

    @Test
    void testDisplayData() {
        assertEquals(Material.SAND, GroundBlocks.displayMaterial(Block.SAND));
        assertEquals("Sand", GroundBlocks.displayName(Block.SAND));
        assertEquals("Red Sand", GroundBlocks.displayName(Block.RED_SAND));
        assertEquals("Deepslate Diamond Ore", GroundBlocks.displayName(Block.DEEPSLATE_DIAMOND_ORE));
    }

    @Test
    void testBlockWithoutItemUsesBarrier() {
        assertEquals(Material.BARRIER, GroundBlocks.displayMaterial(Block.WATER));
    }
}
