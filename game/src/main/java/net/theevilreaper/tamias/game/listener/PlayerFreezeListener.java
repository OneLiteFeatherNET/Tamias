package net.theevilreaper.tamias.game.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.theevilreaper.tamias.common.util.Tags;

import java.util.function.Consumer;

/**
 * Locks a frozen player's horizontal position while still allowing them to look around and letting gravity
 * resolve their Y position normally - locking Y as well would fight the client's own ground-snapping/falling
 * and cause jitter.
 * <p>
 * Deliberately does not touch {@link net.minestom.server.entity.attribute.Attribute#MOVEMENT_SPEED} - the vanilla
 * client derives its FOV zoom effect from that attribute (the same mechanic behind the Slowness potion), so zeroing
 * it to freeze a player also zooms their camera in for as long as the freeze lasts.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayerFreezeListener implements Consumer<PlayerMoveEvent> {

    @Override
    public void accept(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.hasTag(Tags.FROZEN)) return;

        Pos anchor = player.getPosition();
        Pos target = event.getNewPosition();
        event.setNewPosition(new Pos(anchor.x(), target.y(), anchor.z(), target.yaw(), target.pitch()));
    }
}
