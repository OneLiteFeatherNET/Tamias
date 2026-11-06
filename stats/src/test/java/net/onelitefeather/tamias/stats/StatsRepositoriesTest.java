package net.onelitefeather.tamias.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsRepositoriesTest {

    @Test
    void testDisabledConfigGivesDisabledRepository() {
        try (StatsRepository repository = StatsRepositories.open(StatsConfig.DISABLED)) {
            assertFalse(repository.enabled());
        }
    }

    @Test
    void testUnreachableDatabaseGivesDisabledRepository() {
        StatsConfig config = new StatsConfig("jdbc:h2:mem:missing;IFEXISTS=TRUE", "", "", 1, "tamias_stats");
        try (StatsRepository repository = StatsRepositories.open(config)) {
            assertFalse(repository.enabled());
        }
    }

    @Test
    void testWorkingDatabaseGivesEnabledRepository() {
        StatsConfig config = new StatsConfig("jdbc:h2:mem:enabled;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "", "", 1, "tamias_stats");
        try (StatsRepository repository = StatsRepositories.open(config)) {
            assertTrue(repository.enabled());
        }
    }
}
