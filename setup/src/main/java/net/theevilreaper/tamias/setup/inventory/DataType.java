package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link DataType} enum contains all data types which can be selected in the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
@SuppressWarnings("java:S3252")
public enum DataType {

    NAME(Material.NAME_TAG),
    AUTHOR(Material.OAK_SIGN),
    SPAWN(Material.GREEN_BED),
    BOMBER(Material.TNT),
    SURVIVOR(Material.GREEN_DYE);

    private static final DataType[] VALUES = values();
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

    /**
     * Returns the data type for the given ordinal.
     *
     * @param ordinal the ordinal
     * @return the data type or null if the ordinal is out of range
     */
    public static @Nullable DataType fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) return null;
        return VALUES[ordinal];
    }
}

