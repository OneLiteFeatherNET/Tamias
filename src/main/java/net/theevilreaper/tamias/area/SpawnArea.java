package net.theevilreaper.tamias.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.config.GameConfig;
import org.jetbrains.annotations.NotNull;

/**
 * The class holds the data about the spawn area where each player will be spawned when the map is in the build phase.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class SpawnArea {

    private final Instance instance;
    private final Pos[] positions;
    private final Runnable resetBlocks;

    public SpawnArea(@NotNull Instance instance, @NotNull Pos startPosition, @NotNull Direction direction, int maxPositions) {
        Check.argCondition(direction == Direction.DOWN || direction == Direction.UP, "The direction must be horizontal");
        this.instance = instance;
        this.positions = new Pos[maxPositions];
        this.positions[0] = startPosition;

        this.resetBlocks = () -> {
            for (Pos position : positions) {
                instance.setBlock(position, Block.AIR);
            }
        };

        this.calculatePositions(direction);
    }

    private void calculatePositions(@NotNull Direction direction) {
        var vec = switch (direction) {
            case NORTH -> new Vec(0, 0, -1);
            case SOUTH -> new Vec(0, 0, 1);
            case EAST -> new Vec(1, 0, 0);
            case WEST -> new Vec(-1, 0, 0);
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
        for (int i = 1; i < positions.length; i++) {
           positions[i] = positions[i].add(vec);
        }
    }

    public void spawnBlocks() {
        for (Pos position : positions) {
            instance.setBlock(position, GameConfig.SPAWN_BLOCK);
        }
    }

    public void resetBlocks() {
        resetBlocks.run();
    }
}
