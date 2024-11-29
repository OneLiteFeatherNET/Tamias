package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.area.placement.AreaPlacement;
import net.theevilreaper.tamias.common.area.placement.CircleAreaPlacement;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.*;

@SuppressWarnings("java:S3252")
public final class GameArea implements Area {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameArea.class);
    public static final Block ORIGINAL_BLOCK = Block.BLUE_ICE;
    public static final GroundData DEFAULT_GROUND_DATA = new GroundData(Block.ORANGE_TERRACOTTA, null);

    private final Instance instance;
    private final GameAreaData gameAreaData;
    private final List<Vec> areaPositions;
    private final List<Vec> specialBlocks;
    private final List<Vec> tntPositions;
    private final GroundData groundData;
    private final AreaPlacement areaPlacement;

    public GameArea(@NotNull Instance instance, @NotNull GameAreaData gameAreaData) {
        this.groundData = DEFAULT_GROUND_DATA;
        this.instance = instance;
        this.gameAreaData = gameAreaData;
        this.areaPositions = new ArrayList<>();
        this.specialBlocks = new ArrayList<>();
        this.tntPositions = new ArrayList<>();
        calculatePositions();
        //calculateSpecialBlockPositions();
        calculateTntPositions();
        this.areaPlacement = new CircleAreaPlacement(this.areaPositions, () -> this.tntPositions, this::placeAtPos);
    }

    @Override
    public void calculatePositions() {
        Check.argCondition(!this.areaPositions.isEmpty(), "The calculation only can runs at once");
        var start = gameAreaData.lowerCorner();
        var end = gameAreaData.upperCorner();
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
    }

    @Override
    public void triggerPlacement() {
        if (this.areaPlacement.isRunning()) return;
        this.areaPlacement.place();
    }

    @Override
    public void reset() {
        for (Vec tntPosition : this.tntPositions) {
            instance.setBlock(tntPosition, Block.AIR);
        }
        this.tntPositions.clear();
        for (Vec areaPosition : this.areaPositions) {
            instance.setBlock(areaPosition, Block.AIR);
        }
    }

    @Override
    public @NotNull Instance getInstance() {
        return this.instance;
    }

    private void placeAtPos(@NotNull Vec pos) {
        if (specialBlocks.contains(pos)) {
            var groundBlock = !groundData.hasAdditionalBlocks() ? groundData.groundBlock() : groundData.additionalBlocks().getFirst();
            instance.setBlock(pos, groundBlock);
        } else {
            instance.setBlock(pos, groundData.groundBlock());
        }
        if (tntPositions.contains(pos)) {
            spawnTnt(pos);
        }
    }

    private void spawnTnt(@NotNull Vec pos) {
        var tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);
        Vec spawnPos = pos.add(0.5, TNT_SPAWN_HEIGHT, 0.5);
        tntEntity.setInstance(instance, spawnPos);
        tntEntity.scheduleNextTick(this::checkIfStillFalling);
    }

    private void checkIfStillFalling(@NotNull Entity entity) {
        if (!entity.isOnGround()) {
            entity.scheduleNextTick(this::checkIfStillFalling);
            return;
        }
        entity.remove();
        instance.setBlock(Pos.fromPoint(entity.getPosition()), Block.TNT);
    }

    /**
     * Calculates the positions for the special blocks.
     */
    private void calculateSpecialBlockPositions() {
        var amountOfSpeedBoost = ThreadLocalRandom.current().nextInt(MAX_SPEED_BOOST_AMOUNT - MIN_SPEED_BOOST_AMOUNT) + MIN_SPEED_BOOST_AMOUNT;
        for (int i = 0; i < amountOfSpeedBoost; i++) {
            var randomPos = areaPositions.get(ThreadLocalRandom.current().nextInt(amountOfSpeedBoost, this.areaPositions.size()));
            specialBlocks.add(randomPos);
        }
    }

    /**
     * Calculates the positions for the tnt blocks.
     */
    private void calculateTntPositions() {
        var amountOfTnt = ThreadLocalRandom.current().nextInt(MAX_TNT_AMOUNT - MIN_TNT_AMOUNT) + MIN_TNT_AMOUNT;
        for (int i = 0; i < amountOfTnt; i++) {
            var randomPos = areaPositions.get(ThreadLocalRandom.current().nextInt(0, this.areaPositions.size()));
            tntPositions.add(randomPos);
        }
    }

    /**
     * Returns a random position from the list of tnt positions.
     * @return a random position or null if the list is empty
     */
    public @Nullable Pos getRandomPosition() {
        if (this.tntPositions.isEmpty()) return null;

        int randomId = ThreadLocalRandom.current().nextInt(0, this.tntPositions.size());

        //TODO: Make this access here thread safe
        Vec pos = this.tntPositions.remove(randomId);
        return Pos.fromPoint(pos);
    }
}
