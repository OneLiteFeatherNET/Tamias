package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.item.Material;

/**
 * The {@link DataType} enum contains all data types which can be selected in the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
@SuppressWarnings("java:S3252")
public enum DataType {

    SPAWN(Material.GREEN_BED),
    BOMBER(Material.TNT),
    SURVIVOR(Material.GREEN_DYE);

    private final Material material;

    /**
     * Creates a new data type with the given material.
     *
     * @param material the material of the data type
     */
    DataType(Material material) {
        this.material = material;
    }

    /**
     * Returns the material of the data type.
     *
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }
}

