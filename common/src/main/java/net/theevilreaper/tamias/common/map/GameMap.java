package net.theevilreaper.tamias.common.map;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S2065")
public final class GameMap extends BaseMap {

    private SpawnLayer spawnLayer;
    private AreaData areaData;
    private Pos bomberInitialSpawn;
    private MapEntry mapEntry;

    public GameMap() {
        super("Not set", Pos.ZERO);
    }

    public GameMap(
            @NotNull String name,
            @Nullable Pos spawn,
            @NotNull Pos bomberInitialSpawn,
            @NotNull SpawnLayer spawnLayer,
            @NotNull AreaData areaData
    ) {
        super(name, spawn, "Team");
        this.bomberInitialSpawn = bomberInitialSpawn;
        this.spawnLayer = spawnLayer;
        this.areaData = areaData;
    }

    public void setMapEntry(MapEntry mapEntry) {
        this.mapEntry = mapEntry;
    }

    /**
     * Set's the game area data for the map.
     *
     * @param areaData the data to set
     */
    public void setGameAreaData(@NotNull AreaData areaData) {
        this.areaData = areaData;
    }

    /**
     * Set's the initial spawn position for the bomber.
     *
     * @param bomberInitialSpawn the position to set
     */
    public void setBomberInitialSpawn(Pos bomberInitialSpawn) {
        this.bomberInitialSpawn = bomberInitialSpawn;
    }

    /**
     * Set's the left survivor spawn position.
     *
     * @param leftSurvivorSpawn the position to set
     * @param direction         the direction to set
     */
    public void setLeftSurvivorSpawn(Pos leftSurvivorSpawn, @NotNull Direction direction) {
        this.spawnLayer = new SpawnLayer(leftSurvivorSpawn, direction);
    }

    /**
     * Get the game area data.
     *
     * @return the game area data
     */
    public @Nullable AreaData getGameAreaData() {
        return areaData;
    }

    /**
     * Get the bomber initial spawn position.
     *
     * @return the bomber initial spawn position
     */
    public @UnknownNullability Pos getBomberInitialSpawn() {
        return bomberInitialSpawn;
    }

    /**
     * Get the spawn data for the survivor team.
     *
     * @return the spawn data
     */
    public @NotNull SpawnLayer getSpawnData() {
        return spawnLayer;
    }

    public MapEntry getMapEntry() {
        return mapEntry;
    }
}
