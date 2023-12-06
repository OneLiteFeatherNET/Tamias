package net.theevilreaper.tamias.map;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
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
    private Direction direction;


    public GameMap() {
        super("", Pos.ZERO, "");
    }

    public GameMap(@NotNull String name, Pos spawn, @NotNull Pos bomberInitialSpawn, @NotNull Pos leftSurvivorSpawn, @NotNull Vec leftAreaPos, @NotNull Vec rightAreaPos, @NotNull Direction direction) {
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

    public void setDirection(@NotNull Direction direction) {
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

    public @NotNull Direction getDirection() {
        return direction;
    }

    public @NotNull Vec getLeftAreaPos() {
        return leftAreaPos;
    }

    public @NotNull Vec getRightAreaPos() {
        return rightAreaPos;
    }
}
