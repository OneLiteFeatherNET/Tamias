package net.theevilreaper.tamias.common.ground;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * The {@link GroundDataRegistry} is used to store all available {@link GroundData} instances.
 * It's used to get a random ground data instance.
 *
 * @author Joltra
 * @version 1.0.0
 * @see GroundData
 * @since 1.0.0
 */
public sealed interface GroundDataRegistry permits TamiasGroundDataRegistry {

    /**
     * Returns the instance of the registry.
     *
     * @return the instance of the registry
     */
    static @NotNull GroundDataRegistry instance() {
        return TamiasGroundDataRegistry.RegistryInstance.INSTANCE;
    }

    /**
     * Adds a new {@link GroundData} instance to the registry.
     *
     * @param groundData the data to add
     */
    void add(@NotNull GroundData groundData);

    /**
     * Gets a random {@link GroundData} instance from the registry.
     *
     * @return the random data
     */
    @NotNull GroundData getRandomData();

    /**
     * Returns all available {@link GroundData} instances.
     *
     * @return the list of all available data
     */
    @NotNull
    @UnmodifiableView
    List<GroundData> getGroundData();
}
