package net.theevilreaper.tamias.common.area.placement;

/**
 * The type defines how a {@link TntGroup} is dropped onto the game area.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum TntDropType {

    /**
     * The center falls down and the members pop up around it.
     */
    NORMAL,
    /**
     * The center falls down from high above with braking thrusters and throws the members in small arcs around it.
     */
    SUPPLY
}
