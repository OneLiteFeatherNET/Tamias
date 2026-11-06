package net.onelitefeather.tamias.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens the {@link StatsRepository} that fits a {@link StatsConfig}.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
public final class StatsRepositories {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsRepositories.class);

    private StatsRepositories() {
    }

    /**
     * Opens the repository for the given settings. Disabled settings, or a database that cannot be reached or
     * migrated, give the repository that keeps nothing: the game never fails to start because of its statistics.
     *
     * @param config the settings
     * @return the repository, {@link StatsRepository#enabled()} tells whether it stores anything
     */
    public static StatsRepository open(StatsConfig config) {
        if (!config.enabled()) {
            LOGGER.info("No stats.jdbc-url configured, round statistics are off");
            return DisabledStatsRepository.INSTANCE;
        }
        try {
            StatsRepository repository = HibernateStatsRepository.open(config);
            LOGGER.info("Round statistics are stored with {}", config);
            return repository;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to open the statistics database with {}, they stay off", config, exception);
            return DisabledStatsRepository.INSTANCE;
        }
    }
}
