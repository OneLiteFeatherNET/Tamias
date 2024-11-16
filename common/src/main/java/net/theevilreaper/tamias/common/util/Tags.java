package net.theevilreaper.tamias.common.util;

import net.minestom.server.tag.Tag;

import java.util.UUID;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class Tags {

    public static final Tag<Byte> TEAM_ID = Tag.Byte("teamId");
    public static final Tag<Byte> ITEM_TAG = Tag.Byte("itemTag");
    public static final Tag<UUID> SHOOTER_ID = Tag.UUID("shooter");

    private Tags() { }
}
