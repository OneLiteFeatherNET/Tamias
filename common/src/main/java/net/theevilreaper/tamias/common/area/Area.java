package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

/**
 * With an {@link Area} it is possible to get specific positions in an area between two points.
 * What the area does with the positions is up to the implementation and can be different.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Area {

    /**
     * Calculates the positions between two points.
     */
    void calculatePositions();

    /**
     * Resets the area to the initial state.
     */
    void reset();

    @NotNull @UnmodifiableView
    Set<Point> getPositions();

}
