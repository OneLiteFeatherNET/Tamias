package net.theevilreaper.tamias.game.util;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import net.minestom.server.timer.TaskSchedule;
import net.theevilreaper.tamias.common.firework.FireworkEffect;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The {@link FireworkUtil} launches the fireworks which celebrate the end of a round.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class FireworkUtil {

    /**
     * The entity status which lets the client render the explosion of a firework rocket.
     */
    private static final byte EXPLODE_STATUS = 17;
    private static final int FLIGHT_TICKS = 30;
    private static final Vec LAUNCH_VELOCITY = new Vec(0, 15, 0);

    /**
     * Launches a firework at the position of the player, which flies up and explodes.
     *
     * @param player the player to launch the firework for
     * @param color  the color of the explosion or null to use a random one
     */
    public static void launch(Player player, @Nullable RGBLike color) {
        Instance instance = player.getInstance();
        if (instance == null) return;

        RGBLike explosionColor = color != null ? color : randomColor();
        FireworkList fireworkList = FireworkEffect.builder()
                .flightDuration((byte) 1)
                .effect(builder -> builder.shape(FireworkExplosion.Shape.LARGE_BALL)
                        .color(explosionColor)
                        .fade(randomColor())
                        .trail()
                        .twinkle())
                .build();

        Entity firework = new Entity(EntityType.FIREWORK_ROCKET);
        FireworkRocketMeta rocketMeta = (FireworkRocketMeta) firework.getEntityMeta();
        rocketMeta.setFireworkInfo(ItemStack.builder(Material.FIREWORK_ROCKET)
                .set(DataComponents.FIREWORKS, fireworkList)
                .build());
        firework.setNoGravity(true);
        firework.setVelocity(LAUNCH_VELOCITY);
        firework.setInstance(instance, player.getPosition());
        firework.scheduler().buildTask(() -> {
            firework.triggerStatus(EXPLODE_STATUS);
            firework.remove();
        }).delay(TaskSchedule.tick(FLIGHT_TICKS)).schedule();
    }

    private static RGBLike randomColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private FireworkUtil() {
        throw new UnsupportedOperationException("The FireworkUtil class can not be instantiated");
    }
}
