package net.theevilreaper.tamias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class ComponentHelper {

    private ComponentHelper() { }

    public static @NotNull Component convertPointToComponent(@NotNull Point pos) {
        return pos instanceof Vec vec ? convertVec(vec) : convertPos((Pos) pos);
    }

    private static @NotNull Component convertPos(@NotNull Pos pos) {
        Component[] data = new Component[5];
        data[0] = Component.text("x: " + pos.blockX());
        data[1] = Component.text("y: " + pos.blockY());
        data[2] = Component.text("z: " + pos.blockZ());
        data[3] = Component.text("yaw: " + pos.yaw());
        data[4] = Component.text("pitch: " + pos.pitch());
        return Component.join(JoinConfiguration.arrayLike(), data);
    }

    private static @NotNull Component convertVec(@NotNull Vec vec) {
        Component[] data = new Component[3];
        data[0] = Component.text("x: " + vec.blockX());
        data[1] = Component.text("y: " + vec.blockY());
        data[2] = Component.text("z: " + vec.blockZ());
        return Component.join(JoinConfiguration.arrayLike(), data);
    }
}
