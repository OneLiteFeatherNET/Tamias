package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Task;

import net.minestom.server.timer.TaskSchedule;
import net.theevilreaper.tamias.common.area.placement.animation.FallingTntAnimation;
import net.theevilreaper.tamias.common.area.placement.animation.TntAnimation;
import net.theevilreaper.tamias.common.area.placement.animation.TntAnimationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * TNTPlacement is responsible for placing TNT blocks in the specified positions.
 * The TNT is dropped in {@link TntGroup}s: the center of a group falls down and the members pop up around it once it
 * has landed. A group is only dropped when the ground below all of its blocks is placed, so the TNT follows the
 * ground placement. How a group visually reaches its position is decided by a {@link TntAnimation}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public class TNTPlacement extends AreaBasePlacement {

    private final Consumer<Vec> landingConsumer;
    private final List<TntGroup> groups;
    private final Set<Task> pendingTasks;
    private final Set<Entity> fallingEntities;
    private final Set<Vec> remainingPositions;
    private final TntAnimationContext animationContext = new AnimationContext();
    private Predicate<Point> groundCheck;
    private Predicate<Point> areaCheck;
    private CompletableFuture<Void> landed;
    private volatile boolean stopped;

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
        super(instance);
        this.landingConsumer = landingConsumer;
        this.groups = new ArrayList<>();
        this.pendingTasks = ConcurrentHashMap.newKeySet();
        this.fallingEntities = ConcurrentHashMap.newKeySet();
        this.remainingPositions = ConcurrentHashMap.newKeySet();
        this.groundCheck = _ -> true;
        this.areaCheck = _ -> true;
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
        this.updateGroups(groups, groundCheck, _ -> true);
    }

    /**
     * Sets the groups which are dropped on the next run and the ground positions of the area.
     *
     * @param groups      the groups in the order they should be dropped
     * @param groundCheck checks if the ground at a position is placed, a group waits until it is true for the ground
     *                    below all of its blocks
     * @param areaCheck   checks if a position belongs to the ground of the area, whether it is placed yet or not; the
     *                    animations use it to stay inside the area
     */
    public void updateGroups(List<TntGroup> groups, Predicate<Point> groundCheck, Predicate<Point> areaCheck) {
        this.groups.clear();
        this.groups.addAll(groups);
        this.groundCheck = groundCheck;
        this.areaCheck = areaCheck;
    }

    /**
     * Sets the positions which are dropped as single blocks on the next run, without waiting for the ground.
     *
     * @param positions the positions of the TNT blocks
     */
    public void updatePositions(List<Vec> positions) {
        this.updateGroups(positions.stream().map(position -> new TntGroup(position, List.of())).toList(), _ -> true);
    }

    /**
     * Starts dropping the TNT groups with the {@link FallingTntAnimation}.
     * Each tick every group whose ground is placed is dropped, so the TNT keeps up with the ground placement.
     */
    public void place() {
        place(new FallingTntAnimation());
    }

    /**
     * Starts dropping the TNT groups with the given animation. The animation instance must not be used for another run.
     * Each tick every group whose ground is placed is handed to the animation, so the TNT keeps up with the ground
     * placement.
     *
     * @param animation the animation which brings the TNT to its positions
     */
    public void place(TntAnimation animation) {
        if (!tryStart()) return;
        this.stopped = false;
        this.landed = new CompletableFuture<>();
        this.remainingPositions.clear();
        this.groups.forEach(group -> this.remainingPositions.addAll(group.positions()));
        List<TntGroup> queue = new ArrayList<>(this.groups);
        animation.begin(List.copyOf(this.groups), this.animationContext);
        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (queue.isEmpty()) {
                // Only the queue is done, the pending landings must still run
                finish();
                completeIfLanded();
                return;
            }
            Iterator<TntGroup> iterator = queue.iterator();
            while (iterator.hasNext()) {
                TntGroup group = iterator.next();
                if (!isGroundReady(group)) continue;
                iterator.remove();
                animation.play(group, this.animationContext);
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    /**
     * Stops the placement and cancels the TNT blocks which have not landed yet.
     */
    @Override
    public void stop() {
        this.stopped = true;
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
     * Completes the future of the current run when every position of the run has landed.
     */
    private void completeIfLanded() {
        if (this.stopped || !this.remainingPositions.isEmpty()) return;
        this.landed.complete(null);
    }

    private boolean isGroundReady(TntGroup group) {
        for (Vec position : group.positions()) {
            if (!this.groundCheck.test(position.sub(0, 1, 0))) return false;
        }
        return true;
    }

    /**
     * Places the TNT block and reports its position. Nothing happens once the placement is stopped, because a
     * landing can still be triggered by a block which lands during the stop.
     *
     * @param position the position where the TNT lands
     */
    private void land(Vec position) {
        if (this.stopped) return;
        doPlaceBlock(position, Block.TNT);
        this.remainingPositions.remove(position);
        this.landingConsumer.accept(position);
        completeIfLanded();
    }

    private void schedule(int delay, Runnable runnable) {
        // A stopped placement must not start new landings, e.g. the members of a group which lands during the stop
        if (this.stopped) return;
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            this.pendingTasks.remove(task[0]);
            runnable.run();
        }).delay(TaskSchedule.tick(delay)).schedule();
        this.pendingTasks.add(task[0]);
    }

    /**
     * The context which is handed to the animations. It delegates to the placement, so the animations can't bypass
     * the stop guards.
     */
    private final class AnimationContext implements TntAnimationContext {

        @Override
        public Instance instance() {
            return TNTPlacement.this.instance;
        }

        @Override
        public boolean hasGround(Vec cell) {
            return TNTPlacement.this.groundCheck.test(cell.sub(0, 1, 0));
        }

        @Override
        public boolean isInArea(Vec cell) {
            return TNTPlacement.this.areaCheck.test(cell.sub(0, 1, 0));
        }

        @Override
        public void schedule(int delayTicks, Runnable action) {
            TNTPlacement.this.schedule(delayTicks, action);
        }

        @Override
        public void track(Entity entity) {
            TNTPlacement.this.fallingEntities.add(entity);
        }

        @Override
        public void release(Entity entity) {
            TNTPlacement.this.fallingEntities.remove(entity);
            entity.remove();
        }

        @Override
        public void land(Vec position) {
            TNTPlacement.this.land(position);
        }
    }
}
