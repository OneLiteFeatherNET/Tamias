package net.theevilreaper.tamias.game.listener.game;

import ca.atlasengine.projectiles.AbstractProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.util.ProjectileHelper;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileEntityListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testEggHitExplodesBomber(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player shooter = env.createPlayer(instance);
        Player bomber = env.createPlayer(instance);
        bomber.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        StaminaBar explodeBar = StaminaFactory.createExplodeBar(bomber);
        explodeBar.start();
        ProjectileEntityListener listener = new ProjectileEntityListener(_ -> explodeBar);

        FlexibleListener<BomberExplodeEvent> explodeListener = env.listen(BomberExplodeEvent.class);
        explodeListener.followup(event -> assertEquals(bomber, event.getPlayer()));

        listener.accept(createHit(shooter, bomber));

        assertEquals(0, bomber.getLevel());
        assertEquals(0, bomber.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue());

        env.destroyInstance(instance, true);
    }

    @Test
    void testHitOnBomberCountsForShooter(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player shooter = env.createPlayer(instance);
        Player bomber = env.createPlayer(instance);
        bomber.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        StaminaBar explodeBar = StaminaFactory.createExplodeBar(bomber);
        explodeBar.start();
        ProjectileEntityListener listener = new ProjectileEntityListener(_ -> explodeBar);

        listener.accept(createHit(shooter, bomber));

        assertEquals(1, ((TamiasPlayer) shooter).getHits());

        env.destroyInstance(instance, true);
    }

    @Test
    void testEggHitIgnoresSurvivor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player shooter = env.createPlayer(instance);
        Player survivor = env.createPlayer(instance);
        survivor.setTag(Tags.TEAM_KEY, GameConfig.SURVIVOR_KEY.asString());

        StaminaBar shootBar = StaminaFactory.createShootBar(survivor);
        shootBar.start();
        ProjectileEntityListener listener = new ProjectileEntityListener(_ -> shootBar);

        AtomicBoolean exploded = new AtomicBoolean();
        env.process().eventHandler().addListener(BomberExplodeEvent.class, _ -> exploded.set(true));

        listener.accept(createHit(shooter, survivor));

        assertFalse(exploded.get());
        assertEquals(GameConfig.SURVIVOR_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));

        env.destroyInstance(instance, true);
    }

    @Test
    void testEggFliesThroughSpectator(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player shooter = env.createPlayer(instance);
        Player spectator = env.createPlayer(instance);
        spectator.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());
        SpectatorHelper.enable(spectator);

        AtomicBoolean exploded = new AtomicBoolean();
        env.process().eventHandler().addListener(BomberExplodeEvent.class, _ -> exploded.set(true));
        ProjectileEntityListener listener = new ProjectileEntityListener(_ -> null);

        ProjectileCollideWithEntityEvent hit = createHit(shooter, spectator);
        hit.getEntity().setInstance(instance, shooter.getPosition()).join();
        listener.accept(hit);

        assertTrue(hit.isCancelled());
        assertFalse(hit.getEntity().isRemoved());
        assertFalse(exploded.get());
        assertEquals(0, ((TamiasPlayer) shooter).getHits());

        env.destroyInstance(instance, true);
    }

    private ProjectileCollideWithEntityEvent createHit(Player shooter, Player target) {
        AbstractProjectile projectile = ProjectileHelper.createProjectile(shooter);
        return new ProjectileCollideWithEntityEvent(projectile, target.getPosition(), target);
    }
}
