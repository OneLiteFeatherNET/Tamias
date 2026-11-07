package net.onelitefeather.tamias.stats.round;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;

/**
 * The result of one player in one {@link TamiasRound}.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
@Entity
@Table(name = "tamias_round_stats")
public class PlayerRoundStats {

    @EmbeddedId
    private Key key;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_role", nullable = false, length = 16)
    private StatsRole startRole;

    @Column(name = "won", nullable = false)
    private boolean won;

    @Column(name = "converted", nullable = false)
    private boolean converted;

    @Column(name = "kills", nullable = false)
    private int kills;

    @Column(name = "hits", nullable = false)
    private int hits;

    @Column(name = "explosions", nullable = false)
    private int explosions;

    protected PlayerRoundStats() {
        // for hibernate
    }

    public PlayerRoundStats(UUID roundId, FinishedRound.PlayerResult result) {
        this.key = new Key(roundId, result.playerUuid());
        this.startRole = result.startRole();
        this.won = result.won();
        this.converted = result.converted();
        this.kills = result.kills();
        this.hits = result.hits();
        this.explosions = result.explosions();
    }

    public Key getKey() {
        return key;
    }

    public StatsRole getStartRole() {
        return startRole;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isConverted() {
        return converted;
    }

    public int getKills() {
        return kills;
    }

    public int getHits() {
        return hits;
    }

    public int getExplosions() {
        return explosions;
    }

    @Embeddable
    public record Key(
            @Column(name = "round_id") UUID roundId,
            @Column(name = "player_uuid") UUID playerUuid
    ) implements Serializable {
    }
}
