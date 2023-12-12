package net.theevilreaper.tamias.area;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.event.FinishBuildEvent;
import net.theevilreaper.tamias.util.GroundData;
import net.theevilreaper.tamias.util.Helper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

import static net.theevilreaper.tamias.util.GameAreaHelper.*;

@SuppressWarnings("java:S3252")
public final class GameArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameArea.class);
    public static final Block ORIGINAL_BLOCK = Block.BLUE_ICE;
    public static final GroundData DEFAULT_GROUND_DATA = new GroundData(Block.ORANGE_TERRACOTTA, null);
    private final Instance instance;
    private final Vec start;
    private final Vec end;
    private final List<Vec> areaPositions;
    private final List<Vec> specialBlocks;
    private final List<Vec> tntPositions;
    private Random random;
    private GroundData groundData;

    public GameArea(@NotNull Instance instance, @NotNull Vec start, @NotNull Vec end) {
        this.groundData = DEFAULT_GROUND_DATA;
        this.instance = instance;
        this.start = start;
        this.end = end;
        var distance = start.sub(end);
        Check.argCondition(distance.equals(Vec.ZERO), "NPE");
        this.areaPositions = new ArrayList<>();
        this.specialBlocks = new ArrayList<>();
        this.tntPositions = new ArrayList<>();
        calculatePositions();
        //calculateSpecialBlockPositions();
        calculateTntPositions();
    }

    void calculatePositions() {
        var startBlockX = start.blockX();
        var endBlockX = end.blockX();
        if (startBlockX > endBlockX) {
            var temp = startBlockX;
            startBlockX = endBlockX;
            endBlockX = temp;
        }

        var startBlockZ = start.blockZ();
        var endBlockZ = end.blockZ();
        if (startBlockZ > endBlockZ) {
            var temp = startBlockZ;
            startBlockZ = endBlockZ;
            endBlockZ = temp;
        }

        var blockY = start.blockY();
        for (int x = startBlockX; x < endBlockX; x++) {
            for (int z = startBlockZ; z < endBlockZ; z++) {
                var pos = new Vec(x, blockY, z);
                var chunk = instance.getChunk(pos.chunkX(), pos.chunkZ());
                if (chunk == null) {
                    instance.loadChunk(pos.chunkX(), pos.chunkZ()).join();
                }
                if (Objects.equals(instance.getBlock(x, blockY, z).name(), ORIGINAL_BLOCK.name())) {
                    instance.setBlock(x, blockY, z, Block.BARRIER);
                    areaPositions.add(new Vec(x, blockY, z));
                }
            }
        }
        if (!instance.getBlock(start).name().equals(Block.AIR.name())) {
            instance.setBlock(start, Block.AIR);
        }
        if (!instance.getBlock(end).name().equals(Block.AIR.name())) {
            instance.setBlock(end, Block.AIR);
        }
        LOGGER.info("The calculated area contains {} blocks", areaPositions.size());
        this.random = new Random(this.areaPositions.size());
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
                EventDispatcher.call(new FinishBuildEvent());
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
            var groundBlock = !groundData.hasAdditionalBlocks() ? groundData.groundBlock() : groundData.additionalBlocks().get(0);
            instance.setBlock(pos, groundBlock);
        } else {
            instance.setBlock(pos, groundData.groundBlock());
        }
        if (tntPositions.contains(pos)) {
            spawnTnt(pos);
        }
    }

    private void spawnTnt(@NotNull Vec pos) {
        System.out.println("Spawning tnt at " + pos);
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

    private void calculateSpecialBlockPositions() {
        var amountOfSpeedBoost = random.nextInt(MAX_SPEED_BOOST_AMOUNT - MIN_SPEED_BOOST_AMOUNT) + MIN_SPEED_BOOST_AMOUNT;
        for (int i = 0; i < amountOfSpeedBoost; i++) {
            var randomPos = areaPositions.get(random.nextInt(areaPositions.size()));
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
