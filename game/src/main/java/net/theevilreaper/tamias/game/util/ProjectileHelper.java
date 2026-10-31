package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.projectile.VanillaProjectile;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class ProjectileHelper {

    // Same speed as a thrown vanilla egg, a faster egg is hard to follow for the shooter
    private static final double PROJECTILE_POWER = 1.5;
    private static final double PROJECTILE_UNCERTAINTY = 1.0;
    private static final float THROW_VOLUME = 0.5f;

    private ProjectileHelper() { }

    public static void spawnProjectile(Player player, Function<Player, VanillaProjectile> projectTileMapper) {
        Check.argCondition(player.getInstance() == null, "Can't shoot without a instance from a player");
        var projectile = projectTileMapper.apply(player);
        projectile.setTag(Tags.SHOOTER_ID, player.getUuid());

        final Pos origin = player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
        // The projectile spawns itself into the shooter's instance and is removed when a listener cancels the shot
        projectile.shoot(origin, PROJECTILE_POWER, PROJECTILE_UNCERTAINTY);
        if (projectile.isRemoved()) return;

        final float pitch = (float) (0.4 / (ThreadLocalRandom.current().nextDouble() * 0.4 + 0.8));
        final Sound throwSound = Sound.sound(SoundEvent.ENTITY_EGG_THROW, Sound.Source.PLAYER, THROW_VOLUME, pitch);
        player.getInstance().playSoundExcept(null, throwSound, player.getPosition());
    }

    /**
     * Creates a new burning egg projectile for the given player.
     * The projectile is not spawned until it gets shot.
     *
     * @param player the player who should shoot
     * @return a new instance of the {@link VanillaProjectile}
     */
    public static VanillaProjectile createProjectile(Player player) {
        EggProjectile projectile = new EggProjectile(player);
        projectile.getEntityMeta().setOnFire(true);
        return projectile;
    }
}
