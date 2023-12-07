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
import net.theevilreaper.tamias.event.FinishBuildEvent;
import net.theevilreaper.tamias.util.Helper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

@SuppressWarnings("java:S3252")
public final class GameArea {
    private static final Block GROUND_BLOCK = Block.STONE;
    private static final Block SPEED_BOOST_BLOCK = Block.REDSTONE_BLOCK;
    private static final Block ORIGINAL_BLOCK = Block.BLUE_ICE;
    private static final int BLOCKS_PER_STEP = 20;
    private static final int TNT_SPAWN_HEIGHT = 20;
    private static final int MIN_TNT_AMOUNT = 10;
    private static final int MAX_TNT_AMOUNT = 20;
    private static final int MIN_SPEED_BOOST_AMOUNT = 10;
    private static final int MAX_SPEED_BOOST_AMOUNT = 20;

    private final Instance instance;
    private Vec start;
    private Vec end;

    private final List<Vec> areaPositions;
    private final List<Vec> specialBlocks;
    private final List<Vec> tntPositions;
    private final Random random = new Random();


    public GameArea(@NotNull Instance instance, @NotNull Vec start, @NotNull Vec end) {
        this.instance = instance;
        var distance = start.sub(end);
        Check.argCondition(distance.equals(Vec.ZERO), "NPE");
        this.start = start;
        this.end = end;
        this.areaPositions = new ArrayList<>();
        this.specialBlocks = new ArrayList<>();
        this.tntPositions = new ArrayList<>();
        calculatePositions();
        calculateSpecialBlockPositions(areaPositions);
        calculateTntPositions();
    }

    void calculatePositions() {
        var startBlockX = start.blockX();
        var startBlockZ = start.blockZ();
        var endBlockX = end.blockX();
        var endBlockZ = end.blockZ();
        var blockY = start.blockY();
        for (int x = startBlockX; x < endBlockX; x++) {
            for (int z = endBlockZ; z < startBlockZ; z++) {
                var pos = new Vec(x, blockY, z);
                var chunk = instance.getChunk(pos.chunkX(), pos.chunkZ());
                if (chunk == null) {
                    instance.loadChunk(pos.chunkX(), pos.chunkZ()).join();
                }
                if (instance.getBlock(x, blockY, z).name().equals(ORIGINAL_BLOCK.name())) {
                    instance.setBlock(x, blockY, z, Block.BARRIER);
                    areaPositions.add(new Vec(x, blockY, z));
                }
            }
        }
    }

    @NotNull
    public Task build() {
        var posList = new ArrayList<>(areaPositions);
        posList.sort(Helper.getComparator());
        return buildFromQueue(posList);
    }

    @NotNull
    private Task buildFromQueue(@NotNull List<Vec> posList) {
        var queue = new LinkedBlockingDeque<>(posList);
        return MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<Vec> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < BLOCKS_PER_STEP; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                MinecraftServer.getGlobalEventHandler().call(new FinishBuildEvent());
                return;
            }
            for (Vec pos : positions) {
                placeAtPos(pos);
            }
            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                float progress = (float) queue.size() / posList.size();
                onlinePlayer.setLevel(100 - (int) (progress * 100));
                onlinePlayer.setExp(1 - progress);
            }
        }).repeat(5, ChronoUnit.MILLIS).schedule();
    }

    private void placeAtPos(@NotNull Vec pos) {
        if (specialBlocks.contains(pos)) {
            instance.setBlock(pos, SPEED_BOOST_BLOCK);
            spawnTnt(pos);
        } else {
            instance.setBlock(pos, GROUND_BLOCK);
        }
        if (tntPositions.contains(pos)) {
            spawnTnt(pos);
        }
    }

    private void spawnTnt(@NotNull Vec pos) {
        var tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);
        tntEntity.setInstance(instance, pos.withY(pos.y() + TNT_SPAWN_HEIGHT));
        tntEntity.scheduleNextTick(this::checkIfStillFalling);
    }

    private void checkIfStillFalling(@NotNull Entity entity) {
        if (entity.isOnGround()) {
            entity.remove();
            instance.setBlock(Pos.fromPoint(entity.getPosition()), Block.TNT);
        } else {
            entity.scheduleNextTick(this::checkIfStillFalling);
        }
    }

    private void calculateSpecialBlockPositions(@NotNull List<Vec> posList) {
        var amountOfSpeedBoost = random.nextInt(MAX_SPEED_BOOST_AMOUNT - MIN_SPEED_BOOST_AMOUNT) + MIN_SPEED_BOOST_AMOUNT;
        for (int i = 0; i < amountOfSpeedBoost; i++) {
            var randomPos = posList.get(random.nextInt(posList.size()));
            specialBlocks.add(randomPos);
        }
    }

    private void calculateTntPositions() {
        var amountOfTnt = random.nextInt(MAX_TNT_AMOUNT - MIN_TNT_AMOUNT) + MIN_TNT_AMOUNT;
        for (int i = 0; i < amountOfTnt; i++) {
            var randomPos = areaPositions.get(random.nextInt(areaPositions.size()));
            tntPositions.add(randomPos);
        }
    }

}
