package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;

import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * TNTPlacement is responsible for placing TNT blocks in the specified positions.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public class TNTPlacement extends AreaBasePlacement<Vec> {

    /**
     * Constructs a TNTPlacement instance.
     *
     * @param instance       the instance where the TNT will be placed
     * @param blockPositions the positions where the TNT will be placed
     */
    public TNTPlacement(Instance instance, List<Vec> blockPositions) {
        super(instance, blockPositions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void place(GroundData groundData) {
        if (this.buildTask != null) return;
        Iterator<Vec> iterator = blockPositions.iterator();
        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!iterator.hasNext()) {
                stop();
                return;
            }
            for (int i = 0; i < 10 && iterator.hasNext(); i++) {
                spawnTnt(iterator.next(), groundData);
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    /**
     * Spawns a TNT entity at the specified position.
     * The TNT will fall and explode when it reaches the ground.
     *
     * @param pos        the position to spawn the TNT
     * @param groundData the ground data to use when placing the block
     */
    private void spawnTnt(Vec pos, GroundData groundData) {
        Entity tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);

        Point entityPos = pos.add(0, 5, 0);
        tntEntity.setInstance(instance, entityPos);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            placeBlock(pos, groundData);
            tntEntity.remove();
        }).delay(TaskSchedule.tick(5)).schedule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void placeBlock(Vec position, GroundData groundData) {
        this.instance.setBlock(position, groundData.groundBlock());
    }
}
