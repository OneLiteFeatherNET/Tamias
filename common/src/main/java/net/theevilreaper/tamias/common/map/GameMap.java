package net.theevilreaper.tamias.common.map;

import net.minestom.server.coordinate.Pos;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    private final @Nullable List<GroundData> groundPresets;

    /**
     * Constructs a new GameMap instance with the specified parameters.
     *
     * @param name               the name of the map
     * @param spawn              the spawn position for the map, can be null
     * @param bomberInitialSpawn the initial spawn position for the bomber, can be null
     * @param spawnLayer         the spawn layer for the survivor team
     * @param areaData           the game area data
     * @param groundPresets      the ground presets which belong to the map theme
     */
    public GameMap(
            String name,
            @Nullable Pos spawn,
            Pos bomberInitialSpawn,
            SpawnLayer spawnLayer,
            AreaData areaData,
            List<GroundData> groundPresets
    ) {
        super(name, spawn, List.of("Team"));
        this.bomberInitialSpawn = bomberInitialSpawn;
        this.spawnLayer = spawnLayer;
        this.areaData = areaData;
        this.groundPresets = groundPresets.isEmpty() ? null : List.copyOf(groundPresets);
    }

    /**
     * Get the game area data.
     *
     * @return the game area data
     */
    public AreaData getGameAreaData() {
        return areaData;
    }

    /**
     * Get the bomber initial spawn position.
     *
     * @return the bomber initial spawn position
     */
    public Pos getBomberInitialSpawn() {
        return bomberInitialSpawn;
    }

    /**
     * Get the spawn data for the survivor team.
     *
     * @return the spawn data
     */
    public SpawnLayer getSpawnData() {
        return spawnLayer;
    }

    /**
     * Get the ground presets which belong to the theme of the map.
     * Maps without own presets return an empty list.
     *
     * @return the ground presets
     */
    public List<GroundData> getGroundPresets() {
        return groundPresets == null ? List.of() : groundPresets;
    }

    /**
     * Checks if the map defines its own ground presets.
     *
     * @return {@code true} if the map has own presets
     */
    public boolean hasGroundPresets() {
        return groundPresets != null && !groundPresets.isEmpty();
    }
}
