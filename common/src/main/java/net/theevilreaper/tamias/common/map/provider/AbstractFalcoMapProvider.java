package net.theevilreaper.tamias.common.map.provider;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import net.onelitefeather.falco.anvil.FalcoAnvilLoader;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.aves.map.provider.AbstractMapProvider;
import net.theevilreaper.aves.util.functional.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class extending {@link AbstractMapProvider} that uses {@link FalcoAnvilLoader}
 * for parallel chunk loading and handles resource cleanup via {@link AutoCloseable}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractFalcoMapProvider extends AbstractMapProvider implements AutoCloseable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractFalcoMapProvider.class);

    protected final List<FalcoAnvilLoader> chunkLoaders;

    protected AbstractFalcoMapProvider(FileHandler fileHandler, PathFilter<MapEntry> mapFilter) {
        super(fileHandler, mapFilter);
        this.chunkLoaders = new ArrayList<>();
    }

    /**
     * Registers the given instance with a {@link FalcoAnvilLoader} attached to it.
     *
     * @param instance the instance the map is loaded into
     * @param mapEntry the map entry whose directory root is the world root of the loader
     */
    protected void registerFalcoInstance(InstanceContainer instance, MapEntry mapEntry) {
        FalcoAnvilLoader chunkLoader = new FalcoAnvilLoader(
                mapEntry.getDirectoryRoot(),
                DimensionType.OVERWORLD.key()
        );
        this.chunkLoaders.add(chunkLoader);

        instance.setChunkLoader(chunkLoader);
        instance.enableAutoChunkLoad(true);

        var defaultClock = instance.defaultClock();
        if (defaultClock != null) {
            defaultClock.rate(0f);
        }
        MinecraftServer.getInstanceManager().registerInstance(instance);
    }

    /**
     * Closes every {@link FalcoAnvilLoader} opened by this provider.
     * Prevents region file locks and memory leaks upon server shutdown.
     */
    @Override
    public void close() {
        for (FalcoAnvilLoader chunkLoader : this.chunkLoaders) {
            try {
                chunkLoader.close();
            } catch (IOException exception) {
                LOGGER.error("Failed to close FalcoAnvilLoader", exception);
            }
        }
        this.chunkLoaders.clear();
    }
}
