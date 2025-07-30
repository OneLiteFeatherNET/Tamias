package net.theevilreaper.tamias.game.phase;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.MinecraftServer;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import static net.minestom.server.MinecraftServer.getConnectionManager;
import static net.theevilreaper.tamias.game.util.GameMessages.MAP_BUILDING;
import static net.theevilreaper.tamias.game.util.GameMessages.MAP_READY;

/**
 * The phase implementation handles each logic which should be executed during the period where the map builds up.
 * Its use only the {@link GamePhase} abstraction because the build process is not limited to a strict time duration.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class GroundBuildPhase extends GamePhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroundBuildPhase.class);

    private final Supplier<VoidConsumer> mapPlacementTaskTrigger;
    private VoidConsumer taskReset;

    public GroundBuildPhase(
            @NotNull Supplier<VoidConsumer> mapPlacementTaskTrigger
    ) {
        super("MapBuild");
        addListener(AreaFinishBuildEvent.class, areaFinishBuildEvent -> {
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(MAP_READY);
            finish();
        });
        this.mapPlacementTaskTrigger = mapPlacementTaskTrigger;
    }

    @Override
    protected void onStart() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(MAP_BUILDING);
            LOGGER.info("Map is building up...");
            this.taskReset = this.mapPlacementTaskTrigger.get();
            LOGGER.info("Map placement task started");
        }).delay(10, ChronoUnit.SECONDS).schedule();
    }

    @Override
    public void finish() {
        super.finish();
        this.taskReset.apply();
    }
}
