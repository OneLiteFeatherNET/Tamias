package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.component.StaminaComponent;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RoleToBomberChangeListener implements Consumer<RoleToBomberChangeEvent> {

    private final TeamService teamService;
    private final StaminaService staminaService;
    private final Supplier<Pos> spawnSupplier;

    public RoleToBomberChangeListener(TeamService teamService, StaminaService staminaService, Supplier<Pos> spawnSupplier) {
        this.teamService = teamService;
        this.staminaService = staminaService;
        this.spawnSupplier = spawnSupplier;
    }

    @Override
    public void accept(RoleToBomberChangeEvent event) {
        Player player = event.getPlayer();

        Pos spawnPos = this.spawnSupplier.get();
        if (spawnPos == null) return;

        Team bomberTeam = this.teamService.getTeam(GameConfig.BOMBER_KEY).orElse(null);
        if (bomberTeam != null) {
            StaminaComponent staminaComp = bomberTeam.get(StaminaComponent.class);
            if (staminaComp != null) {
                this.staminaService.removeStaminaBar(player.getUuid());
                StaminaBar staminaBar = staminaComp.staminaFactory().apply(player);
                this.staminaService.add(player.getUuid(), staminaBar);
                staminaBar.start();
            }
        }

        player.teleport(spawnPos);
    }
}
