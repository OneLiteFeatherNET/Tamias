package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.placement.TNTPlacement;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TntAnimationIntegrationTest {

    private static final Vec CENTER = new Vec(5, 50, 5);
    private static final Vec MEMBER = new Vec(6, 50, 5);
    private static final Vec OTHER_CENTER = new Vec(10, 50, 10);

    @Test
    void testBeginIsCalledOnceWithAllGroupsBeforeAnyPlay(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<TntGroup> groups = List.of(new TntGroup(CENTER, List.of(MEMBER)), new TntGroup(OTHER_CENTER, List.of()));
        RecordingAnimation animation = new RecordingAnimation();
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(groups, _ -> true);
        placement.place(animation);

        tick(env, 3);

        assertEquals(List.of("begin", "play", "play"), animation.events, "begin must run once before the plays");
        assertEquals(groups, animation.beganGroups, "begin must receive all groups of the run");
        env.destroyInstance(instance);
    }

    @Test
    void testPlayIsCalledOnceForEachGroup(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<TntGroup> groups = List.of(new TntGroup(CENTER, List.of(MEMBER)), new TntGroup(OTHER_CENTER, List.of()));
        RecordingAnimation animation = new RecordingAnimation();
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(groups, _ -> true);
        placement.place(animation);

        tick(env, 30);

        assertEquals(groups, animation.playedGroups, "Every group must be played exactly once");
        env.destroyInstance(instance);
    }

    @Test
    void testLandedCompletesAfterTheLastLandingWithoutPendingTasks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        ManualLandingAnimation animation = new ManualLandingAnimation();
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(animation);
        CompletableFuture<Void> landed = placement.landed();

        // The animation schedules nothing, the queue is empty after the first tick
        tick(env, 3);
        animation.context.land(CENTER);
        tick(env, 3);
        assertFalse(landed.isDone(), "The member has not landed yet");

        animation.context.land(MEMBER);
        assertTrue(landed.isDone(), "The run must complete once the last position has landed");
        env.destroyInstance(instance);
    }

    @Test
    void testLandedDoesNotCompleteWhenAPositionNeverLands(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        ManualLandingAnimation animation = new ManualLandingAnimation();
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(animation);
        CompletableFuture<Void> landed = placement.landed();

        animation.context.land(CENTER);
        tick(env, 50);

        assertFalse(landed.isDone(), "A run with a position which never lands must not complete");
        env.destroyInstance(instance);
    }

    @Test
    void testLandAfterStopPlacesNoBlockAndReportsNoPosition(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<Vec> landed = new ArrayList<>();
        ManualLandingAnimation animation = new ManualLandingAnimation();
        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(animation);
        tick(env, 3);

        placement.stop();
        animation.context.land(CENTER);

        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT must be placed after the stop");
        assertTrue(landed.isEmpty(), "No landing must be reported after the stop");
        env.destroyInstance(instance);
    }

    @Test
    void testTrackedEntityIsRemovedOnStop(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Entity entity = spawn(instance, CENTER.add(0, 5, 0));
        TrackingAnimation animation = new TrackingAnimation(entity);
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(animation);
        tick(env, 2);
        assertFalse(entity.isRemoved(), "A tracked entity must live until the placement stops");

        placement.stop();

        assertTrue(entity.isRemoved(), "A tracked entity must be removed when the placement stops");
        env.destroyInstance(instance);
    }

    @Test
    void testReleasedEntityIsRemovedImmediately(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Entity entity = spawn(instance, CENTER.add(0, 5, 0));
        ReleasingAnimation animation = new ReleasingAnimation(entity);
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(animation);
        tick(env, 3);

        assertTrue(animation.removedAfterRelease, "A released entity must be removed by the release call itself");
        assertTrue(entity.isRemoved());
        env.destroyInstance(instance);
    }

    private static void tick(Env env, int ticks) {
        for (int i = 0; i < ticks; i++) {
            env.tick();
        }
    }

    private static Entity spawn(Instance instance, Vec position) {
        Entity entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, position).join();
        return entity;
    }

    /**
     * Records the calls of a run, the test asserts the order and the groups.
     */
    private static final class RecordingAnimation implements TntAnimation {

        private final List<String> events = new ArrayList<>();
        private final List<TntGroup> playedGroups = new ArrayList<>();
        private List<TntGroup> beganGroups;

        @Override
        public void begin(List<TntGroup> groups, TntAnimationContext context) {
            this.events.add("begin");
            this.beganGroups = groups;
        }

        @Override
        public void play(TntGroup group, TntAnimationContext context) {
            this.events.add("play");
            this.playedGroups.add(group);
        }
    }

    /**
     * Schedules nothing, the test lands the positions by hand through the context.
     */
    private static final class ManualLandingAnimation implements TntAnimation {

        private TntAnimationContext context;

        @Override
        public void begin(List<TntGroup> groups, TntAnimationContext context) {
            this.context = context;
        }

        @Override
        public void play(TntGroup group, TntAnimationContext context) {
            // The test lands the positions by hand
        }
    }

    /**
     * Tracks the given entity when the run starts.
     */
    private static final class TrackingAnimation implements TntAnimation {

        private final Entity entity;

        private TrackingAnimation(Entity entity) {
            this.entity = entity;
        }

        @Override
        public void begin(List<TntGroup> groups, TntAnimationContext context) {
            context.track(this.entity);
        }

        @Override
        public void play(TntGroup group, TntAnimationContext context) {
            // Nothing to animate
        }
    }

    /**
     * Releases the given entity when the group is played and records whether it was removed right away.
     */
    private static final class ReleasingAnimation implements TntAnimation {

        private final Entity entity;
        private boolean removedAfterRelease;

        private ReleasingAnimation(Entity entity) {
            this.entity = entity;
        }

        @Override
        public void play(TntGroup group, TntAnimationContext context) {
            context.release(this.entity);
            this.removedAfterRelease = this.entity.isRemoved();
        }
    }
}
