package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("java:S3252")
public final class EntityHelper {

    private static final int TNT_FUSE_TIME = 50;

    public static void switchToTNT(@NotNull Player player) {
        player.switchEntityType(EntityType.TNT);
    }

    public static void switchToPlayer(@NotNull Player player) {
        player.switchEntityType(EntityType.PLAYER);
    }

    public static void updateTNTMeta(@NotNull Player player) {
        if (player.getEntityType() != EntityType.TNT) return;

        player.editEntityMeta(PrimedTntMeta.class, primedTntMeta -> primedTntMeta.setFuseTime(TNT_FUSE_TIME));
    }

    private EntityHelper() {

    }
}
