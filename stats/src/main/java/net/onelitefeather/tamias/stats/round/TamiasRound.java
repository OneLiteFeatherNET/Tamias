package net.onelitefeather.tamias.stats.round;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One played round of a Tamias game.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
@Entity
@Table(name = "tamias_round")
public class TamiasRound {

    @Id
    @Column(name = "round_id")
    private UUID roundId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "map_name", nullable = false, length = 64)
    private String mapName;

    @Enumerated(EnumType.STRING)
    @Column(name = "winner", nullable = false, length = 16)
    private StatsRole winner;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    protected TamiasRound() {
        // for hibernate
    }

    public TamiasRound(FinishedRound round) {
        this.roundId = round.roundId();
        this.gameId = round.gameId();
        this.roundNumber = round.roundNumber();
        this.mapName = round.mapName();
        this.winner = round.winner();
        this.startedAt = round.startedAt();
        this.finishedAt = round.finishedAt();
    }

    public UUID getRoundId() {
        return roundId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getMapName() {
        return mapName;
    }

    public StatsRole getWinner() {
        return winner;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
