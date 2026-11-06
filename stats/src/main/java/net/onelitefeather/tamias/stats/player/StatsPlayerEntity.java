package net.onelitefeather.tamias.stats.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * The last name a player was seen with, so commands can find a player by name.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
@Entity
@Table(name = "tamias_player")
public class StatsPlayerEntity {

    @Id
    @Column(name = "player_uuid")
    private UUID uuid;

    @Column(name = "last_name", nullable = false, length = 16)
    private String lastName;

    @Column(name = "name_key", nullable = false, length = 16)
    private String nameKey;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    protected StatsPlayerEntity() {
        // for hibernate
    }

    public StatsPlayerEntity(UUID uuid, String name, Instant seenAt) {
        this.uuid = uuid;
        this.lastName = name;
        this.nameKey = nameKey(name);
        this.lastSeen = seenAt;
    }

    /**
     * Turns a name into the case-insensitive form it is looked up by.
     *
     * @param name the name
     * @return the lower-case name
     */
    public static String nameKey(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Records that the player was seen with a name. Older sightings than the stored one are ignored.
     *
     * @param name   the name
     * @param seenAt when the player was seen
     */
    public void seen(String name, Instant seenAt) {
        if (seenAt.isBefore(this.lastSeen)) {
            return;
        }
        this.lastName = name;
        this.nameKey = nameKey(name);
        this.lastSeen = seenAt;
    }

    public TamiasStatsPlayer toPlayer() {
        return new TamiasStatsPlayer(this.uuid, this.lastName);
    }
}
