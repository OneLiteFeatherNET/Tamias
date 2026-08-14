package net.theevilreaper.tamias.setup.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

public class DirectionSetEvent implements PlayerEvent {

    private final Player player;
    private final Direction direction;
    private final MapDataCategory category;

    public DirectionSetEvent(Player player, Direction direction, MapDataCategory category) {
        this.player = player;
        this.direction = direction;
        this.category = category;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public MapDataCategory getCategory() {
        return this.category;
    }
}
