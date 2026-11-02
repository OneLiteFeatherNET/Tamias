package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A {@link TntAnimation} in which TNT minecarts drive over short-lived rails to every position and leave the TNT
 * behind them. The routes are searched over passable cells, so the carts drive around walls and blocks. A run with
 * many positions uses several carts, which share the open positions. The rails are displays which disappear behind
 * the carts.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MinecartTntAnimation implements TntAnimation {

    /**
     * The distance in blocks a cart drives per tick.
     */
    static final double CART_SPEED = 1.0;
    /**
     * The number of cells ahead of a cart which get a rail.
     */
    static final int RAILS_AHEAD = 4;
    /**
     * The lifetime of a rail in ticks, the track disappears behind the cart.
     */
    static final int RAIL_LIFETIME = 30;
    /**
     * The delay in ticks between the arrival of a cart and the placement of the TNT, so the TNT appears behind it.
     */
    static final int LAND_DELAY = 3;
    /**
     * The number of positions one cart takes care of.
     */
    static final int TNT_PER_CART = 10;
    /**
     * The largest number of carts of one run.
     */
    static final int MAX_CARTS = 4;

    private final List<Vec> openPositions = new ArrayList<>();
    private final List<Cart> carts = new ArrayList<>();
    private int totalPositions;
    private int landedCount;
    private boolean finished;

    /**
     * Returns the number of carts for the given amount of positions: one cart per {@link #TNT_PER_CART} positions,
     * at least one and at most {@link #MAX_CARTS}.
     *
     * @param positions the number of TNT positions of the run
     * @return the number of carts
     */
    static int cartCount(int positions) {
        long carts = (long) Math.ceil(positions / (double) TNT_PER_CART);
        return (int) Math.clamp(carts, 1, MAX_CARTS);
    }

    /**
     * Spawns the carts around the cell of the average position of all groups, at the height of the first group.
     * Every cart gets its own cell, the nearest passable cells next to the average are used.
     *
     * @param groups  all groups of the run
     * @param context the context of the run
     */
    @Override
    public void begin(List<TntGroup> groups, TntAnimationContext context) {
        List<Vec> allPositions = groups.stream().flatMap(group -> group.positions().stream()).toList();
        this.totalPositions = allPositions.size();
        if (allPositions.isEmpty()) return;

        double sumX = 0;
        double sumZ = 0;
        for (Vec position : allPositions) {
            sumX += position.x();
            sumZ += position.z();
        }
        double y = groups.getFirst().center().y();
        Predicate<Vec> passable = cell -> GridRoute.isPassable(context.instance(), cell);
        Vec average = GridRoute.cell(sumX / allPositions.size(), y, sumZ / allPositions.size());
        Vec base = GridRoute.nearestPassable(average, passable, GridRoute.MAX_SPAWN_RADIUS);

        Set<Vec> used = new HashSet<>();
        for (int i = 0; i < cartCount(allPositions.size()); i++) {
            Vec startCell = GridRoute.nearestPassable(base, cell -> passable.test(cell) && !used.contains(cell),
                    GridRoute.MAX_SPAWN_RADIUS);
            used.add(startCell);
            Cart cart = spawnCart(centre(startCell), context);
            this.carts.add(cart);
            context.schedule(1, () -> step(cart, context));
        }
    }

    /**
     * Adds the positions of the group to the open work, a cart only drives to a position once it is added here.
     *
     * @param group   the group whose ground is ready
     * @param context the context of the run
     */
    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        this.openPositions.addAll(group.positions());
    }

    private Cart spawnCart(Vec position, TntAnimationContext context) {
        Entity entity = new Entity(EntityType.TNT_MINECART);
        entity.setNoGravity(true);
        entity.setInstance(context.instance(), position);
        context.track(entity);
        return new Cart(entity, position);
    }

    /**
     * Drives one cart a tick: an idle cart takes the nearest open position and plans its route, a driving cart moves
     * along its route. Without open work the cart stands still.
     *
     * @param cart    the cart to drive
     * @param context the context of the run
     */
    private void step(Cart cart, TntAnimationContext context) {
        if (this.finished) return;
        if (cart.route == null) {
            Vec target = takeNearestOpen(cart);
            if (target != null) {
                startRoute(cart, target, context);
            }
        }
        if (cart.route != null) {
            drive(cart, context);
        }
        if (!this.finished) {
            context.schedule(1, () -> step(cart, context));
        }
    }

    /**
     * Plans the route of the cart to the target. The TNT is placed right away if there is no route, the cart stays.
     *
     * @param cart    the cart which takes the target
     * @param target  the position the TNT is placed on
     * @param context the context of the run
     */
    private void startRoute(Cart cart, Vec target, TntAnimationContext context) {
        Predicate<Vec> passable = cell -> GridRoute.isPassable(context.instance(), cell);
        List<Vec> cells = plan(cart, target, passable);
        if (cells == null) {
            landTnt(target, context);
            return;
        }
        if (cells.isEmpty()) {
            arrive(target, context);
            return;
        }
        cart.route = new Route(target, cells, GridRoute.cellOf(cart.position));
        Instance instance = context.instance();
        instance.playSound(Sound.sound(SoundEvent.BLOCK_METAL_PLACE, Sound.Source.BLOCK, 0.5f, 1f),
                cart.position.x(), cart.position.y(), cart.position.z());
        instance.playSound(Sound.sound(SoundEvent.ENTITY_MINECART_RIDING, Sound.Source.NEUTRAL, 0.3f, 1f),
                cart.position.x(), cart.position.y(), cart.position.z());
        spawnRails(cart, context);
    }

    /**
     * Plans the cells from the cart to the target. Null if there is no route or the target itself can't be walked on,
     * so the cart never drives into a block.
     */
    private static List<Vec> plan(Cart cart, Vec target, Predicate<Vec> passable) {
        List<Vec> cells = GridRoute.find(GridRoute.cellOf(cart.position), target, passable, GridRoute.MAX_ROUTE_NODES);
        if (cells == null || (!cells.isEmpty() && !passable.test(cells.getLast()))) return null;
        return cells;
    }

    /**
     * Moves the cart by one tick along its route. A next cell which became blocked, e.g. by fresh TNT, is replanned
     * once. The cart arrives on its target and places the TNT after {@link #LAND_DELAY} ticks.
     *
     * @param cart    the cart to move
     * @param context the context of the run
     */
    private void drive(Cart cart, TntAnimationContext context) {
        Predicate<Vec> passable = cell -> GridRoute.isPassable(context.instance(), cell);
        if (!passable.test(cart.route.cells.get(cart.route.index))) {
            List<Vec> replanned = plan(cart, cart.route.target, passable);
            if (replanned == null) {
                Vec target = cart.route.target;
                cart.route = null;
                landTnt(target, context);
                return;
            }
            if (replanned.isEmpty()) {
                Vec target = cart.route.target;
                cart.route = null;
                arrive(target, context);
                return;
            }
            cart.route = new Route(cart.route.target, replanned, GridRoute.cellOf(cart.position));
            spawnRails(cart, context);
        }

        Vec next = centre(cart.route.cells.get(cart.route.index));
        double dx = next.x() - cart.position.x();
        double dz = next.z() - cart.position.z();
        double distance = Math.hypot(dx, dz);
        if (distance <= CART_SPEED) {
            cart.position = next;
            cart.route.index++;
        } else {
            cart.position = new Vec(
                    cart.position.x() + dx / distance * CART_SPEED,
                    cart.position.y(),
                    cart.position.z() + dz / distance * CART_SPEED
            );
        }
        if (distance > 1e-6) {
            cart.yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        }
        cart.entity.teleport(new Pos(cart.position.x(), cart.position.y(), cart.position.z(), cart.yaw, 0f));
        spawnRails(cart, context);

        if (cart.route.index == cart.route.cells.size()) {
            Vec target = cart.route.target;
            cart.route = null;
            arrive(target, context);
        }
    }

    /**
     * Spawns the rails of the next cells of the cart's route which don't have a rail yet.
     *
     * @param cart    the cart whose route gets the rails
     * @param context the context of the run
     */
    private static void spawnRails(Cart cart, TntAnimationContext context) {
        List<Vec> cells = cart.route.cells;
        int limit = Math.min(cells.size(), cart.route.index + RAILS_AHEAD);
        while (cart.route.railsSpawned < limit) {
            int index = cart.route.railsSpawned;
            Vec previous = index == 0 ? cart.route.start : cells.get(index - 1);
            Vec next = index + 1 < cells.size() ? cells.get(index + 1) : null;
            spawnRail(cells.get(index), railShape(previous, cells.get(index), next), context);
            cart.route.railsSpawned++;
        }
    }

    private static void spawnRail(Vec cell, String shape, TntAnimationContext context) {
        Entity rail = new Entity(EntityType.BLOCK_DISPLAY);
        rail.setNoGravity(true);
        rail.editEntityMeta(BlockDisplayMeta.class, meta -> meta.setBlockState(Block.RAIL.withProperty("shape", shape)));
        rail.setInstance(context.instance(), cell);
        context.track(rail);
        context.schedule(RAIL_LIFETIME, () -> context.release(rail));
    }

    /**
     * Places the TNT of the target after the delay, so it appears behind the cart.
     *
     * @param target  the position of the TNT
     * @param context the context of the run
     */
    private void arrive(Vec target, TntAnimationContext context) {
        context.schedule(LAND_DELAY, () -> landTnt(target, context));
    }

    private void landTnt(Vec target, TntAnimationContext context) {
        context.land(target);
        this.landedCount++;
        Point center = centre(target).add(0, 0.5, 0);
        context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        context.instance().playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
        if (this.landedCount == this.totalPositions) {
            finish(context);
        }
    }

    /**
     * Ends the run: every cart makes a last poof and is removed, which stops all driving. The rails stay until their
     * lifetime ends.
     *
     * @param context the context of the run
     */
    private void finish(TntAnimationContext context) {
        this.finished = true;
        for (Cart cart : this.carts) {
            context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, cart.position.add(0, 1, 0), new Vec(0.2), 0.02f, 8));
            context.release(cart.entity);
        }
    }

    /**
     * Takes the open position nearest to the cart, the position is removed from the open work.
     *
     * @param cart the cart which looks for work
     * @return the nearest open position, null if no position is open
     */
    private Vec takeNearestOpen(Cart cart) {
        if (this.openPositions.isEmpty()) return null;
        int nearest = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < this.openPositions.size(); i++) {
            Vec position = this.openPositions.get(i);
            double distance = Math.hypot(position.x() - cart.position.x(), position.z() - cart.position.z());
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return this.openPositions.remove(nearest);
    }

    /**
     * Returns the rail shape of a cell, derived from the cells before and after it. A cell connected to two sides
     * which are not opposite gets a corner shape, otherwise the rail runs straight along the axis of its neighbours.
     *
     * @param previous the cell the route comes from
     * @param current  the cell of the rail
     * @param next     the cell the route goes to, null for the last cell of the route
     * @return the shape property of the rail block
     */
    static String railShape(Vec previous, Vec current, @Nullable Vec next) {
        boolean north = false;
        boolean south = false;
        boolean east = false;
        boolean west = false;
        for (Vec neighbour : new Vec[]{previous, next}) {
            if (neighbour == null) continue;
            double dx = neighbour.x() - current.x();
            double dz = neighbour.z() - current.z();
            east |= dx > 0;
            west |= dx < 0;
            south |= dz > 0;
            north |= dz < 0;
        }
        if (north && east) return "north_east";
        if (north && west) return "north_west";
        if (south && east) return "south_east";
        if (south && west) return "south_west";
        if (north || south) return "north_south";
        return "east_west";
    }

    private static Vec centre(Vec cell) {
        return new Vec(cell.x() + 0.5, cell.y(), cell.z() + 0.5);
    }

    /**
     * The state of one cart: its entity, its position, its yaw and the route it drives.
     */
    private static final class Cart {

        private final Entity entity;
        private Vec position;
        private float yaw;
        private Route route;

        private Cart(Entity entity, Vec position) {
            this.entity = entity;
            this.position = position;
        }
    }

    /**
     * The route of a cart to one target: the cells from the first step to the target and how far the cart got.
     */
    private static final class Route {

        private final Vec target;
        private final List<Vec> cells;
        private final Vec start;
        private int index;
        private int railsSpawned;

        private Route(Vec target, List<Vec> cells, Vec start) {
            this.target = target;
            this.cells = cells;
            this.start = start;
        }
    }
}
