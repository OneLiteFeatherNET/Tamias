package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroundCategoryTest {

    @ParameterizedTest
    @EnumSource(GroundCategory.class)
    void testCategoryBlocks(GroundCategory category) {
        List<Block> blocks = category.blocks();
        assertFalse(blocks.isEmpty());
        assertTrue(blocks.size() <= GroundCategory.MAX_SIZE, "The category must fit into the picker inventory");
        assertEquals(blocks.size(), new HashSet<>(blocks).size(), "The category must not contain duplicates");
        assertTrue(blocks.contains(category.icon()), "The icon must be part of the category");

        for (Block block : blocks) {
            String name = block.key().asString();
            assertTrue(block.solid(), name + " must be solid");
            assertTrue(block.collisionShape().isFaceFull(BlockFace.TOP), name + " must have a full top face");
            assertNotNull(block.material(), name + " must have an item to display it");
            assertFalse(block.compare(GameAreaHelper.GROUND_MARKER_BLOCK), name + " is reserved as ground marker");
            assertFalse(block.compare(Block.TNT), name + " is reserved by the game");
        }
    }
}
