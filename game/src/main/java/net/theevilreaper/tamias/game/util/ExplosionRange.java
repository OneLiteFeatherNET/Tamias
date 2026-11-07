package net.theevilreaper.tamias.game.util;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.theevilreaper.tamias.game.player.TamiasPlayer;

/**
 * Decides which players an explosion reaches. The radius is measured from the middle of the TNT to the nearest point
 * of a player's hit box, so a survivor whose body touches the radius is hit regardless of the height difference.
 * <p>
 * The bomber sees the other players delayed by the latency and the client interpolation. The check therefore also
 * looks at the position a player had that long ago, capped at {@link #MAX_REWIND_MILLIS}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ExplosionRange {

    /**
     * The client interpolates other players over 3 ticks, on average they lag about 2 ticks behind.
     */
    static final long INTERPOLATION_MILLIS = 100;
    /**
     * The furthest an explosion reaches back in time, otherwise a high latency would hit players who are long gone.
     */
    public static final long MAX_REWIND_MILLIS = TamiasPlayer.POSITION_HISTORY_MILLIS;

    /**
     * Returns the middle of the bomber's box, the point the explosion is measured from.
     *
     * @param feet the position of the bomber
     * @param box  the box of the bomber
     * @return the center of the explosion
     */
    public static Point center(Point feet, BoundingBox box) {
        return feet.add(0, box.minY() + box.height() / 2, 0);
    }

    /**
     * Returns how far the explosion reaches back in time for a bomber with the given latency.
     *
     * @param latency the latency of the bomber in milliseconds
     * @return the rewind in milliseconds, between the interpolation and {@link #MAX_REWIND_MILLIS}
     */
    public static long rewindMillis(int latency) {
        return Math.clamp(Math.max(0, latency) + INTERPOLATION_MILLIS, 0, MAX_REWIND_MILLIS);
    }

    /**
     * Tells whether the explosion reaches a hit box.
     *
     * @param center    the center of the explosion
     * @param radius    the radius of the explosion
     * @param targetPos the position of the target
     * @param targetBox the box of the target, relative to its position
     * @return true if the nearest point of the box is within the radius
     */
    public static boolean reaches(Point center, double radius, Point targetPos, BoundingBox targetBox) {
        double dx = center.x() - Math.clamp(center.x(), targetPos.x() + targetBox.minX(), targetPos.x() + targetBox.maxX());
        double dy = center.y() - Math.clamp(center.y(), targetPos.y() + targetBox.minY(), targetPos.y() + targetBox.maxY());
        double dz = center.z() - Math.clamp(center.z(), targetPos.z() + targetBox.minZ(), targetPos.z() + targetBox.maxZ());
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private ExplosionRange() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
