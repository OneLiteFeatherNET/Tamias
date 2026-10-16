package net.theevilreaper.tamias.common.map.builder;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder class for creating instances of {@link GameMap}.
 * This class allows setting the initial spawn position for the bomber and configuring the spawn layer.
 *
 * @author Joltra
 * @version 1.0.0
 * @since 0.1.0
 */
public final class GameMapBuilder extends BaseMapBuilder {

    private @Nullable Pos bomberInitialSpawn;
    private final SpawnLayer.Builder spawnLayerBuilder;
    private final AreaData.Builder areaDataBuilder;
    private final List<GroundData> groundPresets;

    /**
     * Constructs a new GameMapBuilder instance with default values.
     */
    public GameMapBuilder() {
        this.spawnLayerBuilder = SpawnLayer.builder();
        this.areaDataBuilder = AreaData.builder();
        this.areaDataBuilder.facing(Direction.NORTH);
        this.groundPresets = new ArrayList<>();
    }

    /**
     * Constructs a new GameMapBuilder instance with the specified game map.
     *
     * @param gameMap the game map to use for building
     */
    public GameMapBuilder(GameMap gameMap) {
        super(gameMap);
        this.bomberInitialSpawn = gameMap.getBomberInitialSpawn();
        this.spawnLayerBuilder = SpawnLayer.builder(gameMap.getSpawnData());
        this.areaDataBuilder = AreaData.builder(gameMap.getGameAreaData());
        this.groundPresets = new ArrayList<>(gameMap.getGroundPresets());
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
     * Adds a ground preset to the map.
     *
     * @param groundData the preset to add
     * @return this builder instance for chaining
     */
    public GameMapBuilder addGroundPreset(GroundData groundData) {
        this.groundPresets.add(groundData);
        return this;
    }

    /**
     * Replaces the ground preset at the given index.
     *
     * @param index      the index of the preset to replace
     * @param groundData the new preset
     * @return this builder instance for chaining
     */
    public GameMapBuilder setGroundPreset(int index, GroundData groundData) {
        this.groundPresets.set(index, groundData);
        return this;
    }

    /**
     * Removes the ground preset at the given index.
     *
     * @param index the index of the preset to remove
     * @return this builder instance for chaining
     */
    public GameMapBuilder removeGroundPreset(int index) {
        this.groundPresets.remove(index);
        return this;
    }

    /**
     * Returns the ground presets of the map.
     *
     * @return the ground presets
     */
    public @UnmodifiableView List<GroundData> getGroundPresets() {
        return Collections.unmodifiableList(this.groundPresets);
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
    public SpawnLayer.Builder getSpawnLayerBuilder() {
        return this.spawnLayerBuilder;
    }

    /**
     * Returns the {@link AreaData} builder.
     *
     * @return the area builder
     */
    public AreaData.Builder getAreaDataBuilder() {
        return this.areaDataBuilder;
    }

    /**
     * Builds the game map with the provided parameters.
     *
     * @return a new instance of GameMap
     */
    @Override
    public GameMap build() {
        return new GameMap(
                this.name,
                this.spawn,
                this.bomberInitialSpawn,
                this.spawnLayerBuilder.build(),
                this.areaDataBuilder.build(),
                this.groundPresets
        );
    }
}
