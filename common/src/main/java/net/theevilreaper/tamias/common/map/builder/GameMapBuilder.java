package net.theevilreaper.tamias.common.map.builder;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder class for creating instances of {@link GameMap}.
 * This class allows setting the initial spawn position for the bomber and configuring the spawn layer.
 *
 * @author Joltra
 * @version 1.0.0
 * @since 0.1.0
 */
public final class GameMapBuilder extends BaseMapBuilder {

    private Pos bomberInitialSpawn;
    private final SpawnLayer.Builder spawnLayerBuilder;
    private final AreaData.Builder areaDataBuilder;

    /**
     * Constructs a new GameMapBuilder instance with default values.
     */
    public GameMapBuilder() {
        this.spawnLayerBuilder = SpawnLayer.builder();
        this.areaDataBuilder = AreaData.builder();
    }

    /**
     * Constructs a new GameMapBuilder instance with the specified game map.
     *
     * @param gameMap the game map to use for building
     */
    public GameMapBuilder(@NotNull GameMap gameMap) {
        super(gameMap);
        this.bomberInitialSpawn = gameMap.getBomberInitialSpawn();
        this.spawnLayerBuilder = SpawnLayer.builder(gameMap.getSpawnData());
        this.areaDataBuilder = AreaData.builder(gameMap.getGameAreaData());
    }

    /**
     * Sets the initial spawn position for the bomber.
     *
     * @param bomberInitialSpawn the initial spawn position for the bomber can be null
     * @return this builder instance for chaining
     */
    public GameMapBuilder bomberSpawn(@Nullable Pos bomberInitialSpawn) {
        this.bomberInitialSpawn = bomberInitialSpawn;
        return this;
    }

    /**
     * Sets the lower corner of the area.
     *
     * @param lowerCorner the lower corner to set
     * @return this builder instance for chaining
     */
    public GameMapBuilder areaLowerCorner(Vec lowerCorner) {
        this.areaDataBuilder.lowerCorner(lowerCorner);
        return this;
    }

    /**
     * Sets the upper corner of the area.
     *
     * @param upperCorner the upper corner to set
     * @return this builder instance for chaining
     */
    public GameMapBuilder areaUpperCorner(Vec upperCorner) {
        this.areaDataBuilder.upperCorner(upperCorner);
        return this;
    }

    /**
     * Sets the facing direction for the area.
     *
     * @param facing the direction to set
     * @return this builder instance for chaining
     */
    public GameMapBuilder areaFacing(Direction facing) {
        this.areaDataBuilder.facing(facing);
        return this;
    }

    /**
     * Builds the game map with the provided parameters.
     *
     * @return a new instance of GameMap
     */
    public GameMapBuilder spawnLayerPos(Pos pos) {
        this.spawnLayerBuilder.pos(pos);
        return this;
    }

    /**
     * Sets the direction for the spawn layer.
     *
     * @param direction the direction to set
     * @return this builder instance for chaining
     */
    public GameMapBuilder spawnLayerDirection(Direction direction) {
        this.spawnLayerBuilder.direction(direction);
        return this;
    }

    /**
     * Returns the initial spawn position for the bomber.
     *
     * @return the initial spawn
     */
    public @Nullable Pos getBomberInitialSpawn() {
        return bomberInitialSpawn;
    }

    /**
     * Returns the {@link SpawnArea} builder.
     *
     * @return the layer builder
     */
    public @NotNull SpawnLayer.Builder getSpawnLayerBuilder() {
        return this.spawnLayerBuilder;
    }

    /**
     * Returns the {@link AreaData} builder.
     *
     * @return the area builder
     */
    public @NotNull AreaData.Builder getAreaDataBuilder() {
        return this.areaDataBuilder;
    }

    /**
     * Builds the game map with the provided parameters.
     *
     * @return a new instance of GameMap
     */
    @Override
    public @NotNull GameMap build() {
        return new GameMap(
                this.name,
                this.spawn,
                this.bomberInitialSpawn,
                this.spawnLayerBuilder.build(),
                this.areaDataBuilder.build()
        );
    }
}
