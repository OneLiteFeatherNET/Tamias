package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;

/**
 * The {@link EntityHelper} provides some utility methods to handle entities.
 * This class is not intended to be instantiated.
 * It provides methods to switch the player to a TNT entity and update the meta.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class EntityHelper {

    private static final int TNT_FUSE_TIME = 50;

    /**
     * Switches the player to a TNT entity.
     *
     * @param player the player to switch
     */
    public static void switchToTNT(Player player) {
        player.switchEntityType(EntityType.TNT);
    }

    /**
     * Switches the player to a player entity.
     *
     * @param player the player to switch
     */
    public static void switchToPlayer(Player player) {
        player.switchEntityType(EntityType.PLAYER);
    }

    /**
     * Updates the TNT meta for the player.
     *
     * @param player the player to update the meta
     */
    public static void updateTNTMeta(Player player) {
        if (player.getEntityType() != EntityType.TNT) return;

        player.editEntityMeta(PrimedTntMeta.class, meta -> meta.setFuseTime(TNT_FUSE_TIME));
    }

    private EntityHelper() {

    }
}
