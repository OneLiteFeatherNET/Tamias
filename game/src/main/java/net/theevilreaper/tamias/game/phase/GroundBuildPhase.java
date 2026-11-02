package net.theevilreaper.tamias.game.phase;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.event.AreaBuildProgressEvent;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
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

    private static final Duration GROUND_DELAY = Duration.ofSeconds(10);
    private static final Duration TNT_DELAY = Duration.ofSeconds(2);

    private final Supplier<CompletableFuture<Void>> groundTrigger;
    private final Supplier<CompletableFuture<Void>> tntDropTrigger;
    private final VoidConsumer groundReset;
    private final Duration groundDelay;
    private final Duration tntDelay;
    private @Nullable Task groundTask;
    private @Nullable Task tntTask;
    private boolean groundStarted;

    /**
     * Creates a new {@link GroundBuildPhase}.
     *
     * @param groundTrigger  starts the ground placement and returns a future which completes once the ground is built
     * @param tntDropTrigger drops the TNT and returns a future which completes once every TNT block has landed
     * @param groundReset    stops the placements and resets the build progress when the phase finishes
     */
    public GroundBuildPhase(
            Supplier<CompletableFuture<Void>> groundTrigger,
            Supplier<CompletableFuture<Void>> tntDropTrigger,
            VoidConsumer groundReset
    ) {
        this(groundTrigger, tntDropTrigger, groundReset, GROUND_DELAY, TNT_DELAY);
    }

    /**
     * Creates a new {@link GroundBuildPhase} with custom delays.
     *
     * @param groundTrigger  starts the ground placement and returns a future which completes once the ground is built
     * @param tntDropTrigger drops the TNT and returns a future which completes once every TNT block has landed
     * @param groundReset    stops the placements and resets the build progress when the phase finishes
     * @param groundDelay    the delay between the start of the phase and the ground placement
     * @param tntDelay       the delay between the ground placement and the TNT drop
     */
    GroundBuildPhase(
            Supplier<CompletableFuture<Void>> groundTrigger,
            Supplier<CompletableFuture<Void>> tntDropTrigger,
            VoidConsumer groundReset,
            Duration groundDelay,
            Duration tntDelay
    ) {
        super("MapBuild");
        addListener(AreaBuildProgressEvent.class, event -> showBuildProgress(event.progress()));
        this.groundTrigger = groundTrigger;
        this.tntDropTrigger = tntDropTrigger;
        this.groundReset = groundReset;
        this.groundDelay = groundDelay;
        this.tntDelay = tntDelay;
    }

    @Override
    protected void onStart() {
        this.groundStarted = false;
        prepareRound();
        this.groundTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
            players.sendMessage(MAP_BUILDING);
            players.playSound(BUILD_START_SOUND, Sound.Emitter.self());
            LOGGER.info("Map is building up...");
            this.groundStarted = true;
            CompletableFuture<Void> groundBuilt = this.groundTrigger.get();
            LOGGER.info("Map placement task started");

            // The ground starts first, so the TNT doesn't drop into the air
            this.tntTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
                CompletableFuture<Void> tntLanded = this.tntDropTrigger.get();
                CompletableFuture.allOf(groundBuilt, tntLanded).thenRun(this::finishIfBuilt);
            }).delay(this.tntDelay).schedule();
        }).delay(this.groundDelay).schedule();
    }

    /**
     * Finishes the phase once the ground is built and every TNT block has landed.
     */
    private void finishIfBuilt() {
        if (isFinished()) return;
        Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
        players.sendMessage(MAP_READY);
        players.playSound(BUILD_FINISH_SOUND, Sound.Emitter.self());
        finish();
    }

    /**
     * Shows the build progress of the ground in the experience bar of every online player.
     *
     * @param progress the share of placed ground blocks from 0 to 1
     */
    private static void showBuildProgress(double progress) {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.setLevel((int) (progress * 100));
            player.setExp((float) progress);
        }
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
        // The phase can finish early, e.g. when too few players are left, so the delayed placements must not start
        cancelTask(this.groundTask);
        cancelTask(this.tntTask);
        super.finish();
        if (!this.groundStarted) return;
        this.groundReset.apply();
    }

    private static void cancelTask(@Nullable Task task) {
        if (task != null) {
            task.cancel();
        }
    }
}
