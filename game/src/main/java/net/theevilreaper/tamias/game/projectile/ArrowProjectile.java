package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.instance.block.Block;

/**
 * An arrow which flies with the drag and gravity of vanilla arrows. A block hit sticks the arrow in place until it is removed.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArrowProjectile extends VanillaProjectile {

    private static final double BASE_DAMAGE = 2.0;
    private static final double WATER_DRAG = 0.6;
    private static final double AIR_DRAG = 0.99;
    private static final double GRAVITY = 0.05;
    private static final double STUCK_OFFSET = 0.05;
    private static final int STUCK_TICKS_LIMIT = 1200;
    private static final long SYNCHRONIZATION_TICKS = 20;

    private boolean critical;
    private boolean inBlock;
    private int stuckTicks;

    /**
     * Creates an arrow which is not spawned until it gets shot.
     *
     * @param type    the entity type of the arrow
     * @param shooter the entity which shoots the arrow
     */
    public ArrowProjectile(EntityType type, Entity shooter) {
        super(type, shooter);
        this.setSynchronizationTicks(SYNCHRONIZATION_TICKS);
    }

    /**
     * Returns whether the arrow is a critical hit.
     *
     * @return true if the arrow is critical
     */
    public boolean isCritical() {
        return this.critical;
    }

    /**
     * Sets whether the arrow is a critical hit.
     *
     * @param critical true if the arrow is critical
     */
    public void setCritical(boolean critical) {
        this.critical = critical;
        if (this.getEntityMeta() instanceof AbstractArrowMeta arrowMeta) {
            arrowMeta.setCritical(critical);
        }
    }

    /**
     * Returns whether the arrow is stuck in a block.
     *
     * @return true if the arrow is stuck
     */
    public boolean isInBlock() {
        return this.inBlock;
    }

    /**
     * Returns the damage of the arrow for its current speed.
     *
     * @return the damage, rounded up
     */
    public int getDamage() {
        return damageFor(getVelocity().length() / getTicksPerSecond());
    }

    static int damageFor(double speedPerTick) {
        return (int) Math.ceil(Math.clamp(speedPerTick * BASE_DAMAGE, 0.0, Integer.MAX_VALUE));
    }

    @Override
    protected void movementTick() {
        if (this.inBlock) {
            this.velocity = Vec.ZERO;
            this.stuckTicks++;
            if (this.stuckTicks >= STUCK_TICKS_LIMIT) {
                this.remove();
            }
            return;
        }

        final boolean inWater = isInWater();
        Vec movement = getVelocity().div(getTicksPerSecond());
        if (inWater) {
            movement = movement.mul(WATER_DRAG);
        }
        checkLeftShooter(movement);

        Hit hit = traceFlight(movement);
        flyTo(hit.destination(), movement);
        if (isRemoved()) return;

        if (!inWater) {
            movement = movement.mul(AIR_DRAG);
        }
        if (!hasNoGravity()) {
            movement = movement.sub(0.0, GRAVITY, 0.0);
        }
        this.velocity = movement.mul(getTicksPerSecond());
    }

    @Override
    protected void onBlockHit(Hit hit, Block block, Point hitPoint) {
        touchBlock(block, hitPoint);
        if (isRemoved()) return;

        Vec movement = hit.movement();
        // The arrow sinks a little into the block, along each axis it moves on
        Pos stuckPosition = new Pos(
                hitPoint.x() - Math.signum(movement.x()) * STUCK_OFFSET,
                hitPoint.y() - Math.signum(movement.y()) * STUCK_OFFSET,
                hitPoint.z() - Math.signum(movement.z()) * STUCK_OFFSET
        );
        this.velocity = Vec.ZERO;
        this.inBlock = true;
        this.setCritical(false);
        this.refreshPosition(stuckPosition.withView(getPosition()), false, false);
        if (this.getEntityMeta() instanceof AbstractArrowMeta arrowMeta) {
            arrowMeta.setInGround(true);
        }
        this.synchronizeNextTick();
    }

    @Override
    protected void onEntityHit(Entity target) {
        remove();
    }
}
