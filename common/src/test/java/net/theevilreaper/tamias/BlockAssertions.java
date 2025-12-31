package net.theevilreaper.tamias;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assertion utility class for blocks and positions.
 * @version 1.0.0
 * @since 0.1.0
 * @author theEvilReaper
 */
public class BlockAssertions {

    private BlockAssertions() {
        // Nothing to do here in this utility class
    }

    /**
     * Asserts that the given block is present at the specified positions.
     *
     * @param instance  the instance to check the blocks in
     * @param positions the positions to check
     * @param expected  the expected block
     */
    public static <T extends Point> void assertBlock(@NotNull Instance instance, @NotNull Collection<T> positions, @NotNull Block expected) {
        String blockName = expected.name();
        for (T testPosition : positions) {
            Block block = instance.getBlock(testPosition);
            assertNotNull(block);
            assertEquals(expected, block, "The block at " + testPosition + " should be a " + blockName);
        }
    }

    /**
     * Asserts that the given positions are present in the area.
     *
     * @param expected the expected positions
     * @param given    the given positions
     * @param <T>      the type of the position
     */
    public static <T extends Point> void assertPositions(@NotNull Set<T> expected, @NotNull Set<T> given) {
        assertEquals(expected.size(), given.size());

        for (T point : expected) {
            assertTrue(given.contains(point), "The position " + point + " should be in the area");
        }
    }
}
