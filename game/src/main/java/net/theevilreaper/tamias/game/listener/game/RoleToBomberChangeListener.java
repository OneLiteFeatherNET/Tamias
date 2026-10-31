package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.component.StaminaComponent;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.tamias.game.util.SpawnProtection;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles a survivor who became a bomber. The new bomber takes the place of a free TNT block.
 * If no TNT is left, the player becomes a spectator until the round ends.
 */
public final class RoleToBomberChangeListener implements Consumer<RoleToBomberChangeEvent> {

    private final TeamService teamService;
    private final StaminaService staminaService;
    private final Supplier<Pos> tntSupplier;
    private final VoidConsumer roundEndCheck;

    /**
     * Creates a new listener.
     *
     * @param teamService    the team service which contains the bomber team
     * @param staminaService the service which holds the stamina bars
     * @param tntSupplier    supplies the position of a free TNT block or null if no TNT is left
     * @param roundEndCheck  checks if the round can end after the player became a spectator
     */
    public RoleToBomberChangeListener(
            TeamService teamService,
            StaminaService staminaService,
            Supplier<Pos> tntSupplier,
            VoidConsumer roundEndCheck
    ) {
        this.teamService = teamService;
        this.staminaService = staminaService;
        this.tntSupplier = tntSupplier;
        this.roundEndCheck = roundEndCheck;
    }

    @Override
    public void accept(RoleToBomberChangeEvent event) {
        Player player = event.getPlayer();

        Pos tntPos = this.tntSupplier.get();
        if (tntPos == null) {
            this.staminaService.removeStaminaBar(player.getUuid());
            SpectatorHelper.enable(player);
            Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                    .sendMessage(GameMessages.getSpectatorMessage(player));
            this.roundEndCheck.apply();
            return;
        }

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

        // Stand exactly where the removed TNT block was, so the disguise replaces it seamlessly
        player.getInstance().setBlock(tntPos, Block.AIR);
        // Without an explicit velocity the client keeps its momentum and slides away from the TNT position
        player.teleport(tntPos.add(0.5, 0, 0.5), Vec.ZERO);
        SpawnProtection.enable(player);
    }
}
