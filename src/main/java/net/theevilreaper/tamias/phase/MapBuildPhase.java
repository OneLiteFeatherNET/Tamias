package net.theevilreaper.tamias.phase;

import de.icevizion.aves.util.Broadcaster;
import de.icevizion.xerus.api.phase.GamePhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.area.GameArea;
import net.theevilreaper.tamias.event.FinishBuildEvent;
import net.theevilreaper.tamias.util.Messages;
import net.theevilreaper.tamias.util.ValueGetter;
import org.jetbrains.annotations.NotNull;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.listener.game.PlayerStoppedMovement;
import org.jetbrains.annotations.Nullable;

/**
 * Tbe phase implementation handles each logic which should be executed during the period where the map builds up.
 * Its use only the {@link GamePhase} abstraction because the build process is not limited to a strict time duration.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class MapBuildPhase extends GamePhase {

    private final ValueGetter<GameArea> mapGetter;
    private Task task;

    public MapBuildPhase(@Nullable ValueGetter<GameArea> mapGetter) {
        super("MapBuild");
        addListener(PlayerMoveEvent.class, new PlayerStoppedMovement());
        addListener(FinishBuildEvent.class, finishBuildEvent -> {
            Broadcaster.broadcast(Messages.withMini("<green>Map is ready!"));
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
        if (this.mapGetter.getValue() == null) return;
        Broadcaster.broadcast(Messages.withMini("<green>Map is building up..."));
        task = this.mapGetter.getValue().build();
    }

    public void stop() {
        task.cancel();
        finish();
    }
}
