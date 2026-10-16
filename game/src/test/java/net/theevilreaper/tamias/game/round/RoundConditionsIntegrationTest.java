package net.theevilreaper.tamias.game.round;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.team.Team;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundConditionsIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testRoundKeepsRunningWithActiveBomber(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = Team.of(GameConfig.SURVIVOR_KEY, 4);
        Team bomberTeam = Team.of(GameConfig.BOMBER_KEY, 4);
        survivorTeam.addPlayer(env.createPlayer(instance));
        Player spectator = env.createPlayer(instance);
        SpectatorHelper.enable(spectator);
        bomberTeam.addPlayer(spectator);
        bomberTeam.addPlayer(env.createPlayer(instance));

        assertFalse(RoundConditions.isRoundOver(survivorTeam, bomberTeam));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndsWhenOnlySpectatingBombersAreLeft(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = Team.of(GameConfig.SURVIVOR_KEY, 4);
        Team bomberTeam = Team.of(GameConfig.BOMBER_KEY, 4);
        survivorTeam.addPlayer(env.createPlayer(instance));
        Player spectator = env.createPlayer(instance);
        SpectatorHelper.enable(spectator);
        bomberTeam.addPlayer(spectator);

        assertTrue(RoundConditions.isRoundOver(survivorTeam, bomberTeam));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndsWithoutSurvivors(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = Team.of(GameConfig.SURVIVOR_KEY, 4);
        Team bomberTeam = Team.of(GameConfig.BOMBER_KEY, 4);
        bomberTeam.addPlayer(env.createPlayer(instance));

        assertTrue(RoundConditions.isRoundOver(survivorTeam, bomberTeam));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndsWithoutBombers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = Team.of(GameConfig.SURVIVOR_KEY, 4);
        Team bomberTeam = Team.of(GameConfig.BOMBER_KEY, 4);
        survivorTeam.addPlayer(env.createPlayer(instance));

        assertTrue(RoundConditions.isRoundOver(survivorTeam, bomberTeam));

        env.destroyInstance(instance, true);
    }
}
