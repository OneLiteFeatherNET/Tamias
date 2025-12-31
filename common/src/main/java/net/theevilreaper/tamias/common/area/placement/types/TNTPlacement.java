package net.theevilreaper.tamias.common.area.placement.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.placement.AreaBasePlacement;
import net.theevilreaper.tamias.common.ground.GroundData;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TNTPlacement extends AreaBasePlacement<Vec> {

    private static final int BLOCKS_PER_STEP = 10;
    private static final Duration STEP_DELAY = Duration.ofMillis(100);
    private static final double SPAWN_HEIGHT = 5.0;
    private static final Duration POLL_DELAY = Duration.ofMillis(50);

    private final Deque<Vec> queue = new ArrayDeque<>();

    public TNTPlacement(Instance instance, List<Vec> blockPositions) {
        super(instance, blockPositions);
        this.queue.addAll(blockPositions);
    }

    /**
     * Startet das TNT Placement.
     * Ein Task arbeitet die Queue in Steps ab.
     */
    @Override
    public void place(GroundData groundData) {
        if (this.buildTask != null) return; // bereits aktiv

        this.buildTask = MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    for (int i = 0; i < BLOCKS_PER_STEP && !queue.isEmpty(); i++) {
                        Vec pos = queue.poll();
                        spawnTnt(pos, groundData);
                    }

                    if (queue.isEmpty()) {
                        buildTask.cancel();
                        buildTask = null;
                    }
                })
                .repeat(STEP_DELAY)
                .schedule();
    }

    private void spawnTnt(Vec pos, GroundData groundData) {
        Entity tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta meta = (FallingBlockMeta) tntEntity.getEntityMeta();
        meta.setBlock(Block.TNT);

        tntEntity.setInstance(instance, pos.add(0, SPAWN_HEIGHT, 0));

        checkIfStillFalling(tntEntity, pos, groundData);
    }

    /**
     * Prüft, ob das TNT noch fällt.
     * Setzt den Block, wenn es landet, und entfernt die Entity.
     */
    private void checkIfStillFalling(Entity entity, Vec pos, GroundData groundData) {
        entity.scheduler().buildTask(() -> {
                    if (entity.isOnGround()) {
                        placeBlock(pos, groundData);
                        entity.remove();
                    }
                })
                .repeat(POLL_DELAY)
                .schedule();
    }


    /**
     * Platziert den Block am Boden.
     */
    @Override
    protected void placeBlock(Vec position, GroundData groundData) {
        instance.setBlock(position, groundData.groundBlock());
    }

    public void cancelPlacement() {
        if (buildTask != null) {
            buildTask.cancel();
            buildTask = null;
        }
        queue.clear();
    }
}
