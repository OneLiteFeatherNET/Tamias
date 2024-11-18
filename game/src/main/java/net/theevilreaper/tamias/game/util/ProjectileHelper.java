package net.theevilreaper.tamias.game.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class ProjectileHelper {

    private static final double PROJECTILE_SPEED = 1.0;

    private ProjectileHelper() { }

    public static void spawnProjectile(@NotNull Player player, @NotNull Function<Player, EntityProjectile> projectTileMapper) {
        Check.argCondition(player.getInstance() == null, "Can't shoot without a instance from a player");
        var projectile = projectTileMapper.apply(player);

        final Pos position = player.getPosition().add(0, player.getEyeHeight(), 0);

        projectile.setInstance(player.getInstance(), position);
        projectile.setTag(Tags.SHOOTER_ID, player.getUuid());
        Vec direction = projectile.getPosition().direction();
        projectile.shoot(position.add(direction).sub(0, 0.2, 0), PROJECTILE_SPEED * 3, 1.0);
    }

    /**
     * Creates a new {@link EntityProjectile} for the given player.
     *
     * @param player the player who should shoot
     * @return a new instance of the {@link EntityProjectile}
     */
    public static @NotNull EntityProjectile createProjectile(@NotNull Player player) {
        EntityProjectile projectile = new EntityProjectile(player, EntityType.EGG);
        ThrownEggMeta eggMeta = (ThrownEggMeta) projectile.getEntityMeta();
        eggMeta.setOnFire(true);

        projectile.scheduleRemove(Duration.of(100, TimeUnit.SERVER_TICK));
        return projectile;
    }
}
