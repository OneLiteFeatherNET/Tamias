package net.theevilreaper.tamias.game.util;

import ca.atlasengine.projectiles.entities.ThrownItemProjectile;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

/**
 * A thrown egg which tells the client who shot it.
 * Minestom's egg metadata has no shooter, so the spawn packet would contain no owner. Without the owner the client
 * lets the egg collide with the shooter: a moving shooter is ahead of the server position the egg spawns at, so the
 * egg sticks to their back until the next position synchronization and is not visible.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class EggProjectile extends ThrownItemProjectile {

    /**
     * Creates an unspawned egg.
     *
     * @param shooter the player who shoots the egg
     */
    public EggProjectile(Player shooter) {
        super(EntityType.EGG, shooter);
    }

    @Override
    protected SpawnEntityPacket getSpawnPacket() {
        final Pos position = getPosition();
        return new SpawnEntityPacket(getEntityId(), getUuid(), getEntityType(), position, position.yaw(),
                getShooter().getEntityId(), getVelocityForPacket());
    }
}
