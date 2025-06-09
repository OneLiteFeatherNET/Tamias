package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PlayingArea extends Area {

    @NotNull GameAreaData getGameAreaData();

    @NotNull Set<Vec> getTntPositions();

    @NotNull Set<Vec> getSpecialPositions();
}
