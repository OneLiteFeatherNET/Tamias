package net.theevilreaper.tamias.setup.commands.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SpawnType {

    MAP_SPAWN("spawn"),
    SPECTATOR("spectator"),
    BOMBER("bomber"),
    SURVIVOR("survivor");

    private static final SpawnType[] VALUES = values();
    private final String displayName;

    SpawnType(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public static @Nullable SpawnType fromString(@NotNull String name) {
        SpawnType type = null;
        for (int i = 0; i < VALUES.length && type == null; i++) {
            SpawnType current = VALUES[i];
            if (current.getDisplayName().equalsIgnoreCase(name)) {
                type = current;
            }
        }
        return type;
    }

    public static @NotNull String[] getAsArray() {
        String[] array = new String[VALUES.length];
        for (int i = 0; i < VALUES.length; i++) {
            array[i] = VALUES[i].getDisplayName();
        }
        return array;
    }
}
