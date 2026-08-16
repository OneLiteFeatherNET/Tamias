package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.timer.TaskSchedule;
import net.theevilreaper.tamias.game.event.bomber.BomberEliminatedEvent;
import net.theevilreaper.tamias.game.event.bomber.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.util.Effects;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Handles a Bomber eliminated by a Survivor's gun projectile: applies the same visual
 * blast/blindness/movement-lock treatment as self-detonation, then requests a respawn
 * for that player only — no radius scan, no Survivor conversion. The respawn request
 * goes through the existing {@link BomberRequireSpawnEvent}/{@code BomberReviveListener}
 * path, which owns the ticket check for this (and every other) respawn.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class BomberEliminatedListener implements Consumer<BomberEliminatedEvent> {

    // Mirrors self-detonation's ExplodeBar regen-to-respawn window (~1.5s) so the blast/
    // blindness/lock treatment applied below is actually visible before the respawn fires.
    private static final TaskSchedule RESPAWN_DELAY = TaskSchedule.tick(30);

    private final Function<Player, StaminaBar> barGetter;

    public BomberEliminatedListener(Function<Player, StaminaBar> barGetter) {
        this.barGetter = barGetter;
    }

    @Override
    public void accept(BomberEliminatedEvent event) {
        Player player = event.getPlayer();

        StaminaBar staminaBar = this.barGetter.apply(player);
        if (!(staminaBar instanceof ExplodeBar explodeBar)) return;

        Effects.applyBlastEffects(player, player.getPosition());

        MinecraftServer.getSchedulerManager()
                .buildTask(() -> EventDispatcher.call(new BomberRequireSpawnEvent(player, explodeBar)))
                .delay(RESPAWN_DELAY)
                .schedule();
    }
}
