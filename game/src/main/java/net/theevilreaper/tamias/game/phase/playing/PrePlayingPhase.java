package net.theevilreaper.tamias.game.phase.playing;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.util.functional.VoidConsumer;
import de.icevizion.xerus.api.phase.TickDirection;
import de.icevizion.xerus.api.phase.TimedPhase;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.EntityHelper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The {@link PrePlayingPhase} deals each code logic which should be executed before the {@link PlayingPhase} begins.
 * It reduces the complexity of the playing phase without dealing too much overhead.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PrePlayingPhase extends TimedPhase {

    private final TeamService<Team> teamService;
    private final Supplier<BaseMap> gameMapSupplier;
    private final VoidConsumer gamePreparation;
    private final BiConsumer<Player, Integer> itemConsumer;

    /**
     * Creates a new instance from the phase
     *
     * @param teamService     the service which provides access to the teams
     * @param gameMapSupplier the supplier to access data from the map
     * @param gamePreparation the logic which should be executed during due to this phase
     * @param itemConsumer     the consumer which triggers the item set logic
     */
    public PrePlayingPhase(
            @NotNull TeamService<Team> teamService,
            @NotNull Supplier<BaseMap> gameMapSupplier,
            @NotNull VoidConsumer gamePreparation,
            @NotNull BiConsumer<Player, Integer> itemConsumer
    ) {
        super("Pre-Playing", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(5);
        this.setTickDirection(TickDirection.DOWN);
        this.teamService = teamService;
        this.gameMapSupplier = gameMapSupplier;
        this.itemConsumer = itemConsumer;
        this.gamePreparation = gamePreparation;

    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        TeamHelper.allocateTeams(this.teamService);
    }

    @Override
    protected void onFinish() {
        if (!(this.gameMapSupplier.get() instanceof GameMap gameMap)) {
            throw new IllegalStateException("The game map is not a valid game map");
        }
        this.gamePreparation.apply();
        //Teleportation
        TeamHelper.teleport(this.teamService, gameMap, this::updatePlayer);
        Team tntTeam = this.teamService.getTeams().get(GameConfig.TNT_ID);
        EntityHelper.switchToTNT(tntTeam.getPlayers().stream().findFirst().get());
    }

    @Override
    public void onUpdate() {
    }

    /**
     * Updates the player with the correct team and items.
     *
     * @param player the player to update
     */
    private void updatePlayer(@NotNull Player player) {
        byte id = player.getTag(Tags.TEAM_ID);

        this.itemConsumer.accept(player, ((int) id));

        Team team = this.teamService.getTeams().get(id);

        Component displayName = Component.text(player.getUsername(), team.getColorData().getChatColor());
        player.setDisplayName(displayName);
    }
}
