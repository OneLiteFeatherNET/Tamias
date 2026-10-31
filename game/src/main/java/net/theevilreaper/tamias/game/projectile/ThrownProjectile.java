package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;

/**
 * A thrown item like an egg or a snowball. Gravity and air drag slow it down, it breaks on the first block or entity it hits.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ThrownProjectile extends VanillaProjectile {

    private static final double GRAVITY = 0.03;
    private static final double AIR_DRAG = 0.99;
    private static final double WATER_DRAG = 0.8;
    private static final byte HIT_STATUS = 3;
    private static final long SYNCHRONIZATION_TICKS = 10;

    /**
     * Creates a thrown projectile which is not spawned until it gets shot.
     *
     * @param type    the entity type, for example the egg
     * @param shooter the entity which throws the projectile
     */
    public ThrownProjectile(EntityType type, Entity shooter) {
        super(type, shooter);
        this.setSynchronizationTicks(SYNCHRONIZATION_TICKS);
    }

    @Override
    protected void movementTick() {
        Vec movement = getVelocity().div(getTicksPerSecond());
        if (!hasNoGravity()) {
            movement = movement.sub(0.0, GRAVITY, 0.0);
        }
        movement = movement.mul(isInWater() ? WATER_DRAG : AIR_DRAG);

        Hit hit = traceFlight(movement);
        flyTo(hit.destination(), movement);
        if (isRemoved()) return;

        this.velocity = movement.mul(getTicksPerSecond());
        checkLeftShooter(movement);
    }

    @Override
    protected void onBlockHit(Hit hit, Block block, Point hitPoint) {
        touchBlock(block, hitPoint);
        triggerStatus(HIT_STATUS);
        remove();
    }

    @Override
    protected void onEntityHit(Entity target) {
        triggerStatus(HIT_STATUS);
        remove();
    }
}
