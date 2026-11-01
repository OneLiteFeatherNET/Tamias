package net.theevilreaper.tamias.common.util;

import net.minestom.server.tag.Tag;

import java.util.UUID;

/**
 * The class stores some tags which are used in certain places of the game,
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class Tags {

    public static final Tag<String> TEAM_KEY = Tag.String("team_key");
    public static final Tag<Byte> ITEM_TAG = Tag.Byte("itemTag");
    public static final Tag<UUID> SHOOTER_ID = Tag.UUID("shooter");
    public static final Tag<Boolean> SPECTATOR = Tag.Boolean("spectator");
    // Stores the time in milliseconds until the bomber can't be shot after a respawn
    public static final Tag<Long> SPAWN_PROTECTION = Tag.Long("spawn_protection");
    // Marks a ground block as speed block, the value is the horizontal boost in blocks per second
    public static final Tag<Double> SPEED_BOOST = Tag.Double("speed_boost");
    // Stores the time in milliseconds until the player can get the next speed boost
    public static final Tag<Long> SPEED_BOOST_COOLDOWN = Tag.Long("speed_boost_cooldown");

    private Tags() {
        // Nothing to do here due a utility class
    }
}
