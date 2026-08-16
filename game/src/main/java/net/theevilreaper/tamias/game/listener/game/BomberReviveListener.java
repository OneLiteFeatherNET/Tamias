package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.bomber.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.round.BomberTicketService;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.util.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("java:S3252")
public final class BomberReviveListener implements Consumer<BomberRequireSpawnEvent> {

    private static final Sound RESPAWN = Sound.sound(SoundEvent.BLOCK_ANVIL_PLACE, Sound.Source.MASTER, 1f, 0.65f);

    private final Function<Player, StaminaBar> barGetter;
    private final Supplier<Pos> spawnPos;
    private final BomberTicketService ticketService;
    private final VoidConsumer roundEndCheck;

    public BomberReviveListener(
            Function<Player, StaminaBar> barGetter,
            Supplier<Pos> spawnPos,
            BomberTicketService ticketService,
            VoidConsumer roundEndCheck
    ) {
        this.barGetter = barGetter;
        this.spawnPos = spawnPos;
        this.ticketService = ticketService;
        this.roundEndCheck = roundEndCheck;
    }

    @Override
    public void accept(BomberRequireSpawnEvent event) {
        Player player = event.getPlayer();

        if (!player.hasTag(Tags.TEAM_KEY)) return;

        String teamKey = player.getTag(Tags.TEAM_KEY);

        if (!GameConfig.BOMBER_KEY.asString().equals(teamKey)) return;

        Pos spawnPos = this.spawnPos.get();
        if (spawnPos == null) {
            event.setCancelled(true);
            //TODO: Light spectator mode is here required
            return;
        }

        if (!(this.barGetter.apply(player) instanceof ExplodeBar explodeBar)) {
            event.setCancelled(true);
            //TODO: Light spectator mode is here required
            return;
        }

        boolean ticketAvailable = this.ticketService.tryConsume();
        this.roundEndCheck.apply();

        if (!ticketAvailable) {
            event.setCancelled(true);
            //TODO: Light spectator mode is here required
            return;
        }

        Pos newSpawnPos = spawnPos.add(0, 1, 0);

        explodeBar.resetToDefaults();

        List<TimedPotion> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (TimedPotion activeEffect : activeEffects) {
            player.removeEffect(activeEffect.potion().effect());
        }

        Instance instance = player.getInstance();
        instance.setBlock(newSpawnPos, Block.AIR);

        player.teleport(newSpawnPos.add(0.5, 1, 0.5));
        player.playSound(RESPAWN);
        AttributeHelper.enableMovement(player);
        Items.setBombItem(player);
    }
}
