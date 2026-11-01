package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.ground.SpeedBlock;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.util.SpectatorHelper;

import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Gives a player a short velocity boost in the looking direction when the player steps onto a {@link SpeedBlock}.
 * The listener is only registered while the ground of the round contains speed blocks, see {@link SpeedBlockHandler}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SpeedBlockListener implements Consumer<PlayerMoveEvent> {

    /**
     * The time in milliseconds until a player can get the next boost.
     */
    static final long COOLDOWN_MILLIS = 1000;
    /**
     * The upward velocity in blocks per second, so the player doesn't drag over the ground.
     */
    static final double VERTICAL_BOOST = 4.0;
    // The block under the feet is looked up slightly below the position, a player stands exactly on the block edge
    private static final double GROUND_OFFSET = 0.2;

    private final LongSupplier clock;

    /**
     * Creates a new listener which uses the system time for the cooldown.
     */
    public SpeedBlockListener() {
        this(System::currentTimeMillis);
    }

    /**
     * Creates a new listener with the given clock for the cooldown.
     *
     * @param clock provides the current time in milliseconds
     */
    SpeedBlockListener(LongSupplier clock) {
        this.clock = clock;
    }

    @Override
    public void accept(PlayerMoveEvent event) {
        if (!event.isOnGround()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR || SpectatorHelper.isSpectator(player)) return;

        Pos newPosition = event.getNewPosition();
        // Only a step onto another block can trigger a boost, a head rotation or a small step doesn't need a lookup
        if (isSameBlock(player.getPosition(), newPosition)) return;

        Instance instance = player.getInstance();
        if (instance == null) return;
        Block ground = instance.getBlock(newPosition.sub(0, GROUND_OFFSET, 0));
        if (!SpeedBlock.isSpeedBlock(ground)) return;

        long now = this.clock.getAsLong();
        Long cooldown = player.getTag(Tags.SPEED_BOOST_COOLDOWN);
        if (cooldown != null && now < cooldown) return;
        player.setTag(Tags.SPEED_BOOST_COOLDOWN, now + COOLDOWN_MILLIS);

        Vec direction = newPosition.direction().withY(0);
        Vec horizontal = direction.isZero() ? Vec.ZERO : direction.normalize().mul(SpeedBlock.getBoost(ground));
        player.setVelocity(horizontal.withY(VERTICAL_BOOST));
    }

    /**
     * Checks if both positions stand on the same block.
     *
     * @param oldPosition the position before the move
     * @param newPosition the position after the move
     * @return true if the block under both positions is the same
     */
    private static boolean isSameBlock(Pos oldPosition, Pos newPosition) {
        return oldPosition.blockX() == newPosition.blockX()
                && oldPosition.blockZ() == newPosition.blockZ()
                && Math.floor(oldPosition.y() - GROUND_OFFSET) == Math.floor(newPosition.y() - GROUND_OFFSET);
    }
}
