package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.ground.GroundData;

import java.util.Collection;

import static net.minestom.server.MinecraftServer.getConnectionManager;

public final class SpawnPlacement implements Placement {

    private final Instance instance;
    private final SpawnArea area;

    public SpawnPlacement(Instance instance, SpawnArea area) {
        this.instance = instance;
        this.area = area;
    }

    @Override
    public void clear() {
        Collection<Point> positions = this.area.getPositions();
        for (Point position : positions) {
            if (instance.getBlock(position) != Block.AIR) {
                instance.setBlock(position, Block.AIR);
            }
        }
    }

    @Override
    public void triggerPlacement(GroundData data) {
        int intMaxPlayerCount = getConnectionManager().getOnlinePlayerCount();
        int i = 0;

        for (Point point : area.getPositions()) {
            if (i++ >= intMaxPlayerCount) break;
            instance.setBlock(point, data.groundBlock());
        }
    }
}
