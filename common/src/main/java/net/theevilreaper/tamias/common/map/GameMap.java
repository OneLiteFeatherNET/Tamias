package net.theevilreaper.tamias.common.map;

import net.minestom.server.coordinate.Pos;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * The {@link GameMap} class contains all relevant information about the map which is used during the game.
 *
 * @author theEvilReaper
 * @version 1.1.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S2065")
public final class GameMap extends BaseMap {

    private final SpawnLayer spawnLayer;
    private final AreaData areaData;
    private final Pos bomberInitialSpawn;
    private MapEntry mapEntry;

    /**
     * Constructs a new GameMap instance with the specified parameters.
     *
     * @param name               the name of the map
     * @param spawn              the spawn position for the map, can be null
     * @param bomberInitialSpawn the initial spawn position for the bomber, can be null
     * @param spawnLayer         the spawn layer for the survivor team
     * @param areaData           the game area data
     */
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
}