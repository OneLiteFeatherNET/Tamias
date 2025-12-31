package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    public static final Block SPAWN_BLOCK = Block.TNT;
    private static final Vec Y_VEC = new Vec(0.5, 1, 0.5);
    private final Pos[] positions;
    private final SpawnLayer spawnLayer;

    /**
     * Creates a new object reference from {@link SpawnArea} class with the given values.
     *
     * @param spawnLayer   the spawn layer which is used to calculate the spawn positions
     * @param maxPositions the maximum number of possible positions
     */
    public SpawnArea(SpawnLayer spawnLayer, int maxPositions) {
        Check.argCondition(maxPositions < 1, "The maximum position count must be higher than 0");
        Check.argCondition(spawnLayer.direction() == Direction.DOWN || spawnLayer.direction() == Direction.UP, "The direction must be horizontal");
        this.spawnLayer = spawnLayer;
        this.positions = new Pos[maxPositions];
        Pos spawnLayerPos = spawnLayer.pos();
        this.positions[0] = new Pos(spawnLayerPos.blockX(), spawnLayerPos.blockY(), spawnLayerPos.blockZ(), spawnLayerPos.yaw(), spawnLayerPos.pitch());
        this.calculatePositions();
    }

    /**
     * Teleports a given number of players to the positions.
     * If the player amount is higher than the maximum of position, it will throw a {@link IllegalArgumentException}.
     *
     * @param instance       the instance where the players should be teleported
     * @param players        the players to teleport
     * @param switchInstance if the player should be switched to the instance
     */
    public void teleport(Instance instance, List<Player> players, boolean switchInstance) {
        Check.argCondition(players.size() > this.positions.length, "The amount of online players is higher than the maximum position count");
        if (!ChunkUtils.isLoaded(instance.getChunkAt(this.spawnLayer.pos()))) {
            instance.loadChunk(this.spawnLayer.pos()).join();
        }
        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        for (int i = 0; i < players.size(); i++) {
            Pos position = this.positions[i].add(Y_VEC);
            Player player = players.get(i);
            if (switchInstance) {
                player.setInstance(instance, position).join();
            } else {
                player.teleport(position);
            }
        }
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported in this variant. Please use the holder instead");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Point> getPositions() {
        return Arrays.asList(this.positions);
    }
}
