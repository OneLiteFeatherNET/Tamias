package net.theevilreaper.tamias.map;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.config.GameConfig;
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

    private Direction direction;

    private final transient Pos[] gameSpawns = new Pos[GameConfig.MAX_PLAYERS];

    public GameMap() {
        super("", Pos.ZERO, "");
    }

    public GameMap(@NotNull String name, Pos spawn, @NotNull Pos bomberInitialSpawn, @NotNull Pos leftSurvivorSpawn) {
        super(name, spawn, "Team");
        this.bomberInitialSpawn = bomberInitialSpawn;
        this.leftSurvivorSpawn = leftSurvivorSpawn;
    }

    public void calculateSpawns() {
        Check.argCondition(this.leftSurvivorSpawn == null, "The calculation needs the initial spawn position!");
        this.gameSpawns[0] = this.leftSurvivorSpawn;
        for (int i = 1; i < GameConfig.MAX_PLAYERS; i++) {
            this.gameSpawns[i] = leftSurvivorSpawn.add(0, 0, i);
        }
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

    public @NotNull Pos[] getGameSpawns() {
        return gameSpawns;
    }
}
