package net.theevilreaper.tamias.game.phase.playing;

import de.icevizion.aves.util.functional.PlayerConsumer;
import de.icevizion.aves.util.functional.VoidConsumer;
import de.icevizion.xerus.api.phase.TickDirection;
import de.icevizion.xerus.api.phase.TimedPhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayingPhase extends TimedPhase {

    private final IntConsumer timeUpdater;
    private final Supplier<VoidConsumer> spawnAreaReset;
    private final PlayerConsumer scoreboardAdd;

    public PlayingPhase(
            @NotNull IntConsumer timeUpdater,
            @NotNull Supplier<VoidConsumer> spawnAreaReset,
            @NotNull PlayerConsumer scoreboardAdd,
            @NotNull Supplier<Map<Class<? extends Event>, Consumer<? extends Event>>> gameListener
    ) {
        super("GamePhase", ChronoUnit.SECONDS, 1);
        this.timeUpdater = timeUpdater;
        this.spawnAreaReset = spawnAreaReset;
        this.scoreboardAdd = scoreboardAdd;
        this.setCurrentTicks(300);
        this.setTickDirection(TickDirection.DOWN);

        for (Map.Entry<Class<? extends Event>, Consumer<? extends Event>> entrySet: gameListener.get().entrySet()) {
            this.addListener((Class<Event>) entrySet.getKey(), (Consumer<Event>) entrySet.getValue());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            AttributeHelper.enableMovement(player);
            this.scoreboardAdd.accept(player);
        }
        this.spawnAreaReset.get().apply();
    }

    @Override
    protected void onFinish() {

    }

    @Override
    public void onUpdate() {
        this.timeUpdater.accept(getCurrentTicks());
    }
}
