package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static net.theevilreaper.tamias.BlockAssertions.assertBlock;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TNTPlacementIntegrationTest {

    @Test
    void testTNTPlacement(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Vec start = Vec.ZERO;
        Vec end = new Vec(5, 0, 5);

        instance.loadChunk(start).join();
        instance.loadChunk(end).join();
        instance.setGenerator(unit -> unit.modifier().fill(start, end, Block.STONE));

        List<Vec> positions = new ArrayList<>();

        for (int x = start.blockX(); x <= end.blockX(); x++) {
            for (int z = start.blockZ(); z <= end.blockZ(); z++) {
                Vec pos = new Vec(x, 0, z);
                positions.add(pos);
                instance.setBlock(pos, Block.STONE);
            }
        }
        assertBlock(instance, positions, Block.STONE);

        List<Vec> testBlocks = List.of(start, end);
        TNTPlacement placement = new TNTPlacement(instance, testBlocks);
        assertNotNull(placement);
        assertInstanceOf(AreaBasePlacement.class, placement);

        placement.place(GroundDataRegistry.DEFAULT_SPAWN_DATA);
        assertTrue(placement.isRunning());

        for (int i = 0; i < 20; i++) {
            env.tick();
        }

        assertBlock(instance, testBlocks, Block.TNT);

        // Advance ticks so iterator finishes and placement stops
        for (int i = 0; i < 10 && placement.isRunning(); i++) {
            env.tick();
        }

        assertFalse(placement.isRunning());

        env.destroyInstance(instance);
    }

    @Test
    void testGroupWaitsForTheGroundAndPopsUpItsMembers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Vec center = new Vec(5, 50, 5);
        List<Vec> members = List.of(new Vec(6, 50, 5), new Vec(5, 50, 6));
        Set<Point> placedGround = ConcurrentHashMap.newKeySet();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(center, members)), placedGround::contains);
        placement.place(GroundDataRegistry.DEFAULT_SPAWN_DATA);

        for (int i = 0; i < 20; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty(), "The group must wait until the ground below it is placed");

        // Only a part of the ground is placed, the group still waits
        placedGround.add(center.sub(0, 1, 0));
        placedGround.add(members.getFirst().sub(0, 1, 0));
        for (int i = 0; i < 20; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty(), "The group must wait until the ground below all of its blocks is placed");

        placedGround.add(members.getLast().sub(0, 1, 0));
        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertEquals(center, landed.getFirst(), "The center lands first");
        assertEquals(Set.copyOf(members), Set.copyOf(landed.subList(1, landed.size())));
        assertBlock(instance, List.of(center, members.get(0), members.get(1)), Block.TNT);
        assertFalse(placement.isRunning());

        env.destroyInstance(instance);
    }

    @Test
    void testStopCancelsTheTntWhichHasNotLanded(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Vec center = new Vec(5, 50, 5);
        Vec member = new Vec(6, 50, 5);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(center, List.of(member))), _ -> true);
        placement.place(GroundDataRegistry.DEFAULT_SPAWN_DATA);

        // The group is dropped, but the center is still falling
        env.tick();
        env.tick();
        placement.stop();

        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertTrue(landed.isEmpty());
        assertEquals(Block.AIR, instance.getBlock(center));
        assertEquals(Block.AIR, instance.getBlock(member));

        env.destroyInstance(instance);
    }
}
