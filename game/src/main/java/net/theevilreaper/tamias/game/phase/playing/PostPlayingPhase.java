package net.theevilreaper.tamias.game.phase.playing;

import de.icevizion.aves.util.functional.VoidConsumer;
import de.icevizion.xerus.api.phase.TimedPhase;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class PostPlayingPhase extends TimedPhase {

    private final BooleanSupplier lastRoundCheck;
    private final VoidConsumer mapResetConsumer;
    private final VoidConsumer spawnAreaPlacement;
    private final Consumer<List<Player>> teleportConsumer;

    public PostPlayingPhase(
            @NotNull BooleanSupplier lastRoundCheck,
            @NotNull VoidConsumer mapResetConsumer,
            @NotNull VoidConsumer spawnAreaPlacement,
            @NotNull Consumer<List<Player>> teleportConsumer
    ) {
        super("RoundEnd", ChronoUnit.SECONDS, 20);
        this.setPaused(false);
        this.setEndTicks(10);
        this.lastRoundCheck = lastRoundCheck;
        this.mapResetConsumer = mapResetConsumer;
        this.spawnAreaPlacement = spawnAreaPlacement;
        this.teleportConsumer = teleportConsumer;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onFinish() {
        if (!this.lastRoundCheck.getAsBoolean()) return;

        //TODO: Yeet the players out of the current round
        this.mapResetConsumer.apply();
    }

    @Override
    public void onUpdate() {
        if (!this.lastRoundCheck.getAsBoolean()) return;
        if (this.getCurrentTicks() == 2) {
            this.spawnAreaPlacement.apply();
        }

        if (this.getCurrentTicks() == 1) {
            List<Player> onlinePlayers = new ArrayList<>(MinecraftServer.getConnectionManager().getOnlinePlayers());
            this.teleportConsumer.accept(onlinePlayers);
        }
    }

}
