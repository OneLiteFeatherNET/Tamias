package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.projectile.ThrownProjectile;
import net.theevilreaper.tamias.game.projectile.VanillaProjectile;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The shooter is a {@link net.theevilreaper.tamias.game.player.TamiasPlayer}, its known movement feeds the launch.
 */
class ProjectileHelperIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testCreationWithoutAPlayerInstance(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        // We need a null reference on the instance to check if the method throws an exception
        player.remove(true);

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> ProjectileHelper.spawnProjectile(player, player1 -> null),
                "Can't shoot without a instance from a player"
        );
    }

    @Test
    void testProjectileCreation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        VanillaProjectile projectile = ProjectileHelper.createProjectile(player);
        assertNotNull(projectile);
        assertInstanceOf(ThrownProjectile.class, projectile);
        assertEquals(EntityType.EGG, projectile.getEntityType());
        assertSame(player, projectile.getShooter());

        assertInstanceOf(ThrownEggMeta.class, projectile.getEntityMeta());
        assertTrue(projectile.getEntityMeta().isOnFire());

        // The projectile is only spawned when it gets shot
        assertEquals(1, instance.getEntities().size());
    }

    @Test
    void testProjectileSpawn(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        VanillaProjectile projectile = ProjectileHelper.createProjectile(player);
        ProjectileHelper.spawnProjectile(player, ignored -> projectile);

        assertEquals(player.getUuid(), projectile.getTag(Tags.SHOOTER_ID));
        assertSame(instance, projectile.getInstance());
        assertEquals(2, instance.getEntities().size());
    }

    @Test
    void testProjectileIgnoresServerFallVelocityOfAJumpingShooter(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        // In the air like a sprint jump, the server simulates a fall velocity the client never had
        Player player = env.createPlayer(instance, new Pos(0, 50, 0));
        for (int i = 0; i < 10; i++) {
            env.tick();
        }
        assertTrue(player.getVelocity().y() < -5, "The server should simulate a fall velocity for the player");

        VanillaProjectile projectile = ProjectileHelper.createProjectile(player);
        ProjectileHelper.spawnProjectile(player, ignored -> projectile);

        // A horizontal shot keeps flying horizontally, only the uncertainty may tilt it a bit
        assertTrue(Math.abs(projectile.getVelocity().y()) < 1, "Vertical velocity was " + projectile.getVelocity().y());
    }

    @Test
    void testSpawnPacketContainsTheShooter(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        Collector<SpawnEntityPacket> spawnPackets = connection.trackIncoming(SpawnEntityPacket.class);

        VanillaProjectile projectile = ProjectileHelper.createProjectile(player);
        ProjectileHelper.spawnProjectile(player, ignored -> projectile);

        // Without the owner the client lets the egg collide with the moving shooter right after the spawn
        spawnPackets.assertSingle(packet -> assertEquals(player.getEntityId(), packet.data()));
    }

    @Test
    void testThrowSoundIsPlayedAtTheShooter(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, new Pos(0, 50, 0));
        Collector<SoundEffectPacket> soundTracker = connection.trackIncoming(SoundEffectPacket.class);

        ProjectileHelper.spawnProjectile(player, ProjectileHelper::createProjectile);

        // The pitch is 0.4 / (random * 0.4 + 0.8) for a random value in [0, 1)
        soundTracker.assertSingle(packet -> {
            assertEquals(SoundEvent.ENTITY_EGG_THROW, packet.soundEvent(), "The egg throw sound should be played");
            assertEquals(Sound.Source.PLAYER, packet.source(), "The egg throw sound should come from players");
            assertEquals(0.5f, packet.volume(), "The egg throw sound should use volume 0.5");
            assertTrue(packet.pitch() > 0.33f && packet.pitch() <= 0.5f, "The pitch was " + packet.pitch());
        });
    }
}
