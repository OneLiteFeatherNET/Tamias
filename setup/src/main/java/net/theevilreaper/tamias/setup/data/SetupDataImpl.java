package net.theevilreaper.tamias.setup.data;

import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract sealed class SetupDataImpl implements SetupData permits LobbyData, GameData {

    private static final Pos SPAWN_POINT = new Pos(0, 100, 0);

    protected final Player player;

    protected MapEntry mapEntry;
    protected BaseMap baseMap;
    protected InstanceContainer instance;
    protected BossBar bossBar;
    protected Component title;

    SetupDataImpl(
            @NotNull Player player,
            @NotNull MapEntry mapEntry,
            @NotNull BaseMap baseMap
    ) {
        this.mapEntry = mapEntry;
        this.player = player;
        this.bossBar = BossBar.bossBar(Component.empty(), 1, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        this.baseMap = baseMap;
    }

    @Override
    public void updateTitle() {
        if (this.title == null) {
            this.title = Component.text("Please set a title", NamedTextColor.RED);
        }
        this.bossBar.name(title);
    }

    @Override
    public boolean loadMap() {
        if (this.instance != null && this.instance.isRegistered()) {
            return false;
        }
        if (this.instance == null) {
            this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        }
        AnvilLoader anvilLoader = new AnvilLoader(this.mapEntry.getDirectoryRoot());
        this.instance.setChunkLoader(anvilLoader);
        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
        return true;
    }

    @Override
    public void reset() {
        if (instance == null) return;
        this.mapEntry = null;
        this.baseMap = null;
        player.hideBossBar(this.bossBar);
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
    }

    @Override
    public void teleport() {
        Pos spawnPoint = baseMap.getSpawnOrDefault(SPAWN_POINT);
        player.setInstance(this.instance, spawnPoint);
        player.showBossBar(this.bossBar);
    }

    @Override
    public boolean hasMap() {
        return this.mapEntry != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetupDataImpl setupData = (SetupDataImpl) o;
        return Objects.equals(player.getUuid(), setupData.player.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(player.getUuid());
    }

    @Override
    public @NotNull MapEntry getMapEntry() {
        return this.mapEntry;
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull BaseMap getBaseMap() {
        return this.baseMap;
    }
}
