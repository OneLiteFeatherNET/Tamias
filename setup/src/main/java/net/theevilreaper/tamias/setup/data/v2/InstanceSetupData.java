package net.theevilreaper.tamias.setup.data.v2;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.onelitefeather.guira.data.BaseSetupData;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class InstanceSetupData<T extends BaseMap> extends BaseSetupData<T> {
    protected static final Pos SPAWN_POINT = new Pos(0, 100, 0);

    protected InstanceContainer instance;
    protected BossBar bossBar;
    protected Component title;

    protected InstanceSetupData(@NotNull UUID uuid, @NotNull MapEntry mapEntry) {
        super(uuid, mapEntry);
        this.bossBar = BossBar.bossBar(Component.empty(), 1, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    }


    public void updateTitle() {
        if (this.title == null) {
            this.title = Component.text("Please set a title", NamedTextColor.RED);
        }
        this.bossBar.name(title);
    }

    public void teleport(@NotNull Player player) {
        Pos spawnPoint = map.getSpawnOrDefault(SPAWN_POINT);
        player.setInstance(this.instance, spawnPoint);
        player.showBossBar(this.bossBar);
    }

    @Override
    public void reset() {
        if (instance == null) return;
        this.map = null;
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
    }
}
