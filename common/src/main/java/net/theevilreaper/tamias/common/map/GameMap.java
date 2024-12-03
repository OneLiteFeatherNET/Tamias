package net.theevilreaper.tamias.common.map;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
@SuppressWarnings("java:S2065")
public final class GameMap extends BaseMap {

    private SpawnLayer spawnLayer;
    private GameAreaData gameAreaData;
    private Pos bomberInitialSpawn;
    private Vec leftAreaPos;
    private Vec rightAreaPos;
    private String direction;

    public GameMap() {
        super("Not set", Pos.ZERO, "");
    }

    public GameMap(
            @NotNull String name,
            @Nullable Pos spawn,
            @NotNull Pos bomberInitialSpawn,
            @NotNull Vec leftAreaPos,
            @NotNull Vec rightAreaPos,
            @NotNull SpawnLayer spawnLayer,
            @NotNull GameAreaData gameAreaData
    ) {
        super(name, spawn, "Team");
        this.bomberInitialSpawn = bomberInitialSpawn;
        this.leftAreaPos = leftAreaPos;
        this.rightAreaPos = rightAreaPos;
        this.spawnLayer = spawnLayer;
        this.gameAreaData = gameAreaData;
    }

    public void setLeftAreaPos(@NotNull Vec vec) {
        this.leftAreaPos = vec;
    }

    public void setRightArePos(@NotNull Vec vec) {
        this.rightAreaPos = vec;
    }

    public void setGameAreaData(@NotNull GameAreaData gameAreaData) {
        this.gameAreaData = gameAreaData;
    }

    public void setDirection(@NotNull String direction) {
        this.direction = direction;
    }

    public void setBomberInitialSpawn(Pos bomberInitialSpawn) {
        this.bomberInitialSpawn = bomberInitialSpawn;
    }

    public void setLeftSurvivorSpawn(Pos leftSurvivorSpawn, @NotNull Direction direction) {
        this.spawnLayer = new SpawnLayer(leftSurvivorSpawn, direction);
    }

    public GameAreaData getGameAreaData() {
        return gameAreaData;
    }

    public @UnknownNullability Pos getBomberInitialSpawn() {
        return bomberInitialSpawn;
    }

    public @NotNull SpawnLayer getSpawnData() {
        return spawnLayer;
    }

    public @NotNull String getDirection() {
        return direction;
    }

    public @NotNull Vec getLeftAreaPos() {
        return leftAreaPos;
    }

    public @NotNull Vec getRightAreaPos() {
        return rightAreaPos;
    }
}
