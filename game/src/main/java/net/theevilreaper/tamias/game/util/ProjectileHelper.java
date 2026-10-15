package net.theevilreaper.tamias.game.util;

import ca.atlasengine.projectiles.AbstractProjectile;
import ca.atlasengine.projectiles.entities.ThrownItemProjectile;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
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

    private static final double PROJECTILE_POWER = 3.0;
    private static final double PROJECTILE_UNCERTAINTY = 1.0;

    private ProjectileHelper() { }

    public static void spawnProjectile(Player player, Function<Player, AbstractProjectile> projectTileMapper) {
        Check.argCondition(player.getInstance() == null, "Can't shoot without a instance from a player");
        var projectile = projectTileMapper.apply(player);
        projectile.setTag(Tags.SHOOTER_ID, player.getUuid());

        final Pos origin = player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
        // The projectile spawns itself into the shooter's instance
        projectile.shoot(origin, PROJECTILE_POWER, PROJECTILE_UNCERTAINTY);
    }

    /**
     * Creates a new burning egg projectile for the given player.
     * The projectile is not spawned until it gets shot.
     *
     * @param player the player who should shoot
     * @return a new instance of the {@link AbstractProjectile}
     */
    public static AbstractProjectile createProjectile(Player player) {
        ThrownItemProjectile projectile = new ThrownItemProjectile(EntityType.EGG, player);
        projectile.getEntityMeta().setOnFire(true);
        return projectile;
    }
}
