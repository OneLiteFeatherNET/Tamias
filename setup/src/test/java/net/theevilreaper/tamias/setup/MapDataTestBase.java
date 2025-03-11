package net.theevilreaper.tamias.setup;

import de.icevizion.aves.map.MapEntry;

import java.nio.file.Paths;

public abstract class MapDataTestBase {

    protected static MapEntry testMapEntry;

    static {
        testMapEntry = MapEntry.of(Paths.get(""));
    }
}
