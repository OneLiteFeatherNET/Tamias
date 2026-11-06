package net.onelitefeather.tamias.stats;

import net.onelitefeather.tamias.stats.player.PlayerStatsView;
import net.onelitefeather.tamias.stats.player.TamiasStatsPlayer;
import net.onelitefeather.tamias.stats.round.FinishedRound;
import net.onelitefeather.tamias.stats.round.StatsRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HibernateStatsRepositoryTest {

    private static final Instant START = Instant.parse("2026-11-06T18:00:00Z");

    private String jdbcUrl;
    private HibernateStatsRepository repository;

    @BeforeEach
    void setUp() {
        this.jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";
        this.repository = HibernateStatsRepository.open(new StatsConfig(this.jdbcUrl, "", "", 2, "tamias_stats"));
    }

    @AfterEach
    void tearDown() {
        this.repository.close();
    }

    private static FinishedRound round(Instant finishedAt, FinishedRound.PlayerResult... players) {
        return new FinishedRound(UUID.randomUUID(), UUID.randomUUID(), 1, "Forest", StatsRole.SURVIVOR,
                finishedAt.minusSeconds(120), finishedAt, List.of(players));
    }

    private static FinishedRound.PlayerResult result(UUID uuid, String name) {
        return new FinishedRound.PlayerResult(uuid, name, StatsRole.SURVIVOR, true, false, 0, 3);
    }

    private long count(String table) throws SQLException {
        try (Connection connection = DriverManager.getConnection(this.jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tamias_stats." + table)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    @Test
    void testSaveRoundStoresRoundAndPlayers() throws SQLException {
        this.repository.saveRound(round(START, result(UUID.randomUUID(), "Alice"), result(UUID.randomUUID(), "Bob")))
                .join();
        assertEquals(1, count("tamias_round"));
        assertEquals(2, count("tamias_round_stats"));
        assertEquals(2, count("tamias_player"));
        assertEquals(0, this.repository.failedWrites());
    }

    @Test
    void testDuplicateRoundFailsAndIsCounted() {
        FinishedRound round = round(START, result(UUID.randomUUID(), "Alice"));
        this.repository.saveRound(round).join();
        assertThrows(CompletionException.class, () -> this.repository.saveRound(round).join());
        assertEquals(1, this.repository.failedWrites());
    }

    @Test
    void testFindPlayerByNameIgnoresCase() {
        UUID uuid = UUID.randomUUID();
        this.repository.saveRound(round(START, result(uuid, "Alice"))).join();

        Optional<TamiasStatsPlayer> player = this.repository.findPlayerByName("aLiCe").join();
        assertTrue(player.isPresent());
        assertEquals(uuid, player.get().uuid());
        assertEquals("Alice", player.get().name());
        assertFalse(this.repository.findPlayerByName("Bob").join().isPresent());
    }

    @Test
    void testRenamedPlayerKeepsNewestName() {
        UUID uuid = UUID.randomUUID();
        this.repository.saveRound(round(START, result(uuid, "Alice"))).join();
        this.repository.saveRound(round(START.plusSeconds(600), result(uuid, "Alicia"))).join();

        assertEquals("Alicia", this.repository.findPlayerByName("alicia").join().orElseThrow().name());
        assertFalse(this.repository.findPlayerByName("alice").join().isPresent());
    }

    @Test
    void testNameSeenLastWins() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        this.repository.saveRound(round(START, result(first, "Alice"))).join();
        this.repository.saveRound(round(START.plusSeconds(600), result(second, "alice"))).join();

        assertEquals(second, this.repository.findPlayerByName("ALICE").join().orElseThrow().uuid());
    }

    @Test
    void testFindTotalsSumsAllRounds() {
        UUID uuid = UUID.randomUUID();
        this.repository.saveRound(round(START,
                new FinishedRound.PlayerResult(uuid, "Alice", StatsRole.SURVIVOR, true, false, 0, 3))).join();
        this.repository.saveRound(round(START.plusSeconds(600),
                new FinishedRound.PlayerResult(uuid, "Alice", StatsRole.BOMBER, true, false, 2, 0))).join();
        this.repository.saveRound(round(START.plusSeconds(1200),
                new FinishedRound.PlayerResult(uuid, "Alice", StatsRole.SURVIVOR, false, true, 0, 1))).join();

        PlayerStatsView totals = this.repository.findTotals(uuid).join().orElseThrow();
        assertEquals(new PlayerStatsView(new TamiasStatsPlayer(uuid, "Alice"), 3, 2, 1, 1, 2, 4), totals);
        assertEquals("Alice", totals.player().name());
    }

    @Test
    void testFindTotalsIsEmptyWithoutRounds() {
        assertFalse(this.repository.findTotals(UUID.randomUUID()).join().isPresent());
    }

    @Test
    void testDeleteByPlayerRemovesOnlyThatPlayer() throws SQLException {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        this.repository.saveRound(round(START, result(alice, "Alice"), result(bob, "Bob"))).join();
        this.repository.saveRound(round(START.plusSeconds(600), result(alice, "Alice"))).join();

        assertEquals(3, this.repository.deleteByPlayer(alice));
        assertEquals(1, count("tamias_round_stats"));
        assertEquals(1, count("tamias_player"));
        assertFalse(this.repository.findPlayerByName("Alice").join().isPresent());
        assertFalse(this.repository.findTotals(alice).join().isPresent());
    }

    @Test
    void testSaveAfterCloseFailsWithoutThrowing() {
        this.repository.close();
        assertThrows(CompletionException.class,
                () -> this.repository.saveRound(round(START, result(UUID.randomUUID(), "Alice"))).join());
        this.repository = HibernateStatsRepository.open(new StatsConfig(this.jdbcUrl, "", "", 2, "tamias_stats"));
    }
}
