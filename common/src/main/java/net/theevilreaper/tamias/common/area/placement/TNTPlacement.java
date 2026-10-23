package net.theevilreaper.tamias.common.area.placement;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.ground.GroundData;

import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.TNT_DROP_HEIGHT;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.TNT_POP_DELAY;

/**
 * TNTPlacement is responsible for placing TNT blocks in the specified positions.
 * The TNT is dropped in {@link TntGroup}s: the center of a group falls down and the members pop up around it once it
 * has landed. A group is only dropped when the ground below all of its blocks is placed, so the TNT follows the
 * ground placement.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public class TNTPlacement extends AreaBasePlacement<Vec> {

    private static final int FALL_TICKS = 5;
    private static final Sound LANDING_SOUND = Sound.sound(SoundEvent.BLOCK_GRAVEL_FALL, Sound.Source.BLOCK, 1f, 0.6f);
    private static final Sound POP_SOUND = Sound.sound(SoundEvent.ENTITY_CHICKEN_EGG, Sound.Source.BLOCK, 0.8f, 1.2f);

    private final Consumer<Vec> landingConsumer;
    private final List<TntGroup> groups;
    private final Set<Task> pendingTasks;
    private final Set<Entity> fallingEntities;
    private Predicate<Point> groundCheck;
    private CompletableFuture<Void> landed;

    /**
     * Constructs a TNTPlacement instance.
     *
     * @param instance       the instance where the TNT will be placed
     * @param blockPositions the positions where the TNT will be placed
     */
    public TNTPlacement(Instance instance, List<Vec> blockPositions) {
        this(instance, _ -> {});
        this.updatePositions(blockPositions);
    }

    /**
     * Constructs a TNTPlacement instance without positions. The positions are set with
     * {@link #updateGroups(List, Predicate)}.
     *
     * @param instance        the instance where the TNT will be placed
     * @param landingConsumer receives the position of each TNT block once it is placed
     */
    public TNTPlacement(Instance instance, Consumer<Vec> landingConsumer) {
        super(instance, List.of());
        this.landingConsumer = landingConsumer;
        this.groups = new ArrayList<>();
        this.pendingTasks = ConcurrentHashMap.newKeySet();
        this.fallingEntities = ConcurrentHashMap.newKeySet();
        this.groundCheck = _ -> true;
        this.landed = CompletableFuture.completedFuture(null);
    }

    /**
     * Sets the groups which are dropped on the next run.
     *
     * @param groups      the groups in the order they should be dropped
     * @param groundCheck checks if the ground at a position is placed, a group waits until it is true for the ground
     *                    below all of its blocks
     */
    public void updateGroups(List<TntGroup> groups, Predicate<Point> groundCheck) {
        this.groups.clear();
        this.groups.addAll(groups);
        this.groundCheck = groundCheck;
        this.blockPositions.clear();
        groups.forEach(group -> this.blockPositions.addAll(group.positions()));
    }

    /**
     * Sets the positions which are dropped as single blocks on the next run, without waiting for the ground.
     *
     * @param positions the positions of the TNT blocks
     */
    @Override
    public void updatePositions(List<Vec> positions) {
        this.updateGroups(positions.stream().map(position -> new TntGroup(position, List.of())).toList(), _ -> true);
    }

    /**
     * {@inheritDoc}
     * Each tick every group whose ground is placed is dropped, so the TNT keeps up with the ground placement.
     */
    @Override
    public void place(GroundData groundData) {
        if (!tryStart()) return;
        this.landed = new CompletableFuture<>();
        List<TntGroup> queue = new ArrayList<>(this.groups);
        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (queue.isEmpty()) {
                // Only the queue is done, the pending landings must still run
                super.stop();
                completeIfLanded();
                return;
            }
            Iterator<TntGroup> iterator = queue.iterator();
            while (iterator.hasNext()) {
                TntGroup group = iterator.next();
                if (!isGroundReady(group)) continue;
                iterator.remove();
                dropGroup(group, groundData);
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    /**
     * Stops the placement and cancels the TNT blocks which have not landed yet.
     */
    @Override
    public void stop() {
        super.stop();
        this.pendingTasks.forEach(Task::cancel);
        this.pendingTasks.clear();
        this.fallingEntities.forEach(Entity::remove);
        this.fallingEntities.clear();
    }

    /**
     * Returns a future which completes once every TNT block of the current run has landed.
     * It doesn't complete when the run is stopped before.
     *
     * @return the future of the current run, a completed one if no run was started
     */
    public CompletableFuture<Void> landed() {
        return this.landed;
    }

    /**
     * Completes the future of the current run when all groups are dropped and no TNT block is falling or waiting
     * to pop up anymore.
     */
    private void completeIfLanded() {
        if (this.buildTask != null || !this.pendingTasks.isEmpty()) return;
        this.landed.complete(null);
    }

    private boolean isGroundReady(TntGroup group) {
        for (Vec position : group.positions()) {
            if (!this.groundCheck.test(position.sub(0, 1, 0))) return false;
        }
        return true;
    }

    /**
     * Lets the center of the group fall down. Once it has landed, the members pop up one after another.
     *
     * @param group      the group to drop
     * @param groundData the ground data to use when placing the blocks
     */
    private void dropGroup(TntGroup group, GroundData groundData) {
        Entity tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);

        Point entityPos = group.center().add(0, TNT_DROP_HEIGHT, 0);
        tntEntity.setInstance(instance, entityPos);
        this.fallingEntities.add(tntEntity);
        instance.sendGroupedPacket(new ParticlePacket(Particle.CLOUD, entityPos, Vec.ZERO, 0f, 5));

        schedule(FALL_TICKS, () -> {
            this.fallingEntities.remove(tntEntity);
            tntEntity.remove();
            land(group.center(), groundData);
            Point center = group.center().add(0.5);
            instance.playSound(LANDING_SOUND, center.x(), center.y(), center.z());

            List<Vec> members = group.members();
            for (int i = 0; i < members.size(); i++) {
                Vec member = members.get(i);
                schedule((i + 1) * TNT_POP_DELAY, () -> popUp(member, groundData));
            }
        });
    }

    private void popUp(Vec position, GroundData groundData) {
        land(position, groundData);
        Point center = position.add(0.5, 0.5, 0.5);
        instance.sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        instance.playSound(POP_SOUND, center.x(), center.y(), center.z());
    }

    private void land(Vec position, GroundData groundData) {
        doPlaceBlock(position, groundData);
        this.landingConsumer.accept(position);
    }

    private void schedule(int delay, Runnable runnable) {
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            this.pendingTasks.remove(task[0]);
            runnable.run();
            completeIfLanded();
        }).delay(TaskSchedule.tick(delay)).schedule();
        this.pendingTasks.add(task[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void placeBlock(Vec position, GroundData groundData) {
        this.instance.setBlock(position, groundData.groundBlock());
    }
}
