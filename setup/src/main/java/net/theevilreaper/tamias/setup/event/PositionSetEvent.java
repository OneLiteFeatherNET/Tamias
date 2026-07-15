package net.theevilreaper.tamias.setup.event;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

public class PositionSetEvent implements PlayerEvent {

    private final Player player;
    private final Pos pos;
    private final MapDataCategory category;

    public PositionSetEvent(Player player, Pos pos, MapDataCategory category) {
        this.player = player;
        this.pos = pos;
        this.category = category;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public Pos getPos() {
        return this.pos;
    }

    public MapDataCategory getCategory() {
        return this.category;
    }
}
