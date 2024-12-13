package net.theevilreaper.tamias.game.phase;

import de.icevizion.aves.util.functional.PlayerConsumer;
import de.icevizion.aves.util.functional.VoidConsumer;
import de.icevizion.xerus.api.phase.GamePhase;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.event.FinishBuildEvent;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import org.jetbrains.annotations.NotNull;

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

    private static final Component MAP_READY = Messages.withMini("<green>Map is ready!");
    private static final Component MAP_BUILDING = Messages.withMini("<green>Map is building up...");
    private final Consumer<List<Player>> teleportConsumer;
    private final Supplier<GameArea> mapGetter;
    private final PlayerConsumer scoreboardRemover;
    private VoidConsumer taskReset;

    public MapBuildPhase(
            @NotNull Consumer<List<Player>> teleportConsumer,
            @NotNull Supplier<GameArea> mapGetter,
            @NotNull PlayerConsumer scoreboardRemover
    ) {
        super("MapBuild");
        addListener(FinishBuildEvent.class, finishBuildEvent -> {
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(MAP_READY);
            finish();
        });
        this.teleportConsumer = teleportConsumer;
        this.mapGetter = mapGetter;
        this.scoreboardRemover = scoreboardRemover;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void onStart() {
        List<Player> playerList = new ArrayList<>(getConnectionManager().getOnlinePlayers());
        this.teleportConsumer.accept(playerList);
        for (Player player : playerList) {
            this.scoreboardRemover.accept(player);
            AttributeHelper.disableMovement(player);
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(MAP_BUILDING);
            this.mapGetter.get().triggerPlacement();
          //  this.taskReset = () -> this.mapGetter.get().resetTask();
        }).delay(10, ChronoUnit.SECONDS).schedule();
    }

    @Override
    public void finish() {
        this.taskReset.apply();
        super.finish();
    }
}
