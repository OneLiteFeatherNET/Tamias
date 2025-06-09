package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.area.Area;
import net.theevilreaper.tamias.common.area.SpawnArea;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static net.minestom.server.MinecraftServer.getConnectionManager;
import static net.theevilreaper.tamias.common.area.SpawnArea.SPAWN_BLOCK;


public final class SpawnPlacement implements Placement {

    private final Instance instance;
    private final Area area;

    public SpawnPlacement(@NotNull Instance instance, @NotNull Area area) {
        Check.argCondition(area instanceof SpawnArea, "The area must be a spawn area");
        this.instance = instance;
        this.area = area;
    }

    @Override
    public void clear() {
        Set<Point> positions = this.area.getPositions();
        for (Point position : positions) {
            this.instance.setBlock(position, Block.AIR);
        }
    }

    @Override
    public void triggerPlacement() {
        List<Vec> positions = new ArrayList(this.area.getPositions());
        Collection<Player> onlinePlayers = getConnectionManager().getOnlinePlayers();
        Iterator<Player> iterator = onlinePlayers.iterator();
        int counter = 0;
        while (iterator.hasNext() && counter < onlinePlayers.size()) {
            instance.setBlock(positions.get(counter++), SPAWN_BLOCK);
        }
    }
}
