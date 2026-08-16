package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.bomber.BomberExplodeEvent;
import net.theevilreaper.tamias.game.round.BomberTicketService;
import net.theevilreaper.tamias.game.team.TeamHelper;
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

    private static final Potion BLINDNESS = new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE);

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
        Pos pos = player.getPosition().asPos();
        instance.explode((float) pos.x(), (float) pos.y(), (float) pos.z(), 1);
        player.addEffect(BLINDNESS);
        AttributeHelper.disableMovement(player);

        this.convertNearbySurvivors(instance, event.getPosition());
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
            if (!GameConfig.SURVIVOR_KEY.asString().equals(survivor.getTag(Tags.TEAM_KEY))) continue;

            boolean ticketAvailable = this.ticketService.tryConsume();
            this.roundEndCheck.apply();
            if (!ticketAvailable) return;

            TeamHelper.switchToTNTTeam(this.teamService, survivor);
        }
    }
}
