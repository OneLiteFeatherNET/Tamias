package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeedBlockTest {

    @Test
    void testSpeedBlockKeepsTheBlockType() {
        Block block = SpeedBlock.of(Block.SEA_LANTERN, 10);
        assertTrue(block.compare(Block.SEA_LANTERN));
        assertTrue(SpeedBlock.isSpeedBlock(block));
        assertEquals(10, SpeedBlock.getBoost(block));
    }

    @Test
    void testDefaultBoost() {
        assertEquals(SpeedBlock.DEFAULT_BOOST, SpeedBlock.getBoost(SpeedBlock.of(Block.PACKED_ICE)));
    }

    @Test
    void testNormalBlockIsNoSpeedBlock() {
        assertFalse(SpeedBlock.isSpeedBlock(Block.SEA_LANTERN));
        assertEquals(0, SpeedBlock.getBoost(Block.SEA_LANTERN));
    }

    @Test
    void testInvalidSpeedBlocks() {
        assertThrowsExactly(IllegalArgumentException.class, () -> SpeedBlock.of(Block.AIR));
        assertThrowsExactly(IllegalArgumentException.class, () -> SpeedBlock.of(Block.STONE, 0));
    }
}
