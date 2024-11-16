package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("java:S3252")
public enum DataType {

    SPAWN(Material.GREEN_BED),
    BOMBER(Material.TNT),
    SURVIVOR(Material.GREEN_DYE);

    private final Material material;

    DataType(@NotNull Material material) {
        this.material = material;
    }

    public @NotNull Material getMaterial() {
        return material;
    }
}

