package net.theevilreaper.tamias.setup.util;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.theevilreaper.tamias.common.util.DirectionFaceHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The {@link DirectionUtil} provides utility methods for the direction of the player.
 * The class is used to parse the direction of the player.
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author Joltras
 */
public final class DirectionUtil {

    /**
     * Parses the direction of the player.
     *
     * @param player the player to parse the direction from
     * @return the direction of the player
     */
    public static @NotNull Optional<Direction> parseDirection(@NotNull Player player) {
        Direction direction = MathUtils.getHorizontalDirection(player.getPosition().yaw());

        Vec dir = player.getPosition().direction();

        double directionPitch = Math.toDegrees(-Math.atan2(dir.y(), Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z())));

        if (!DirectionFaceHelper.isValidFace(directionPitch)) {
            String indirectDirection = DirectionFaceHelper.getInvalidDirection(directionPitch).name();
            player.sendMessage(SetupMessages.getInvalidFace(indirectDirection));
            return Optional.empty();
        }

        return Optional.of(direction);
    }

    private DirectionUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
