package net.theevilreaper.tamias.common.firework;

import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a builder for creating a firework effect.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see FireworkList
 * @since 1.0.0
 */
public sealed interface FireworkEffect permits FireworkEffectBuilder {

    /**
     * Create a new firework effect builder.
     *
     * @return the created builder
     */
    @Contract(pure = true)
    static @NotNull FireworkEffect builder() {
        return new FireworkEffectBuilder();
    }

    /**
     * Set the flight duration of the firework.
     *
     * @param duration the duration of the flight
     * @return the firework effect
     */
    @NotNull FireworkEffect flightDuration(byte duration);

    /**
     * Add a firework explosion to the list.
     *
     * @param fireworkExplosion the explosion to add
     * @return the firework effect
     */
    @NotNull FireworkEffect effect(@NotNull FireworkExplosion fireworkExplosion);

    /**
     * Add a firework explosion to the list.
     *
     * @param builder the builder to create the explosion
     * @return the firework effect
     */
    @NotNull FireworkEffect effect(@NotNull Consumer<FireworkEffectDataBuilder> builder);

    /**
     * Add a list of firework explosions to the list.
     *
     * @param fireworkExplosion the list of explosions
     * @return the firework effect
     */
    @NotNull FireworkEffect effects(@NotNull List<FireworkExplosion> fireworkExplosion);

    /**
     * Build the firework list.
     *
     * @return the created {@link FireworkList}
     */
    @NotNull FireworkList build();
}
