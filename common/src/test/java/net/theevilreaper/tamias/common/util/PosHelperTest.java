package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PosHelperTest {

    @Test
    void testCenter2D() {
        var point = Vec.ZERO;
        var center = PosHelper.getCenter2D(point);
        assertEquals(0.5, center.x());
        assertEquals(1, center.y());
        assertEquals(0.5, center.z());
    }
}
