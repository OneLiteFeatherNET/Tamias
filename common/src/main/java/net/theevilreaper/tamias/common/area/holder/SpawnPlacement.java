package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.Area;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

import static net.minestom.server.MinecraftServer.getConnectionManager;
import static net.theevilreaper.tamias.common.area.SpawnArea.SPAWN_BLOCK;

public final class SpawnPlacement implements Placement {

    private final Instance instance;
    private final Area area;

    public SpawnPlacement(@NotNull Instance instance, @NotNull Area area) {
        if (!(area instanceof SpawnArea)) {
            throw new IllegalArgumentException("The instance must be an instance");
        }
        this.instance = instance;
        this.area = area;
    }

    @Override
    public void clear() {
        Collection<Point> positions = this.area.getPositions();
        for (Point position : positions) {
            this.instance.setBlock(position, Block.AIR);
        }
    }

    @Override
    public void triggerPlacement(@NotNull GroundData groundData) {
        Iterator<Point> positionIterator = this.area.getPositions().iterator();
        int maxPositionsToBuild = getConnectionManager().getOnlinePlayerCount();

        for (int i = 0; i < maxPositionsToBuild && positionIterator.hasNext(); i++) {
            Point point = positionIterator.next();
            instance.setBlock(point, SPAWN_BLOCK);
        }
    }
}
