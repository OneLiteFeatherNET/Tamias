package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroundDataTest {

    @Test
    void testGroundBlockWithAirUsage() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> new GroundData(Block.AIR, null),
                "The ground block can't be air"
        );
    }

    @Test
    void testInvalidAdditionalBlocks() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> new GroundData(Block.AMETHYST_BLOCK, List.of(Block.AIR)),
                "The additional blocks can't contain air"
        );
    }

    @Test
    void testGroundDataWithoutAdditionalBlocks() {
        GroundData groundData = new GroundData(Block.AMETHYST_BLOCK, null);
        assertEquals(Block.AMETHYST_BLOCK, groundData.groundBlock());
        assertNull(groundData.additionalBlocks());
        assertFalse(groundData.hasAdditionalBlocks());
    }

    @Test
    void testGroundDataWithAdditionalBLocks() {
        List<Block> additionalBlocks = List.of(Block.GLASS, Block.ACACIA_DOOR, Block.SAND);
        GroundData groundData = new GroundData(Block.AMETHYST_BLOCK, additionalBlocks);
        assertTrue(groundData.hasAdditionalBlocks());
        assertEquals(Block.AMETHYST_BLOCK, groundData.groundBlock());
        assertNotNull(groundData.additionalBlocks());
        assertEquals(2, groundData.additionalBlocks().size());
        assertListEquals(additionalBlocks, groundData.additionalBlocks());
    }

    /**
     * Asserts that the given list is equal to the expected list.
     * It only compares references not the content
     * @param expected the expected list
     * @param given the given list
     * @param <T> the type of the list
     */
    private <T> void assertListEquals(@NotNull List<T> expected, @NotNull List<T> given) {
        assertEquals(expected.size(), given.size());

        for (int i = 0; i < expected.size(); i++) {
            assertSame(expected.get(i), given.get(i));
        }
    }
}
