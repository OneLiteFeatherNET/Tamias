package net.theevilreaper.tamias.common.map;

import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public interface MapProvider {

    void saveMap(@NotNull Path path, @NotNull BaseMap baseMap);

    void teleportToSpawn(@NotNull Player player, boolean instanceSet);

    @UnmodifiableView @NotNull List<MapEntry> getEntries();

    @NotNull Supplier<@Nullable Instance> getActiveInstance();
}
