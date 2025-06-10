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
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

public class TNTPlacement extends AreaBasePlacement<Vec> {

    public TNTPlacement(@NotNull Instance instance, @NotNull List<Vec> blockPositions) {
        super(instance, blockPositions);
    }

    @Override
    public void place(@NotNull GroundData groundData) {
        if (this.buildTask != null) return;
        Iterator<Vec> iterator = blockPositions.iterator();
        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (int i = 0; i < 10 && iterator.hasNext(); i++) {
                spawnTnt(iterator.next());
            }
        }).delay(Duration.ofMillis(100)).schedule();
    }

    private void spawnTnt(@NotNull Vec pos) {
        Entity tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);

        Point entityPos = pos.add(0, 5, 0);
        tntEntity.setInstance(instance, entityPos);

        // Schedule a check to see if the entity is still falling
        tntEntity.scheduler().buildTask(() -> checkIfStillFalling(tntEntity, pos))
                .delay(Duration.ofMillis(500))
                .schedule();
    }

    private void checkIfStillFalling(@NotNull Entity entity, @NotNull Vec originalPos) {
        if (!entity.isOnGround()) {
            // Schedule with a longer delay to reduce resource usage
            entity.scheduler().buildTask(() -> checkIfStillFalling(entity, originalPos))
                    .delay(Duration.ofMillis(100))
                    .schedule();
        } else {
            placeBlock(originalPos, GroundDataRegistry.DEFAULT_SPAWN_DATA);
            entity.remove();
        }
    }

    protected void placeBlock(@NotNull Vec position, @NotNull GroundData groundData) {
        this.instance.setBlock(position, groundData.groundBlock());
    }
}
