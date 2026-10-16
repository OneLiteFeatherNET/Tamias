package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of TNT blocks which is dropped together. The center falls down and the members pop up around it once it
 * has landed. A single TNT block is a group without members.
 *
 * @param center  the position of the falling TNT block
 * @param members the positions of the TNT blocks which pop up around the center
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record TntGroup(Vec center, List<Vec> members) {

    /**
     * Creates a new {@link TntGroup} with an unmodifiable copy of the members.
     */
    public TntGroup {
        members = List.copyOf(members);
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
