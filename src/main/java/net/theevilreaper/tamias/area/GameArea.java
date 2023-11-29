package net.theevilreaper.tamias.area;

import de.icevizion.xerus.Game;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public final class GameArea {

    private final Vec start;
    private final Vec end;

    public GameArea(@NotNull Vec start, @NotNull Vec end) {
        var distance = start.sub(end);
        Check.argCondition(distance.equals(Vec.ZERO), "NPE");
        this.start = start;
        this.end = end;

    }
}
