package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.Block.Getter.Condition;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * A projectile which flies like its vanilla counterpart. The shooter's movement is added on launch, every tick moves
 * the projectile along its movement until the first block or entity on the way.
 * <p>
 * While flying, the movement is kept in blocks per tick. {@link Entity#getVelocity()} stays in blocks per second.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class VanillaProjectile extends Entity {

    private static final double UNCERTAINTY_SCALE = 0.0172275;
    private static final double MAX_HIT_MARGIN = 0.3;
    private static final double ENTITY_SEARCH_RADIUS = 3.0;
    private static final double BELOW_WORLD_LIMIT = 64.0;
    private static final double ROTATION_SPEED = 0.2;
    // Minestom's BoundingBox.ZERO is package-private, the ray for block hits is a box of no size
    private static final BoundingBox RAY_BOX = new BoundingBox(Vec.ZERO, Vec.ZERO);
    private static final BoundingBox[] HIT_BOXES = createHitBoxes();

    protected final Entity shooter;
    private boolean leftShooter;
    private @Nullable Hit pendingHit;

    /**
     * Creates a projectile which is not spawned until it gets shot.
     *
     * @param type    the entity type of the projectile
     * @param shooter the entity which shoots the projectile
     */
    protected VanillaProjectile(EntityType type, Entity shooter) {
        super(type);
        this.shooter = shooter;
        this.setHasPhysics(false);
    }

    /**
     * Returns the entity which shot this projectile.
     *
     * @return the shooter
     */
    public final Entity getShooter() {
        return this.shooter;
    }

    /**
     * Shoots the projectile from the given point in the view direction of the shooter.
     * The projectile spawns into the instance of the shooter.
     *
     * @param from        the point where the projectile spawns
     * @param power       the power of the shot
     * @param uncertainty the spread of the shot
     */
    public void shoot(Point from, double power, double uncertainty) {
        shoot(from, power, uncertainty, ThreadLocalRandom.current());
    }

    void shoot(Point from, double power, double uncertainty, RandomGenerator random) {
        if (!Double.isFinite(power) || power < 0.0) {
            throw new IllegalArgumentException("power must be finite and non-negative");
        }
        if (!Double.isFinite(uncertainty) || uncertainty < 0.0) {
            throw new IllegalArgumentException("uncertainty must be finite and non-negative");
        }
        Instance instance = this.shooter.getInstance();
        if (instance == null) {
            throw new IllegalStateException("Cannot shoot a projectile before its shooter is in an instance");
        }

        Vec direction = this.shooter.getPosition().direction();
        EntityShootEvent event = new EntityShootEvent(this.shooter, this, from.add(direction), power, uncertainty);
        EventDispatcher.call(event);
        if (event.isCancelled()) {
            this.remove();
            return;
        }
        if (!Double.isFinite(event.getPower()) || event.getPower() < 0.0
                || !Double.isFinite(event.getSpread()) || event.getSpread() < 0.0) {
            throw new IllegalArgumentException("EntityShootEvent produced invalid power or uncertainty");
        }

        Vec movement = getMovementToShoot(direction, event.getPower(), event.getSpread(), random).add(getShooterMovement());
        Vec velocity = movement.mul(getTicksPerSecond());
        Pos spawnPosition = new Pos(from.x(), from.y(), from.z()).withView(targetYaw(movement), targetPitch(movement));
        this.setInstance(instance, spawnPosition).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                this.remove();
                MinecraftServer.getExceptionManager().handleException(throwable);
            } else {
                this.synchronizePosition();
                this.setVelocity(velocity);
            }
        });
    }

    @Override
    public void tick(long time) {
        if (isRemoved()) return;
        this.pendingHit = null;
        super.tick(time);

        Hit hit = this.pendingHit;
        this.pendingHit = null;
        if (hit != null && !isRemoved()) {
            handleHit(hit);
        }
    }

    /**
     * Hits a block, the projectile is removed or stuck by the subclass.
     *
     * @param hit      the hit of this tick
     * @param block    the block which got hit
     * @param hitPoint the point on the block where the projectile hit it
     */
    protected abstract void onBlockHit(Hit hit, Block block, Point hitPoint);

    /**
     * Hits an entity which accepted the hit. The subclass decides what happens to the projectile.
     *
     * @param target the entity which got hit
     */
    protected abstract void onEntityHit(Entity target);

    /**
     * Moves the projectile along the given movement for this tick and collects what it runs into.
     * The collisions are handled once the tick is over, see {@link #handleHit(Hit)}.
     *
     * @param movement the movement of this tick in blocks
     * @return the hit of this tick
     */
    protected final Hit traceFlight(Vec movement) {
        Instance instance = getInstance();
        Pos start = getPosition();
        // Unloaded chunks count as air, the ray flies on and flyTo removes the projectile on entering them
        Block.Getter blocks = new ChunkCache(instance, currentChunk, Block.AIR);
        PhysicsResult physics = CollisionUtils.handlePhysics(blocks, instance.getWorldBorder(), RAY_BOX, start, movement, null, true);

        Point blockPoint = null;
        Block block = null;
        if (physics.hasCollision() && physics.collisionPoints() != null) {
            for (int axis = 0; axis < 3 && blockPoint == null; axis++) {
                Point point = physics.collisionPoints()[axis];
                if (point == null) continue;
                blockPoint = point;
                // A world border collision has no block position
                Point shapePosition = physics.collisionShapePositions() == null ? null : physics.collisionShapePositions()[axis];
                if (shapePosition != null) {
                    // The cache answers for unloaded chunks, the instance would throw there
                    block = blocks.getBlock(shapePosition, Condition.TYPE);
                }
            }
        }

        Vec sweep;
        if (blockPoint != null) {
            sweep = blockPoint.sub(start).asVec();
        } else {
            sweep = movement;
        }
        // The physics result is not passed: its collision fraction belongs to the full movement, not to the shortened sweep
        List<EntityCollisionResult> entities = new ArrayList<>(CollisionUtils.checkEntityCollisions(instance, hitBox(getAliveTicks()),
                start, sweep, ENTITY_SEARCH_RADIUS, this::canHitEntity, null));
        entities.sort(null);

        this.pendingHit = new Hit(start, movement, blockPoint, block, entities);
        return this.pendingHit;
    }

    /**
     * Moves the projectile to the destination of a hit or to the end of its movement.
     * The view follows the movement. The projectile is removed when it is below the world or enters a chunk which is not loaded.
     *
     * @param destination the position to move to
     * @param movement    the movement of this tick
     */
    protected final void flyTo(Pos destination, Vec movement) {
        Instance instance = getInstance();
        if (destination.y() < instance.getCachedDimensionType().minY() - BELOW_WORLD_LIMIT) {
            this.remove();
            return;
        }
        Chunk chunk = ChunkUtils.retrieve(instance, currentChunk, destination);
        if (!ChunkUtils.isLoaded(chunk)) {
            this.remove();
            return;
        }

        Pos current = getPosition();
        Pos view = current;
        if (!movement.isZero()) {
            view = current.withView(lerpRotation(current.yaw(), targetYaw(movement)), lerpRotation(current.pitch(), targetPitch(movement)));
        }
        // Clients predict the flight between the synchronizations of the entity
        this.refreshPosition(destination.withView(view), false, false);
    }

    /**
     * Returns whether the block at the projectile position is water. Used for the drag of the tick start.
     *
     * @return true if the projectile is in water
     */
    protected final boolean isInWater() {
        Block block = getInstance().getBlock(getPosition(), Condition.TYPE);
        return block.compare(Block.WATER) || block.compare(Block.BUBBLE_COLUMN) || "true".equals(block.getProperty("waterlogged"));
    }

    /**
     * Runs the block handler of the block the projectile touched.
     *
     * @param hitBlock the block which got hit
     * @param hitPos   the point of the hit
     */
    protected final void touchBlock(Block hitBlock, Point hitPos) {
        BlockHandler handler = hitBlock.handler();
        if (handler != null) {
            handler.onTouch(new BlockHandler.Touch(hitBlock, getInstance(), hitPos, this));
        }
    }

    /**
     * Lets the projectile leave the shooter once it is clear of the shooter's reach. Once it left, it stays left.
     *
     * @param movement the movement which is used to sweep the projectile box
     */
    protected final void checkLeftShooter(Vec movement) {
        if (this.leftShooter) return;
        if (this.shooter.isRemoved()) {
            this.leftShooter = true;
            return;
        }
        Pos shooterPosition = this.shooter.getPosition();
        this.leftShooter = isClearOfShooter(getBoundingBox(), movement, this.shooter.getBoundingBox(),
                shooterPosition.sub(getPosition()).asVec());
    }

    private void handleHit(Hit hit) {
        for (EntityCollisionResult collision : hit.entities()) {
            Entity target = collision.entity();
            Point point = collision.collisionPoint();
            ProjectileCollideWithEntityEvent event = new ProjectileCollideWithEntityEvent(this, point.asPos(), target);
            EventDispatcher.call(event);
            if (!event.isCancelled()) {
                flyTo(point.asPos(), hit.movement());
                onEntityHit(target);
                return;
            }
        }

        if (hit.blockPoint() == null) return;
        if (hit.block() == null) {
            // The world border has no block to report
            this.remove();
            return;
        }

        ProjectileCollideWithBlockEvent event = new ProjectileCollideWithBlockEvent(this, hit.blockPoint().asPos(), hit.block());
        EventDispatcher.call(event);
        if (event.isCancelled()) {
            flyTo(hit.start().add(hit.movement()), hit.movement());
            return;
        }
        onBlockHit(hit, hit.block(), hit.blockPoint());
    }

    private boolean canHitEntity(Entity entity) {
        if (entity == this) return false;
        if (!(entity instanceof LivingEntity living) || living.isDead()) return false;
        if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) return false;
        return entity != this.shooter || this.leftShooter;
    }

    private Vec getShooterMovement() {
        final int ticksPerSecond = getTicksPerSecond();
        Vec movement = this.shooter instanceof TamiasPlayer player
                ? player.getKnownMovement()
                : this.shooter.getVelocity().div(ticksPerSecond);
        if (!isFinite(movement)) {
            movement = Vec.ZERO;
        }
        return new Vec(movement.x(), this.shooter.isOnGround() ? 0.0 : movement.y(), movement.z());
    }

    private static boolean isFinite(Vec vec) {
        return Double.isFinite(vec.x()) && Double.isFinite(vec.y()) && Double.isFinite(vec.z());
    }

    static int getTicksPerSecond() {
        return ServerProperties.SERVER_TICKS_PER_SECOND.get();
    }

    /**
     * Returns the movement of a launch, the direction is scaled by the power and deviates by the uncertainty.
     * Every axis takes two random numbers, x first, then y and z.
     *
     * @param direction   the view direction of the shooter
     * @param power       the power of the shot
     * @param uncertainty the spread of the shot
     * @param random      the random source of the deviation
     * @return the movement in blocks per tick without the movement of the shooter
     */
    static Vec getMovementToShoot(Point direction, double power, double uncertainty, RandomGenerator random) {
        Vec normalized = direction.asVec().normalize();
        double deviation = UNCERTAINTY_SCALE * uncertainty;
        return normalized.add(
                (random.nextDouble() - random.nextDouble()) * deviation,
                (random.nextDouble() - random.nextDouble()) * deviation,
                (random.nextDouble() - random.nextDouble()) * deviation
        ).mul(power);
    }

    /**
     * Turns the view from one angle towards another in the short direction, the view moves 20% per tick.
     *
     * @param from the current angle in degrees
     * @param to   the target angle in degrees
     * @return the next angle in degrees
     */
    static float lerpRotation(float from, float to) {
        float rotation = from + Math.round((to - from) / 360f) * 360f;
        return rotation + (float) ROTATION_SPEED * (to - rotation);
    }

    /**
     * Returns the margin the entity hit box grows by, it starts after the second tick and stops at 0.3 blocks.
     *
     * @param tick the number of ticks the projectile has been alive
     * @return the margin in blocks
     */
    static double hitMargin(long tick) {
        return Math.max(0.0, Math.min(MAX_HIT_MARGIN, (tick - 2) / 20.0));
    }

    /**
     * Tells whether the projectile has flown clear of its shooter. The projectile box is swept along the movement
     * and inflated by one block on every side, the projectile is clear once that box no longer touches the shooter.
     *
     * @param projectileBox the box of the projectile, relative to its position
     * @param movement      the movement of the projectile
     * @param shooterBox    the box of the shooter, relative to its position
     * @param shooterOffset the position of the shooter relative to the projectile
     * @return true if the projectile is clear of the shooter
     */
    static boolean isClearOfShooter(BoundingBox projectileBox, Vec movement, BoundingBox shooterBox, Point shooterOffset) {
        Vec min = new Vec(
                Math.min(projectileBox.minX(), projectileBox.minX() + movement.x()) - 1.0,
                Math.min(projectileBox.minY(), projectileBox.minY() + movement.y()) - 1.0,
                Math.min(projectileBox.minZ(), projectileBox.minZ() + movement.z()) - 1.0
        );
        Vec max = new Vec(
                Math.max(projectileBox.maxX(), projectileBox.maxX() + movement.x()) + 1.0,
                Math.max(projectileBox.maxY(), projectileBox.maxY() + movement.y()) + 1.0,
                Math.max(projectileBox.maxZ(), projectileBox.maxZ() + movement.z()) + 1.0
        );
        return !shooterBox.intersectBox(shooterOffset, new BoundingBox(min, max));
    }

    private static BoundingBox hitBox(long tick) {
        return HIT_BOXES[(int) Math.round(hitMargin(tick) * 20)];
    }

    private static BoundingBox[] createHitBoxes() {
        // The margins of hitMargin are multiples of 0.05 from 0 to 0.3
        BoundingBox[] boxes = new BoundingBox[7];
        for (int step = 0; step < boxes.length; step++) {
            double margin = step / 20.0;
            double diameter = Math.max(margin * 2.0, 1.0E-6);
            boxes[step] = new BoundingBox(diameter, diameter, diameter, new Vec(0.0, -margin, 0.0));
        }
        return boxes;
    }

    private static float targetYaw(Vec movement) {
        return (float) Math.toDegrees(Math.atan2(movement.x(), movement.z()));
    }

    private static float targetPitch(Vec movement) {
        return (float) Math.toDegrees(Math.atan2(movement.y(), Math.hypot(movement.x(), movement.z())));
    }

    /**
     * The result of the flight of one tick. A projectile without a block point flew through free space.
     *
     * @param start      the position at the start of the tick
     * @param movement   the movement of the tick
     * @param blockPoint the point where the projectile hit a block or the world border, null without a hit
     * @param block      the block which got hit, null for the world border
     * @param entities   the entities on the way which can be hit, nearest first
     */
    protected record Hit(Pos start, Vec movement, @Nullable Point blockPoint, @Nullable Block block,
                         List<EntityCollisionResult> entities) {

        /**
         * Returns where the projectile ends this tick without any entity hit.
         *
         * @return the block hit point or the end of the movement
         */
        Pos destination() {
            return blockPoint != null ? blockPoint.asPos() : start.add(movement);
        }
    }
}
