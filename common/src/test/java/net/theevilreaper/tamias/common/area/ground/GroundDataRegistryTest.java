package net.theevilreaper.tamias.common.area.ground;

import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroundDataRegistryTest {

    @Order(1)
    @Test
    void testDefaultCapacity() {
        assertEquals(4, GroundDataRegistry.instance().getGroundData().size());
    }

    @Order(2)
    @Test
    void testGetRandomData() {
        GroundData groundData = GroundDataRegistry.instance().getRandomData();
        assertNotNull(groundData);
        assertNull(groundData.additionalBlocks());
    }

    @Order(3)
    @Test
    void testGroundDataAdd() {
        GroundDataRegistry.instance().add(new GroundData(Block.AMETHYST_BLOCK, List.of(Block.GOLD_BLOCK)));
        assert GroundDataRegistry.instance().getGroundData().size() > 4;
        assertEquals(5, GroundDataRegistry.instance().getGroundData().size());
    }
}