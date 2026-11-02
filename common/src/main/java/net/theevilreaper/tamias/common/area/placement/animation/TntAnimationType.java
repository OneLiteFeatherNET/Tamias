package net.theevilreaper.tamias.common.area.placement.animation;

import net.theevilreaper.tamias.common.util.RandomPick;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * The animations which can be picked for a run of TNT drops. Every run creates a new instance of the picked type,
 * because an animation holds the state of exactly one run.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum TntAnimationType {

    /**
     * The TNT falls down from above onto the ground.
     */
    FALLING(FallingTntAnimation::new),

    /**
     * The TNT grows on the spot from very small to its full size.
     */
    GROWING(GrowingTntAnimation::new),

    /**
     * Villagers rush around and place the TNT on the ground.
     */
    VILLAGERS(VillagerTntAnimation::new),

    /**
     * The TNT is shot from the middle of the game area in an arc onto its position.
     */
    SHOOTING(ShootingTntAnimation::new),

    /**
     * A minecart drives over short-lived rails to every position and leaves the TNT behind it.
     */
    MINECART(MinecartTntAnimation::new),

    /**
     * Flying animals deliver the TNT from a depot above the game area. The animal is picked at random for every run,
     * so the flying animation is picked as often as any other animation.
     */
    FLYING(() -> FlyingDeliveryTntAnimation.withRandomCarrier(ThreadLocalRandom.current())),

    /**
     * Endermen teleport next to a position, place the TNT and teleport away.
     */
    ENDERMEN(EndermanTntAnimation::new),

    /**
     * The TNT races from the middle of the game area over the ground to its position and leaves a light trail.
     */
    TRON(TronTntAnimation::new);

    private final Supplier<TntAnimation> factory;

    TntAnimationType(Supplier<TntAnimation> factory) {
        this.factory = factory;
    }

    /**
     * Creates a new animation of this type for one run.
     *
     * @return a fresh animation instance
     */
    public TntAnimation create() {
        return this.factory.get();
    }

    /**
     * Picks a random type which differs from the previous one, so the same animation never runs twice in a row.
     *
     * @param previous the type of the last run, null if there was none
     * @param random   the random generator to use
     * @return the picked type
     */
    public static TntAnimationType next(@Nullable TntAnimationType previous, RandomGenerator random) {
        return RandomPick.nextDifferent(values(), previous, random);
    }
}
