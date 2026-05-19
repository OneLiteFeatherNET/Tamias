package net.theevilreaper.tamias.game.phase.playing;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.event.AreaCleanupEvent;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.BooleanSupplier;

public final class PostPlayingPhase extends TimedPhase {

    private final BooleanSupplier lastRoundCheck;
    private final VoidConsumer roundUpdateTrigger;

    public PostPlayingPhase(
            @NotNull BooleanSupplier lastRoundCheck,
            @NotNull VoidConsumer roundUpdateTrigger
    ) {
        super("RoundEnd", ChronoUnit.SECONDS, 1);
        this.setPaused(false);
        this.setCurrentTicks(10);
        this.setTickDirection(TickDirection.DOWN);
        this.lastRoundCheck = lastRoundCheck;
        this.roundUpdateTrigger = roundUpdateTrigger;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.roundUpdateTrigger.apply();
    }

    @Override
    protected void onFinish() {
        if (this.lastRoundCheck.getAsBoolean()) return;
        //TODO: Yeet the players out of the current round
        System.out.println("Round finished");
        EventDispatcher.call(new AreaCleanupEvent(false));
    }

    @Override
    public void onUpdate() {
        if (this.lastRoundCheck.getAsBoolean()) return;
        if (this.getCurrentTicks() == 2) {
            EventDispatcher.call(AreaSpawnTriggerEvent.empty());
        }
        if (this.getCurrentTicks() == 1) {
            //TODO: Teleport
        }
    }
}
