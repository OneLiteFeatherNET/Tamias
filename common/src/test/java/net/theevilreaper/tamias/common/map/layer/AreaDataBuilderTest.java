package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaDataBuilderTest {

    @Test
    void testAreaDataBuilderUsageWithExistingValues() {
        AreaData areaData = new AreaDataBuilder()
                .lowerCorner(Vec.ONE)
                .upperCorner(Vec.ZERO)
                .facing(Direction.NORTH)
                .build();

        assertNotNull(areaData);

        AreaData.Builder builder = AreaData.builder(areaData);
        assertNotNull(builder);
        assertEquals(areaData.lowerCorner(), builder.lowerCorner());
        assertEquals(areaData.upperCorner(), builder.upperCorner());
        assertEquals(areaData.facing(), builder.facing());

        builder.facing(Direction.NORTH).lowerCorner(new Vec(10, 20, 30));

        AreaData newAreaData = builder.build();
        assertNotNull(newAreaData);
        assertEquals(new Vec(10, 20, 30), newAreaData.lowerCorner());
        assertEquals(Vec.ZERO, newAreaData.upperCorner());
        assertEquals(areaData.upperCorner(), newAreaData.upperCorner());
        assertEquals(Direction.NORTH, newAreaData.facing());
    }
}
