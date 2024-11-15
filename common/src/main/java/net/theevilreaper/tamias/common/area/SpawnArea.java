package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
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
    private static final Vec Y_VEC = new Vec(0, 1, 0);
    private final Instance instance;
    private final Direction direction;
    private final Pos[] positions;
    private final Runnable resetBlocks;

    /**
     * Creates a new object reference from {@link SpawnArea} class with the given values.
     *
     * @param instance      the involved instance to place the block
     * @param startPosition the first spawn which is the start position for the position calculation
     * @param direction     the direction to indicates in which direction the calculation should go
     * @param maxPositions  the maximum amount of possible positions
     */
    public SpawnArea(@NotNull Instance instance, @NotNull Pos startPosition, @NotNull Direction direction, int maxPositions) {
        Check.argCondition(maxPositions < 1, "The maximum position count must be higher than 0");
        Check.argCondition(direction == Direction.DOWN || direction == Direction.UP, "The direction must be horizontal");
        this.instance = instance;
        this.direction = direction;
        this.positions = new Pos[maxPositions];
        this.positions[0] = startPosition;

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
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            LOGGER.info("Teleporting player {} to position {}", players.get(i).getUsername(), positions[i]);
            players.get(i).setInstance(instance, positions[i].add(Y_VEC));
        }
    }


    /**
     * Calculates the spawn position for the area on the given {@link Direction} which is set a creation level.
     */
    @Override
    public void calculatePositions() {
        var vec = switch (this.direction) {
            case NORTH -> new Vec(1, 0, 0);
            case SOUTH -> new Vec(-1, 0, 0);
            case EAST -> new Vec(0, 0, 1);
            case WEST -> new Vec(0, 0, -1);
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
        LOGGER.info("Given start position is {}", positions[0]);
        for (int i = 1; i < positions.length; i++) {
            LOGGER.info("Calculating position {}", positions[i - 1].add(vec));
            positions[i] = positions[i - 1].add(vec);
        }
    }

    /**
     * Places for each spawn position a specific block.
     */
    public void spawnBlocks() {
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
