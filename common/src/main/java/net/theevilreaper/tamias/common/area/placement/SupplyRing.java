package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.SUPPLY_RING_RADIUS;

/**
 * Calculates the ring which a supply drop places into the ground around its impact.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SupplyRing {

    private static final double INNER_RADIUS = SUPPLY_RING_RADIUS - 0.5;
    private static final double OUTER_RADIUS = SUPPLY_RING_RADIUS + 0.5;

    private SupplyRing() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Returns the ground positions of the ring around the given center. Only the given ground positions on the
     * height of the center are used, so the ring never leaves the area.
     *
     * @param groundCenter    the ground position below the impact
     * @param groundPositions the ground positions of the area
     * @return the positions of the ring
     */
    public static List<Vec> positions(Vec groundCenter, Collection<Vec> groundPositions) {
        List<Vec> ring = new ArrayList<>();
        for (Vec position : groundPositions) {
            if (position.blockY() != groundCenter.blockY()) continue;
            double dx = position.x() - groundCenter.x();
            double dz = position.z() - groundCenter.z();
            double distanceSquared = dx * dx + dz * dz;
            if (distanceSquared >= INNER_RADIUS * INNER_RADIUS && distanceSquared < OUTER_RADIUS * OUTER_RADIUS) {
                ring.add(position);
            }
        }
        return ring;
    }
}
