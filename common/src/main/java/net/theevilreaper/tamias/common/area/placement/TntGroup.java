package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of TNT blocks which is dropped together. The center falls down and the members pop up around it once it
 * has landed. A single TNT block is a group without members.
 * A {@link TntDropType#SUPPLY} group additionally places a ring into the ground around its impact.
 *
 * @param center  the position of the falling TNT block
 * @param members the positions of the TNT blocks which pop up around the center
 * @param type    the way the group is dropped
 * @param ring    the ground positions of the ring around the impact, empty for a normal group
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record TntGroup(Vec center, List<Vec> members, TntDropType type, List<Vec> ring) {

    /**
     * Creates a new {@link TntGroup} with an unmodifiable copy of the members and the ring.
     */
    public TntGroup {
        members = List.copyOf(members);
        ring = List.copyOf(ring);
    }

    /**
     * Creates a new normal {@link TntGroup} without a ring.
     *
     * @param center  the position of the falling TNT block
     * @param members the positions of the TNT blocks which pop up around the center
     */
    public TntGroup(Vec center, List<Vec> members) {
        this(center, members, TntDropType.NORMAL, List.of());
    }

    /**
     * Returns a copy of this group with the given ring.
     *
     * @param ring the ground positions of the ring around the impact
     * @return the new group
     */
    public TntGroup withRing(List<Vec> ring) {
        return new TntGroup(this.center, this.members, this.type, ring);
    }

    /**
     * Checks if this group is a supply drop.
     *
     * @return true if the group is dropped as a supply drop
     */
    public boolean isSupply() {
        return this.type == TntDropType.SUPPLY;
    }

    /**
     * Returns all positions of this group, the center first.
     *
     * @return the positions of the group
     */
    public List<Vec> positions() {
        List<Vec> positions = new ArrayList<>(this.members.size() + 1);
        positions.add(this.center);
        positions.addAll(this.members);
        return positions;
    }
}
