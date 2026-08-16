package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.PotionEffect;
import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.game.event.bomber.BomberEliminatedEvent;
import net.theevilreaper.tamias.game.event.bomber.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class BomberEliminatedListenerTest {

    @Test
    void testEliminationTriggersRespawnRequest(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player player = env.createPlayer(instance, Pos.ZERO);

        StaminaBar explodeBar = StaminaFactory.createExplodeBar(player);
        explodeBar.start();

        BomberEliminatedListener listener = new BomberEliminatedListener(p -> explodeBar);

        FlexibleListener<BomberRequireSpawnEvent> eventListener = env.listen(BomberRequireSpawnEvent.class);
        AtomicBoolean fired = new AtomicBoolean(false);
        eventListener.followup(event -> {
            fired.set(true);
            assertEquals(player, event.getPlayer());
            assertEquals(explodeBar, event.getExplodeBar());
        });

        listener.accept(new BomberEliminatedEvent(player, player.getPosition().asVec()));

        // Respawn is delayed (mirrors self-detonation's regen window) so the blast/blindness
        // treatment applied on elimination is actually visible before the respawn fires.
        assertFalse(fired.get());
        assertEquals(0.0, player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue());
        assertTrue(player.getActiveEffects().stream()
                .anyMatch(effect -> effect.potion().effect() == PotionEffect.BLINDNESS));

        for (int i = 0; i < 30 && !fired.get(); i++) {
            env.tick();
        }

        assertTrue(fired.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testEliminationWithoutExplodeBarDoesNotDispatchRespawn(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player player = env.createPlayer(instance, Pos.ZERO);

        BomberEliminatedListener listener = new BomberEliminatedListener(p -> null);

        // FlexibleListener requires the event to fire, so use raw listener instead for "never fires" case
        AtomicBoolean fired = new AtomicBoolean(false);
        EventListener<BomberRequireSpawnEvent> rawListener = EventListener.of(BomberRequireSpawnEvent.class, event -> fired.set(true));
        MinecraftServer.getGlobalEventHandler().addListener(rawListener);

        listener.accept(new BomberEliminatedEvent(player, player.getPosition().asVec()));

        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertFalse(fired.get());
        // The bar guard must run before any blast/blindness/movement-lock is applied,
        // otherwise a player with no ExplodeBar gets soft-locked with no way to respawn.
        assertTrue(player.getActiveEffects().isEmpty());
        assertEquals(0.1, player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue(), 0.0001);

        MinecraftServer.getGlobalEventHandler().removeListener(rawListener);
        env.destroyInstance(instance, true);
    }
}
