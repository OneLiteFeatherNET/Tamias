package net.onelitefeather.tamias.stats.player;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player of Tamias that can have stats in our database
 * @param uuid of the player
 * @param name of the player
 * @version 1.0.0
 * @since 0.1.0
 */
public record TamiasStatsPlayer(UUID uuid, String name) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TamiasStatsPlayer that)) return false;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
