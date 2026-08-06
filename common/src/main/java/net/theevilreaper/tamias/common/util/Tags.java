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

    private Tags() {
        // Nothing to do here due a utility class
    }
}
