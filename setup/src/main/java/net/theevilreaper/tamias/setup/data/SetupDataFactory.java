package net.theevilreaper.tamias.setup.data;

import net.minestom.server.entity.Player;
import net.theevilreaper.aves.map.MapEntry;

import java.util.UUID;

/**
 * The {@link SetupDataFactory} is used to create new instances of {@link InstanceSetupData}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class SetupDataFactory {

    /**
     * Creates a new instance of an {@link InstanceSetupData} based on the provided parameters.
     *
     * @param player    who owns the data
     * @param mapEntry  that contains the path reference
     * @param lobbyMode if the data is for the lobby or the game
     * @return the created instance
     */
    public static InstanceSetupData create(Player player, MapEntry mapEntry, boolean lobbyMode) {
        final UUID uuid = player.getUuid();
        return lobbyMode ? new LobbyData(uuid, mapEntry) : new GameData(uuid, mapEntry);
    }

    private SetupDataFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
