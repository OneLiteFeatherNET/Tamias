package net.theevilreaper.tamias.common.map;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
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

    private Pos bomberInitialSpawn;
    private Pos leftSurvivorSpawn;
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
            @NotNull Pos leftSurvivorSpawn,
            @NotNull Vec leftAreaPos,
            @NotNull Vec rightAreaPos,
            @NotNull String direction
    ) {
        super(name, spawn, "Team");
        this.bomberInitialSpawn = bomberInitialSpawn;
        this.leftSurvivorSpawn = leftSurvivorSpawn;
        this.leftAreaPos = leftAreaPos;
        this.rightAreaPos = rightAreaPos;
        this.direction = direction;
    }

    public void setLeftAreaPos(@NotNull Vec vec) {
        this.leftAreaPos = vec;
    }

    public void setRightArePos(@NotNull Vec vec) {
        this.rightAreaPos = vec;
    }

    public void setDirection(@NotNull String direction) {
        this.direction = direction;
    }

    public void setBomberInitialSpawn(Pos bomberInitialSpawn) {
        this.bomberInitialSpawn = bomberInitialSpawn;
    }

    public void setLeftSurvivorSpawn(Pos leftSurvivorSpawn) {
        this.leftSurvivorSpawn = leftSurvivorSpawn;
    }

    public @UnknownNullability Pos getBomberInitialSpawn() {
        return bomberInitialSpawn;
    }

    public @NotNull Pos getInitialSurvivorSpawn() {
        return leftSurvivorSpawn;
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
