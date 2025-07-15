package net.theevilreaper.tamias.game.phase;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.util.Messages;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minestom.server.MinecraftServer.getConnectionManager;

/**
 * Tbe phase implementation handles each logic which should be executed during the period where the map builds up.
 * Its use only the {@link GamePhase} abstraction because the build process is not limited to a strict time duration.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class MapBuildPhase extends GamePhase {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MapBuildPhase.class);
    private static final Component MAP_READY = Messages.withMini("<green>Map is ready!");
    private static final Component MAP_BUILDING = Messages.withMini("<green>Map is building up...");

    private final VoidConsumer mapReferenceChange;
    private final Consumer<List<Player>> teleportConsumer;
    private final Supplier<VoidConsumer> mapPlacementTaskTrigger;
    private VoidConsumer taskReset;

    public MapBuildPhase(
            @NotNull VoidConsumer mapReferenceChange,
            @NotNull Consumer<List<Player>> teleportConsumer,
            @NotNull Supplier<VoidConsumer> mapPlacementTaskTrigger
    ) {
        super("MapBuild");
        addListener(AreaFinishBuildEvent.class, areaFinishBuildEvent -> {
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(MAP_READY);
            finish();
        });
        this.mapReferenceChange = mapReferenceChange;
        this.teleportConsumer = teleportConsumer;
        this.mapPlacementTaskTrigger = mapPlacementTaskTrigger;
    }

    @Override
    protected void onStart() {
        this.mapReferenceChange.apply();
        List<Player> playerList = new ArrayList<>(getConnectionManager().getOnlinePlayers());
        this.teleportConsumer.accept(playerList);
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
        this.taskReset.apply();
        super.finish();
        this.taskReset = null;
    }
}
