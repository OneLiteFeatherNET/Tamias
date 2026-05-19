package net.theevilreaper.tamias.game.phase.playing;

import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.game.stamina.event.StaminaCreateEvent;
import net.theevilreaper.tamias.game.util.Items;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.EntityHelper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

/**
 * The {@link PrePlayingPhase} deals each code logic that should be executed before the {@link PlayingPhase} begins.
 * It reduces the complexity of the playing phase without dealing too much overhead.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PrePlayingPhase extends TimedPhase {

    private final TeamService teamService;

    /**
     * Creates a new instance from the phase
     *
     * @param teamService the service that provides access to the teams
     */
    public PrePlayingPhase(@NotNull TeamService teamService) {
        super("Pre-Playing", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(5);
        this.setTickDirection(TickDirection.DOWN);
        this.teamService = teamService;
    }

    @Override
    public void onStart() {
        super.onStart();
        TeamHelper.allocateTeams(this.teamService);
    }

    @Override
    protected void onFinish() {
        //Teleportation
        Team tntTeam = this.teamService.getTeams().get(GameConfig.TNT_ID);
        EntityHelper.switchToTNT(tntTeam.getPlayers().stream().findFirst().get());
        EventDispatcher.call(new StaminaCreateEvent());
    }

    @Override
    public void onUpdate() {
        // Nothing to do here at the moment
    }

    /**
     * Updates the player with the correct team and items.
     *
     * @param player the player to update
     */
    private void updatePlayer(@NotNull Player player) {
        byte id = player.getTag(Tags.TEAM_ID);

        Items.setItemToPlayer(player, id);

        Team team = this.teamService.getTeams().get(id);

        //TODO: FIX ME
        /*Component displayName = Component.text(player.getUsername(), team.getColorData().getChatColor());
        player.setDisplayName(displayName);*/
    }
}
