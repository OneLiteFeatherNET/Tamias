package net.theevilreaper.tamias.game.team;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class TeamHelperRoundAllocationTest {

    @Test
    void reallocatingClearsPreviousRoundMembershipAndDefersItems(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(4, teamService);

        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();

        Player playerA = env.createPlayer(instance);
        Player playerB = env.createPlayer(instance);

        // Simulate the previous round's allocation: A was Bomber, B was Survivor.
        TeamHelper.addPlayerToTeam(bomberTeam, playerA);
        TeamHelper.addPlayerToTeam(survivorTeam, playerB);

        TeamHelper.allocateTeams(teamService);

        // Exactly one player per team - nobody is left double-booked from the previous round.
        assertEquals(1, bomberTeam.getPlayers().size());
        assertEquals(1, survivorTeam.getPlayers().size());

        Player newBomber = bomberTeam.getPlayers().iterator().next();
        Player newSurvivor = survivorTeam.getPlayers().iterator().next();

        // Whoever ends up Survivor must not still be rendered as TNT from a past round.
        assertEquals(EntityType.TNT, newBomber.getEntityType());
        assertEquals(EntityType.PLAYER, newSurvivor.getEntityType());

        // Roles are set, but weapon items aren't handed out until the round actually starts.
        assertTrue(isEmpty(newBomber));
        assertTrue(isEmpty(newSurvivor));

        TeamHelper.grantRoleItems(teamService);

        assertFalse(isEmpty(newBomber));
        assertFalse(isEmpty(newSurvivor));

        env.destroyInstance(instance, true);
    }

    private static boolean isEmpty(Player player) {
        return Arrays.stream(player.getInventory().getItemStacks()).allMatch(ItemStack::isAir);
    }
}
