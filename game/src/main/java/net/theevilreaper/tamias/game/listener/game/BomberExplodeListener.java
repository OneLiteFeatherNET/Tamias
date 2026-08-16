package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.bomber.BomberExplodeEvent;
import net.theevilreaper.tamias.game.round.BomberTicketService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.Effects;
import net.theevilreaper.xerus.api.team.TeamService;

import java.util.function.Consumer;

/**
 * Handles a self-detonated Bomber and applies the blast, then converts every Survivor
 * within {@link #conversionRadius} blocks to Bomber, one ticket per conversion.
 * Conversion stops as soon as the shared {@link BomberTicketService} pool runs out.
 *
 * @author theEvilReaper
 * @version 2.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class BomberExplodeListener implements Consumer<BomberExplodeEvent> {

    private static final String SURVIVOR_KEY = GameConfig.SURVIVOR_KEY.asString();

    private final TeamService teamService;
    private final BomberTicketService ticketService;
    private final double conversionRadius;
    private final VoidConsumer roundEndCheck;

    public BomberExplodeListener(
            TeamService teamService,
            BomberTicketService ticketService,
            double conversionRadius,
            VoidConsumer roundEndCheck
    ) {
        this.teamService = teamService;
        this.ticketService = ticketService;
        this.conversionRadius = conversionRadius;
        this.roundEndCheck = roundEndCheck;
    }

    @Override
    public void accept(BomberExplodeEvent event) {
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        Vec blastPos = event.getPosition();
        Effects.applyBlastEffects(player, blastPos.asPos());

        this.convertNearbySurvivors(instance, blastPos);
        this.roundEndCheck.apply();
    }

    /**
     * Converts every Survivor within {@link #conversionRadius} blocks of the blast to Bomber,
     * one ticket per conversion, stopping as soon as the ticket pool is exhausted.
     *
     * @param instance the instance the blast happened in
     * @param blastPos the blast position
     */
    private void convertNearbySurvivors(Instance instance, Vec blastPos) {
        for (Entity nearby : instance.getNearbyEntities(blastPos, this.conversionRadius)) {
            if (!(nearby instanceof Player survivor)) continue;
            if (!survivor.hasTag(Tags.TEAM_KEY)) continue;
            if (!SURVIVOR_KEY.equals(survivor.getTag(Tags.TEAM_KEY))) continue;

            if (!this.ticketService.tryConsume()) return;

            TeamHelper.switchToTNTTeam(this.teamService, survivor);
        }
    }
}
