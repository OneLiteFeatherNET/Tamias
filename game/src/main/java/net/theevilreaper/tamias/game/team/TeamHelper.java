package net.theevilreaper.tamias.game.team;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.aves.util.Players;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import net.theevilreaper.tamias.game.team.component.EntityComponent;
import net.theevilreaper.tamias.game.team.component.ItemComponent;
import net.theevilreaper.tamias.game.team.component.StaminaComponent;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.tamias.game.util.Items;
import net.theevilreaper.xerus.api.ColorData;
import net.theevilreaper.xerus.api.component.team.ColorComponent;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class providing helper methods to load, find, assign, and manage teams.
 *
 * @author theEvilReaper
 * @version 1.3.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class TeamHelper {

    /**
     * Loads the initial game teams into the team service.
     * Survivor team is configured with green chat colors, shoot stamina bar, and shoot item.
     * Bomber team is configured with red chat colors, TNT entity component, explode stamina bar, and bomb item.
     *
     * @param teamSize    the maximum capacity of each team
     * @param teamService the team service where teams are registered
     */
    public static void loadTeams(int teamSize, TeamService teamService) {
        if (teamService.hasTeams()) return;

        Team survivorTeam = Team.of(GameConfig.SURVIVOR_KEY, teamSize);
        survivorTeam.add(ColorComponent.class, new ColorComponent(ColorData.GREEN));
        survivorTeam.add(StaminaComponent.class, new StaminaComponent(StaminaFactory::createShootBar));
        survivorTeam.add(ItemComponent.class, new ItemComponent(Items::setShootItem));
        teamService.add(survivorTeam);

        Team bomberTeam = Team.of(GameConfig.BOMBER_KEY, teamSize);
        bomberTeam.add(ColorComponent.class, new ColorComponent(ColorData.RED));
        bomberTeam.add(EntityComponent.class, new EntityComponent(EntityType.TNT));
        bomberTeam.add(StaminaComponent.class, new StaminaComponent(StaminaFactory::createExplodeBar));
        bomberTeam.add(ItemComponent.class, new ItemComponent(Items::setBombItem));
        teamService.add(bomberTeam);
    }

    /**
     * Adds a player to the target team and applies all attached team components
     * (e.g. team key tag, TNT entity transformation, initial team items).
     *
     * @param team   the team to receive the player
     * @param player the player to add
     */
    public static void addPlayerToTeam(Team team, Player player) {
        team.addPlayer(player);
        player.setTag(Tags.TEAM_KEY, team.key().asString());

        EntityComponent entityComp = team.get(EntityComponent.class);
        if (entityComp != null && entityComp.entityType() == EntityType.TNT) {
            EntityHelper.switchToTNT(player);
        }

        ItemComponent itemComp = team.get(ItemComponent.class);
        if (itemComp != null) {
            itemComp.itemApplier().accept(player);
        }
    }

    /**
     * Removes a player from a team, clearing their team tag and inventory, and showing them as a normal player again.
     *
     * @param team   the team from which the player is removed
     * @param player the player to remove
     */
    public static void removePlayerFromTeam(Team team, Player player) {
        team.removePlayer(player);
        player.removeTag(Tags.TEAM_KEY);
        player.getInventory().clear();
        EntityHelper.switchToPlayer(player);
    }

    /**
     * Switches a survivor player to the TNT/Bomber team during gameplay.
     *
     * @param teamService the team service
     * @param player      the survivor player to convert
     */
    public static void switchToTNTTeam(TeamService teamService, Player player) {
        Check.argCondition(!player.hasTag(Tags.TEAM_KEY), "Need a team tag for switching teams");
        Check.argCondition(!GameConfig.SURVIVOR_KEY.asString().equals(player.getTag(Tags.TEAM_KEY)), "The player must be a survivor");

        Team survivorTeam = getTeam(teamService, GameConfig.SURVIVOR_KEY);
        Team bomberTeam = getTeam(teamService, GameConfig.BOMBER_KEY);

        removePlayerFromTeam(survivorTeam, player);
        addPlayerToTeam(bomberTeam, player);
        EventDispatcher.call(new RoleToBomberChangeEvent(player));
    }

    /**
     * Allocates all online players into teams at game start.
     * Exactly one random player is chosen as the initial Bomber, while all other online players join the Survivor team.
     *
     * @param teamService the team service providing teams
     */
    public static void allocateTeams(TeamService teamService) {
        Check.argCondition(!teamService.hasTeams(), "The team service must contain teams");

        Set<Player> onlinePlayers = new HashSet<>(MinecraftServer.getConnectionManager().getOnlinePlayers());
        Player bomber = Players.getRandomPlayer()
                .orElseThrow(() -> new IllegalStateException("No online player found for bomber allocation"));
        onlinePlayers.remove(bomber);

        Team bomberTeam = getTeam(teamService, GameConfig.BOMBER_KEY);
        Team survivorTeam = getTeam(teamService, GameConfig.SURVIVOR_KEY);

        addPlayerToTeam(bomberTeam, bomber);
        onlinePlayers.forEach(survivor -> addPlayerToTeam(survivorTeam, survivor));
    }

    /**
     * Returns the team with the given key.
     *
     * @param teamService the service which holds the teams
     * @param key         the key of the team
     * @return the team
     * @throws IllegalStateException if the team does not exist
     */
    public static Team getTeam(TeamService teamService, Key key) {
        return teamService.getTeam(key)
                .orElseThrow(() -> new IllegalStateException("Team " + key.asString() + " not found"));
    }

    private TeamHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
