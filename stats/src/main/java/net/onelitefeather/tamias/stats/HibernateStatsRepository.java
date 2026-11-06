package net.onelitefeather.tamias.stats;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.onelitefeather.tamias.stats.player.PlayerStatsView;
import net.onelitefeather.tamias.stats.player.StatsPlayerEntity;
import net.onelitefeather.tamias.stats.player.TamiasStatsPlayer;
import net.onelitefeather.tamias.stats.round.FinishedRound;
import net.onelitefeather.tamias.stats.round.PlayerRoundStats;
import net.onelitefeather.tamias.stats.round.StatsRole;
import net.onelitefeather.tamias.stats.round.TamiasRound;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Stores the round statistics with Hibernate on a Hikari pool. Flyway owns the schema.
 * <p>
 * All reads and writes run one after the other on a single thread, so the calling thread never waits for the
 * database and a round is never written while an older one is still pending.
 * </p>
 *
 * @version 1.0.0
 * @since 0.1.0
 */
final class HibernateStatsRepository implements StatsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateStatsRepository.class);
    private static final long CLOSE_TIMEOUT_SECONDS = 10;

    private final HikariDataSource dataSource;
    private final SessionFactory sessionFactory;
    private final ExecutorService executor;
    private final AtomicLong failedWrites = new AtomicLong();

    private HibernateStatsRepository(HikariDataSource dataSource, SessionFactory sessionFactory) {
        this.dataSource = dataSource;
        this.sessionFactory = sessionFactory;
        this.executor = Executors.newSingleThreadExecutor(Thread.ofPlatform().name("tamias-stats").daemon().factory());
    }

    /**
     * Connects, migrates the schema and builds the session factory.
     *
     * @param config enabled settings
     * @return the repository
     */
    static HibernateStatsRepository open(StatsConfig config) {
        migrate(config);
        HikariDataSource dataSource = dataSource(config);
        try {
            return new HibernateStatsRepository(dataSource, sessionFactory(dataSource, config.schema()));
        } catch (RuntimeException exception) {
            dataSource.close();
            throw exception;
        }
    }

    /**
     * Migrates the schema on connections of its own: Flyway holds more than one at a time, which a small pool could
     * not give it.
     */
    private static void migrate(StatsConfig config) {
        Flyway.configure(HibernateStatsRepository.class.getClassLoader())
                .dataSource(config.jdbcUrl(), nullIfEmpty(config.username()), nullIfEmpty(config.password()))
                .schemas(config.schema())
                .defaultSchema(config.schema())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private static @Nullable String nullIfEmpty(String value) {
        return value.isEmpty() ? null : value;
    }

    private static HikariDataSource dataSource(StatsConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("tamias-stats");
        hikari.setJdbcUrl(config.jdbcUrl());
        if (!config.username().isEmpty()) {
            hikari.setUsername(config.username());
        }
        if (!config.password().isEmpty()) {
            hikari.setPassword(config.password());
        }
        hikari.setMaximumPoolSize(config.poolSize());
        hikari.setMinimumIdle(1);
        return new HikariDataSource(hikari);
    }

    private static SessionFactory sessionFactory(HikariDataSource dataSource, String schema) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource)
                .applySetting(AvailableSettings.DEFAULT_SCHEMA, schema)
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .build();
        try {
            return new MetadataSources(registry)
                    .addAnnotatedClasses(TamiasRound.class, PlayerRoundStats.class, StatsPlayerEntity.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (RuntimeException exception) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw exception;
        }
    }

    @Override
    public CompletableFuture<Void> saveRound(FinishedRound round) {
        return this.<Void>submit(() -> {
            this.sessionFactory.inTransaction(session -> write(session, round));
            return null;
        }).whenComplete((_, throwable) -> {
            if (throwable != null) {
                long failed = this.failedWrites.incrementAndGet();
                LOGGER.error("Failed to store round {} ({} failed writes so far)", round.roundId(), failed, throwable);
            }
        });
    }

    private static void write(Session session, FinishedRound round) {
        session.persist(new TamiasRound(round));
        for (FinishedRound.PlayerResult result : round.players()) {
            session.persist(new PlayerRoundStats(round.roundId(), result));
            StatsPlayerEntity player = session.find(StatsPlayerEntity.class, result.playerUuid());
            if (player == null) {
                session.persist(new StatsPlayerEntity(result.playerUuid(), result.playerName(), round.finishedAt()));
            } else {
                player.seen(result.playerName(), round.finishedAt());
            }
        }
    }

    @Override
    public CompletableFuture<Optional<TamiasStatsPlayer>> findPlayerByName(String name) {
        return submit(() -> this.sessionFactory.fromSession(session -> session
                .createSelectionQuery("from StatsPlayerEntity where nameKey = :key order by lastSeen desc",
                        StatsPlayerEntity.class)
                .setParameter("key", StatsPlayerEntity.nameKey(name))
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(StatsPlayerEntity::toPlayer)));
    }

    @Override
    public CompletableFuture<Optional<PlayerStatsView>> findTotals(UUID playerUuid) {
        return submit(() -> this.sessionFactory.fromSession(session -> {
            StatsPlayerEntity player = session.find(StatsPlayerEntity.class, playerUuid);
            if (player == null) {
                return Optional.<PlayerStatsView>empty();
            }
            Object[] totals = session.createSelectionQuery("""
                            select count(*),
                                   sum(case when s.won then 1 else 0 end),
                                   sum(case when s.won and s.startRole = :survivor then 1 else 0 end),
                                   sum(case when s.won and s.startRole = :bomber then 1 else 0 end),
                                   sum(s.kills),
                                   sum(s.hits)
                            from PlayerRoundStats s
                            where s.key.playerUuid = :uuid""", Object[].class)
                    .setParameter("uuid", playerUuid)
                    .setParameter("survivor", StatsRole.SURVIVOR)
                    .setParameter("bomber", StatsRole.BOMBER)
                    .getSingleResult();
            long rounds = number(totals[0]);
            if (rounds == 0) {
                return Optional.<PlayerStatsView>empty();
            }
            return Optional.of(new PlayerStatsView(player.toPlayer(), rounds, number(totals[1]), number(totals[2]),
                    number(totals[3]), number(totals[4]), number(totals[5])));
        }));
    }

    private static long number(@Nullable Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    @Override
    public int deleteByPlayer(UUID playerUuid) {
        return this.sessionFactory.fromTransaction(session -> {
            int rounds = session.createMutationQuery("delete from PlayerRoundStats where key.playerUuid = :uuid")
                    .setParameter("uuid", playerUuid)
                    .executeUpdate();
            int players = session.createMutationQuery("delete from StatsPlayerEntity where uuid = :uuid")
                    .setParameter("uuid", playerUuid)
                    .executeUpdate();
            return rounds + players;
        });
    }

    /**
     * Tells how many round writes failed since the repository was opened.
     *
     * @return the number of failed writes
     */
    long failedWrites() {
        return this.failedWrites.get();
    }

    private <T> CompletableFuture<T> submit(Supplier<T> task) {
        try {
            return CompletableFuture.supplyAsync(task, this.executor);
        } catch (RejectedExecutionException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    @Override
    public void close() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.warn("Statistics writes did not finish within {} seconds, dropping them", CLOSE_TIMEOUT_SECONDS);
                this.executor.shutdownNow();
            }
        } catch (InterruptedException _) {
            this.executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        this.sessionFactory.close();
        this.dataSource.close();
    }
}
