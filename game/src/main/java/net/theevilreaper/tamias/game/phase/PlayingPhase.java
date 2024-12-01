package net.theevilreaper.tamias.game.phase;

import de.icevizion.aves.util.functional.VoidConsumer;
import de.icevizion.xerus.api.phase.TimedPhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
public final class PlayingPhase extends TimedPhase {

    private final IntConsumer timeUpdater;
    private final Supplier<VoidConsumer> spawnAreaReset;

    public PlayingPhase(@NotNull IntConsumer timeUpdater, @NotNull Supplier<VoidConsumer> spawnAreaReset) {
        super("GamePhase", ChronoUnit.SECONDS, 1);
        this.timeUpdater = timeUpdater;
        this.spawnAreaReset = spawnAreaReset;
    }

    @Override
    public void onStart() {
        super.onStart();
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            AttributeHelper.enableMovement(player);

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
