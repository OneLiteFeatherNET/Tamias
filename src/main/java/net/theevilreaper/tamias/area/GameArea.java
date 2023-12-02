package net.theevilreaper.tamias.area;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

public final class GameArea {
    private static final Block GROUND_BLOCK = Block.STONE;
    private static final Block SPEED_BOOST_BLOCK = Block.REDSTONE_BLOCK;

    private final Instance instance;
    private final Vec start;
    private final Vec end;

    private final List<Vec> areaPositions;
    private final List<Vec> specialBlocks;
    private final Random random = new Random();


    public GameArea(@NotNull Instance instance, @NotNull Vec start, @NotNull Vec end) {
        this.instance = instance;
        var distance = start.sub(end);
        Check.argCondition(distance.equals(Vec.ZERO), "NPE");
        this.start = start;
        this.end = end;
        this.areaPositions = new ArrayList<>();
        this.specialBlocks = new ArrayList<>();
        calculatePositions();
        calculateSpecialBlockPositions(areaPositions);
    }

    void calculatePositions() {
        for (int x = (int) start.x(); x < end.x(); x++) {
            for (int y = (int) start.y(); y < end.y(); y++) {
                for (int z = (int) start.z(); z < end.z(); z++) {
                    areaPositions.add(new Vec(x, y, z));
                }
            }
        }
    }

    public Task build() {
        var posList = new ArrayList<>(areaPositions);
        posList.sort(Comparator.comparing(pos -> {
            var x = pos.x();
            var z = pos.z();
            var y = pos.y();
            return Math.sqrt(x * x + z * z + y * y);
        }));
        return buildFromQueue(posList);
    }
    private Task buildFromQueue(List<Vec> posList) {

        var queue = new LinkedBlockingDeque<>(posList);
        return MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<Vec> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < 20; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                return;
            }
            for (Vec pos : positions) {
                if (specialBlocks.contains(pos)) {
                    instance.setBlock(pos, SPEED_BOOST_BLOCK);
                    spawnTnt(pos);
                } else {
                    instance.setBlock(pos, GROUND_BLOCK);
                }
            }
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                float progress = (float) queue.size() / posList.size();
                onlinePlayer.setLevel(100 - (int) (progress * 100));
                onlinePlayer.setExp(1 - progress);
            }
        }).repeat(5, ChronoUnit.MILLIS).schedule();
    }

    private void spawnTnt(Vec pos) {
        var tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);
        tntEntity.setInstance(instance, pos.withY(pos.y() + 20));
        tntEntity.scheduleNextTick(entity -> checkIfStillFalling(entity, instance));
    }

    private void checkIfStillFalling(Entity entity, Instance instance) {
        if (entity.isOnGround()) {
            entity.remove();
            instance.setBlock(Pos.fromPoint(entity.getPosition()), Block.TNT);
        } else {
            entity.scheduleNextTick(entity1 -> checkIfStillFalling(entity1, instance));
        }
    }

    private void calculateSpecialBlockPositions(List<Vec> posList) {
        var amountOfSpecialBlocks = (int) Math.ceil((double) posList.size() / 100);
        var blocks = new ArrayList<Vec>();
        for (int i = 0; i < amountOfSpecialBlocks; i++) {
            var randomPos = posList.get(random.nextInt(posList.size()));
            blocks.add(randomPos);
        }
        this.specialBlocks.addAll(blocks);
    }

}
