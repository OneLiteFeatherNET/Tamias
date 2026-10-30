package net.theevilreaper.tamias.game.util;

import ca.atlasengine.projectiles.AbstractProjectile;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.util.Tags;

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

    private ProjectileHelper() { }

    public static void spawnProjectile(Player player, Function<Player, AbstractProjectile> projectTileMapper) {
        Check.argCondition(player.getInstance() == null, "Can't shoot without a instance from a player");
        var projectile = projectTileMapper.apply(player);
        projectile.setTag(Tags.SHOOTER_ID, player.getUuid());

        final Pos origin = player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
        // Aim at a target point, the rotation based shot would add the server-simulated fall velocity
        // of a jumping player, which the client never had, and let the egg drop into the ground
        final Pos target = origin.add(player.getPosition().direction());
        // The projectile spawns itself into the shooter's instance
        projectile.shoot(origin, target, PROJECTILE_POWER, PROJECTILE_UNCERTAINTY);
    }

    /**
     * Creates a new burning egg projectile for the given player.
     * The projectile is not spawned until it gets shot.
     *
     * @param player the player who should shoot
     * @return a new instance of the {@link AbstractProjectile}
     */
    public static AbstractProjectile createProjectile(Player player) {
        EggProjectile projectile = new EggProjectile(player);
        projectile.getEntityMeta().setOnFire(true);
        return projectile;
    }
}
