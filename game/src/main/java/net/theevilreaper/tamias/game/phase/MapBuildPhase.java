package net.theevilreaper.tamias.game.phase;

import de.icevizion.xerus.api.phase.GamePhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.event.FinishBuildEvent;
import net.theevilreaper.tamias.game.listener.game.PlayerStoppedMovement;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Tbe phase implementation handles each logic which should be executed during the period where the map builds up.
 * Its use only the {@link GamePhase} abstraction because the build process is not limited to a strict time duration.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class MapBuildPhase extends GamePhase {

    private static final Component MAP_READY = GameMessages.withMini("<green>Map is ready!");
    private static final Component MAP_BUILDING = GameMessages.withMini("<green>Map is building up...");
    private final Supplier<GameArea> mapGetter;
    private Task task;

    public MapBuildPhase(@Nullable Supplier<GameArea> mapGetter) {
        super("MapBuild");
        addListener(PlayerMoveEvent.class, new PlayerStoppedMovement());
        addListener(FinishBuildEvent.class, finishBuildEvent -> {
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(MAP_READY);
            stop();
        });
        this.mapGetter = mapGetter;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void onStart() {
        if (this.mapGetter.get() == null) return;
        Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                .sendMessage(MAP_BUILDING);
        task = this.mapGetter.get().build();
    }

    public void stop() {
        task.cancel();
        finish();
    }
}
