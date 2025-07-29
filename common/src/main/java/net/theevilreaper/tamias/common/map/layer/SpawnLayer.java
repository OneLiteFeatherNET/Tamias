package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a spawn layer in the game, containing a position and a direction.
 * This class is immutable and can be constructed using the provided builder.
 *
 * @author Joltra
 * @version 1.0.0
 * @since 0.1.0
 */
public record SpawnLayer(@NotNull Pos pos, @NotNull Direction direction) {

    /**
     * Builder for creating instances of {@link SpawnLayer}.
     */
    @Contract(pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating instances of {@link SpawnLayer} with specified position and direction.
     *
     * @param spawnLayer the spawn layer to use for building
     * @return a new instance of {@link Builder}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Builder builder(@NotNull SpawnLayer spawnLayer) {
        return new Builder(spawnLayer);
    }

    /**
     * Builder class for constructing instances of {@link SpawnLayer}.
     *
     * @author Joltra
     * @version 1.0.0
     * @since 0.1.0
     */
    public static final class Builder {

        private Pos pos;
        private Direction direction;

        /**
         * Constructs a new Builder instance.
         * This constructor initializes the builder without any parameters.
         */
        private Builder() {
        }

        /**
         * Constructs a new Builder instance with the specified spawn layer.
         *
         * @param spawnLayer the spawn layer to use for building
         */
        private Builder(@NotNull SpawnLayer spawnLayer) {
            this.pos = spawnLayer.pos();
            this.direction = spawnLayer.direction();
        }

        /**
         * Sets the position for the spawn layer.
         *
         * @param pos the position to set
         * @return this builder instance for chaining
         */
        public Builder pos(Pos pos) {
            this.pos = pos;
            return this;
        }

        /**
         * Sets the direction for the spawn layer.
         *
         * @param direction the direction to set
         * @return this builder instance for chaining
         */
        public Builder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        /**
         * Builds a new instance of {@link SpawnLayer} with the specified position and direction.
         *
         * @return a new instance of {@link SpawnLayer}
         * @throws IllegalStateException if position or direction is not set
         */
        public @NotNull SpawnLayer build() {
            if (pos == null) {
                throw new IllegalStateException("Position is required for SpawnLayer");
            }
            if (direction == null) {
                throw new IllegalStateException("Direction is required for SpawnLayer");
            }
            return new SpawnLayer(pos, direction);
        }

        /**
         * Gets the direction of the spawn layer.
         *
         * @return the direction of the spawn layer
         */
        public @Nullable Direction getDirection() {
            return direction;
        }

        /**
         * Gets the position of the spawn layer.
         *
         * @return the position of the spawn layer
         */
        public @Nullable Pos getPos() {
            return pos;
        }
    }
}
