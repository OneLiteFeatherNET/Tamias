package net.theevilreaper.tamias.setup.listener.map;

import de.icevizion.aves.map.MapEntry;
import de.icevizion.aves.util.functional.PlayerConsumer;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.setup.data.SetupData;
import net.theevilreaper.tamias.setup.event.MapSetupFinishEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MapSetupFinishListener implements Consumer<MapSetupFinishEvent> {

    private final MapProvider mapProvider;
    private final PlayerConsumer instanceSwitcher;

    public MapSetupFinishListener(@NotNull MapProvider mapProvider, @NotNull PlayerConsumer instanceSwitcher) {
        this.mapProvider = mapProvider;
        this.instanceSwitcher = instanceSwitcher;
    }

    @Override
    public void accept(@NotNull MapSetupFinishEvent event) {
        SetupData setupData = event.setupData();

        if (setupData.hasMap()) {
            MapEntry mapEntry = setupData.getMapEntry();
            this.mapProvider.saveMap(mapEntry.getMapFile(), setupData.getBaseMap());
        }
        this.instanceSwitcher.accept(setupData.getPlayer());
        setupData.reset();
    }
}
