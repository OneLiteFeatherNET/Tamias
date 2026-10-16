package net.theevilreaper.tamias.game.phase;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

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
    private static final Sound BUILD_START_SOUND = Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 1f, 1f);
    private static final Sound BUILD_FINISH_SOUND = Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1f, 1f);

    private final Supplier<VoidConsumer> mapPlacementTaskTrigger;
    private final GamePlacement gamePlacement;
    private @Nullable VoidConsumer taskReset;

    public GroundBuildPhase(Supplier<VoidConsumer> mapPlacementTaskTrigger, GamePlacement gamePlacement) {
        super("MapBuild");
        addListener(AreaFinishBuildEvent.class, areaFinishBuildEvent -> {
            Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
            players.sendMessage(MAP_READY);
            players.playSound(BUILD_FINISH_SOUND, Sound.Emitter.self());
            finish();
        });
        this.mapPlacementTaskTrigger = mapPlacementTaskTrigger;
        this.gamePlacement = gamePlacement;
    }

    @Override
    protected void onStart() {
        prepareRound();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
            players.sendMessage(MAP_BUILDING);
            players.playSound(BUILD_START_SOUND, Sound.Emitter.self());
            LOGGER.info("Map is building up...");
            this.taskReset = this.mapPlacementTaskTrigger.get();
            LOGGER.info("Map placement task started");

            MinecraftServer.getSchedulerManager().buildTask(() ->
                    this.gamePlacement.dropTnt(() -> ThreadLocalRandom.current().nextInt(GameAreaHelper.MIN_TNT_AMOUNT, GameAreaHelper.MAX_TNT_AMOUNT + 1))
            ).delay(2, ChronoUnit.SECONDS).schedule();
        }).delay(10, ChronoUnit.SECONDS).schedule();
    }

    /**
     * Prepares a new round. As this phase is the first one of the round cycle, it runs for every round,
     * so no check for the first or last round is required.
     */
    private void prepareRound() {
        EventDispatcher.call(AreaSpawnTriggerEvent.empty());
        EventDispatcher.call(new RoundPrepareEvent());
        EventDispatcher.call(new RoundStartEvent());
    }

    @Override
    public void finish() {
        super.finish();
        if (this.taskReset == null) return;
        this.taskReset.apply();
    }
}
