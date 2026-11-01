package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroundDataRegistryTest {

    private static final int DEFAULT_GROUNDS = 10;

    @Order(1)
    @Test
    void testDefaultCapacity() {
        assertEquals(DEFAULT_GROUNDS, GroundDataRegistry.instance().getGroundData().size());
    }

    @Order(2)
    @Test
    void testGetRandomData() {
        GroundData groundData = GroundDataRegistry.instance().getRandomData();
        assertNotNull(groundData);
        assertNotNull(groundData.groundBlock());
    }

    @Order(3)
    @Test
    void testGroundDataAdd() {
        GroundDataRegistry.instance().add(new GroundData(Block.AMETHYST_BLOCK, List.of(Block.GOLD_BLOCK)));
        assertEquals(DEFAULT_GROUNDS + 1, GroundDataRegistry.instance().getGroundData().size());
    }

    @Test
    void testDefaultAccentsDontChangeTheMovement() {
        List<Block> forbidden = List.of(Block.SOUL_SAND, Block.SLIME_BLOCK, Block.HONEY_BLOCK, Block.MAGMA_BLOCK,
                Block.ICE, Block.PACKED_ICE, Block.BLUE_ICE);
        for (GroundData data : new TamiasGroundDataRegistry().getGroundData()) {
            if (!data.hasAdditionalBlocks()) continue;
            for (Block block : data.additionalBlocks()) {
                // Speed blocks change the movement on purpose
                if (SpeedBlock.isSpeedBlock(block)) continue;
                assertFalse(forbidden.contains(block), block + " changes how " + data.groundBlock() + " plays");
                assertTrue(block.occludes(), block + " is not a full ground block");
            }
        }
    }

    @Test
    void testDefaultSpeedBlocksMatchTheGround() {
        List<GroundData> defaults = new TamiasGroundDataRegistry().getGroundData();
        // Some rounds must stay without speed blocks, so the move listener isn't always needed
        assertTrue(defaults.stream().anyMatch(GroundData::hasSpeedBlocks));
        assertTrue(defaults.stream().anyMatch(data -> !data.hasSpeedBlocks()));

        for (GroundData data : defaults) {
            if (!data.hasSpeedBlocks()) continue;
            for (Block block : data.additionalBlocks()) {
                if (!SpeedBlock.isSpeedBlock(block)) continue;
                assertFalse(block.compare(data.groundBlock()), block + " looks like the ground " + data.groundBlock());
                assertTrue(block.occludes(), block + " is not a full ground block");
            }
        }
    }

    @Test
    void testGetRandomDataKeepsTheOrderOfTheRegistry() {
        GroundDataRegistry registry = new TamiasGroundDataRegistry();
        List<GroundData> order = List.copyOf(registry.getGroundData());

        for (int i = 0; i < 20; i++) {
            registry.getRandomData();
        }

        assertEquals(order, registry.getGroundData());
    }

    @Test
    void testGetRandomDataNeverRepeatsTheLastData() {
        GroundDataRegistry registry = new TamiasGroundDataRegistry();
        GroundData last = registry.getRandomData();

        for (int i = 0; i < 100; i++) {
            GroundData next = registry.getRandomData();
            assertNotSame(last, next);
            last = next;
        }
    }
}
