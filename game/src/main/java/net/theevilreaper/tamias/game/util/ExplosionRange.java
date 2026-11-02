package net.theevilreaper.tamias.game.util;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;

/**
 * Decides which players an explosion reaches. The radius is measured from the middle of the TNT to the nearest point
 * of a player's hit box, so a survivor whose body touches the radius is hit regardless of the height difference.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ExplosionRange {

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
