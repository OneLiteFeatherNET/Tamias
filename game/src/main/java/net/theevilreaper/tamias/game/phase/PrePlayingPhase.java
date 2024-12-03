package net.theevilreaper.tamias.game.phase;

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
import net.theevilreaper.tamias.game.util.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public final class PrePlayingPhase extends TimedPhase {

    private final Logger LOGGER = LoggerFactory.getLogger(PrePlayingPhase.class);
    private final TeamService<Team> teamService;
    private final Supplier<BaseMap> gameMapSupplier;
    private final VoidConsumer gamePreparation;
    private final Items items;

    public PrePlayingPhase(
            @NotNull TeamService<Team> teamService,
            @NotNull Supplier<BaseMap> gameMapSupplier,
            @NotNull VoidConsumer gamePreparation,
            @NotNull Items items
    ) {
        super("Pre-Playing", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(5);
        this.setTickDirection(TickDirection.DOWN);
        this.teamService = teamService;
        this.gameMapSupplier = gameMapSupplier;
        this.items = items;
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
        LOGGER.info("Allocated teams");
    }

    @Override
    protected void onFinish() {
        if (!(this.gameMapSupplier.get() instanceof GameMap gameMap)) {
            throw new IllegalStateException("The game map is not a valid game map");
        }
        this.gamePreparation.apply();
        //Teleportation
        TeamHelper.teleport(this.teamService, gameMap, this::updatePlayer);
    }

    @Override
    public void onUpdate() {
       LOGGER.info("Map is ");
    }

    /**
     * Updates the player with the correct team and items.
     *
     * @param player the player to update
     */
    private void updatePlayer(@NotNull Player player) {
        byte id = player.getTag(Tags.TEAM_ID);

        if (id == GameConfig.SURVIVOR_ID) {
            this.items.setShootItem(player);
        } else {
            this.items.setBombItem(player);
        }

        Team team = this.teamService.getTeams().get(id);

        Component displayName = Component.text(player.getUsername(), team.getColorData().getChatColor());
        player.setDisplayName(displayName);
    }
}
