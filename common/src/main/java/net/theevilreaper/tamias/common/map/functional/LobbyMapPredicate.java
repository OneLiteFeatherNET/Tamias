package net.theevilreaper.tamias.common.map.functional;

import net.theevilreaper.aves.map.MapEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A predicate to filter maps that are specifically for the lobby.
 * It checks if the directory root of the map entry ends with "lobby".
 *
 * @author Joltras
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LobbyMapPredicate implements Predicate<MapEntry> {

    /**
     * Tests if the given map entry is a lobby map.
     *
     * @param mapEntry the input argument to test
     * @return true if the map entry is a lobby map, false otherwise
     */
    @Override
    public boolean test(@NotNull MapEntry mapEntry) {
        return mapEntry.getDirectoryRoot().endsWith("lobby");
    }
}
