package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.bomber.BomberEliminatedEvent;
import net.theevilreaper.tamias.game.util.ProjectileHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ProjectileEntityListenerTest {

    @Test
    void testHitOnBomberDispatchesEliminatedEvent(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player shooter = env.createPlayer(instance, Pos.ZERO);
        Player bomber = env.createPlayer(instance, new Pos(1, 0, 0));
        bomber.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        EntityProjectile projectile = ProjectileHelper.createProjectile(shooter);
        projectile.setInstance(instance, Pos.ZERO).join();

        FlexibleListener<BomberEliminatedEvent> eventListener = env.listen(BomberEliminatedEvent.class);
        AtomicBoolean fired = new AtomicBoolean(false);
        eventListener.followup(event -> {
            fired.set(true);
            assertEquals(bomber, event.getPlayer());
        });

        ProjectileEntityListener listener = new ProjectileEntityListener();
        listener.accept(new ProjectileCollideWithEntityEvent(projectile, Pos.ZERO, bomber));

        assertTrue(fired.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testHitOnSurvivorDoesNothing(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player shooter = env.createPlayer(instance, Pos.ZERO);
        Player survivor = env.createPlayer(instance, new Pos(1, 0, 0));
        survivor.setTag(Tags.TEAM_KEY, GameConfig.SURVIVOR_KEY.asString());

        EntityProjectile projectile = ProjectileHelper.createProjectile(shooter);
        projectile.setInstance(instance, Pos.ZERO).join();

        // Cyano's FlexibleListener#followup/failFollowup both require the event to fire by
        // test end (asserted in EnvImpl cleanup), so they can't express "never fires". A raw
        // listener on the global event handler, manually removed below, verifies that instead.
        AtomicBoolean fired = new AtomicBoolean(false);
        EventListener<BomberEliminatedEvent> rawListener = EventListener.of(BomberEliminatedEvent.class, event -> fired.set(true));
        MinecraftServer.getGlobalEventHandler().addListener(rawListener);

        ProjectileEntityListener listener = new ProjectileEntityListener();
        listener.accept(new ProjectileCollideWithEntityEvent(projectile, Pos.ZERO, survivor));

        assertEquals(GameConfig.SURVIVOR_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));
        assertFalse(fired.get());

        MinecraftServer.getGlobalEventHandler().removeListener(rawListener);
        env.destroyInstance(instance, true);
    }
}
