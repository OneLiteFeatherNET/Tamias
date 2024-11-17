package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * The class holds the data about the spawn area where each player will be spawned when the map is in the build phase.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class SpawnArea implements Area {

    private static final Block SPAWN_BLOCK = Block.TNT;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnArea.class);
    private static final Vec Y_VEC = new Vec(0.5, 1, 0.5);
    private final Instance instance;
    private final Pos[] positions;
    private final Runnable resetBlocks;
    private final SpawnLayer spawnLayer;

    /**
     * Creates a new object reference from {@link SpawnArea} class with the given values.
     *
     * @param instance     the involved instance to place the block
     * @param spawnLayer   the spawn layer which is used to calculate the spawn positions
     * @param maxPositions the maximum amount of possible positions
     */
    public SpawnArea(@NotNull Instance instance, @NotNull SpawnLayer spawnLayer, int maxPositions) {
        Check.argCondition(maxPositions < 1, "The maximum position count must be higher than 0");
        Check.argCondition(spawnLayer.direction() == Direction.DOWN || spawnLayer.direction() == Direction.UP, "The direction must be horizontal");
        this.spawnLayer = spawnLayer;
        this.instance = instance;
        this.positions = new Pos[maxPositions];
        Pos spawnLayerPos = spawnLayer.pos();
        this.positions[0] = new Pos(spawnLayerPos.blockX(), spawnLayerPos.blockY(), spawnLayerPos.blockZ(), spawnLayerPos.yaw(), spawnLayerPos.pitch());

        this.resetBlocks = () -> {
            for (Pos position : positions) {
                instance.setBlock(position, Block.AIR);
            }
        };

        this.calculatePositions();
    }

    /**
     * Teleports a given amount of players to the positions.
     * If the player amount is higher than the maximum of position it will throw a {@link IllegalArgumentException}.
     *
     * @param players the players to teleport
     */
    public void teleport(@NotNull Instance instance, @NotNull List<Player> players) {
        Check.argCondition(players.size() > this.positions.length, "The amount of online players is higher then the maximum position count");
        if (!ChunkUtils.isLoaded(instance.getChunkAt(this.spawnLayer.pos()))) {
            instance.loadChunk(this.spawnLayer.pos()).join();
        }
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            Pos position = this.positions[i].add(Y_VEC);
            LOGGER.info("Teleporting player {} to position {}", players.get(i).getUsername(), positions[i]);
            Player player = players.get(i);
            player.setInstance(instance, position).join();
        }
    }

    /**
     * Calculates the spawn position for the area on the given {@link Direction} which is set a creation level.
     */
    @Override
    public void calculatePositions() {
        Direction direction = this.spawnLayer.direction();
        Vec vec = switch (direction) {
            case NORTH -> new Vec(0, 0, -1); // Negative Z
            case SOUTH -> new Vec(0, 0, 1);  // Positive Z
            case EAST -> new Vec(1, 0, 0);  // Positive X
            case WEST -> new Vec(-1, 0, 0); // Negative X
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
        for (int i = 1; i < positions.length; i++) {
            positions[i] = positions[i - 1].add(vec);
        }
    }

    @Override
    public void triggerPlacement() {
        for (Pos position : positions) {
            instance.setBlock(position, SPAWN_BLOCK);
        }
    }

    /**
     * Resets all blocks in the spawn area.
     */
    @Override
    public void reset() {
        resetBlocks.run();
    }

    @Override
    public @NotNull Instance getInstance() {
        return this.instance;
    }
}
